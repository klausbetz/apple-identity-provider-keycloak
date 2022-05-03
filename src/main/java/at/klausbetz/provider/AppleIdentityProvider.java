package at.klausbetz.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.OIDCIdentityProvider;
import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Base64;
import org.keycloak.common.util.Time;
import org.keycloak.crypto.KeyWrapper;
import org.keycloak.crypto.ServerECDSASignatureSignerContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.vault.VaultStringSecret;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class AppleIdentityProvider extends OIDCIdentityProvider implements SocialIdentityProvider<OIDCIdentityProviderConfig> {

    public static final String OAUTH2_PARAMETER_CODE = "code";

    private static final Logger logger = Logger.getLogger(AppleIdentityProvider.class);
    private static final String AUTH_URL = "https://appleid.apple.com/auth/authorize?response_mode=form_post";
    private static final String TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String ISSUER = "https://appleid.apple.com";
    static final String APPLE_AUTHZ_CODE = "apple-authz-code";

    @Context
    private ClientConnection clientConnection;

    public AppleIdentityProvider(KeycloakSession session, AppleIdentityProviderConfig config) {
        super(session, config);

        config.setAuthorizationUrl(AUTH_URL);
        config.setTokenUrl(TOKEN_URL);
        config.setClientAuthMethod(OIDCLoginProtocol.CLIENT_SECRET_POST);
        config.setIssuer(ISSUER);
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new AppleIdentityProviderEndpoint(this, realm, callback, event);
    }

    @Override
    protected String getDefaultScopes() {
        return "openid%20name%20email";
    }

    @Override
    public AppleIdentityProviderConfig getConfig() {
        return (AppleIdentityProviderConfig) super.getConfig();
    }

    @Override
    protected BrokeredIdentityContext exchangeExternalImpl(EventBuilder event, MultivaluedMap<String, String> params) {
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);
        if (exchangeParams.getSubjectToken() == null) {
            event.detail(Details.REASON, OAuth2Constants.SUBJECT_TOKEN + " param unset");
            event.error(Errors.INVALID_TOKEN);
            throw new ErrorResponseException(OAuthErrorException.INVALID_TOKEN, "token not set", Response.Status.BAD_REQUEST);
        }

        if (OAuth2Constants.JWT_TOKEN_TYPE.equals(exchangeParams.getSubjectTokenType()) || OAuth2Constants.ID_TOKEN_TYPE.equals(exchangeParams.getSubjectTokenType())) {
            return validateJwt(event, exchangeParams.getSubjectToken(), exchangeParams.getSubjectTokenType());
        } else if (APPLE_AUTHZ_CODE.equals(exchangeParams.getSubjectTokenType())) {
            return exchangeAuthorizationCode(exchangeParams.getSubjectToken(), exchangeParams.getUserJson(), exchangeParams.getAppIdentifier());
        } else {
            event.detail(Details.REASON, OAuth2Constants.SUBJECT_TOKEN_TYPE + " invalid");
            event.error(Errors.INVALID_TOKEN_TYPE);
            throw new ErrorResponseException(OAuthErrorException.INVALID_TOKEN, "invalid token type", Response.Status.BAD_REQUEST);
        }
    }

    public void prepareClientSecret(String clientId) {
        if (!isValidSecret(getConfig().getClientSecret())) {
            getConfig().setClientSecret(generateJWS(
                    getConfig().getP8Content(),
                    getConfig().getKeyId(),
                    getConfig().getTeamId(),
                    clientId)
            );
        }
    }

    public BrokeredIdentityContext sendTokenRequest(String authorizationCode, String clientId, String userDataJson, AuthenticationSessionModel authSession) throws IOException {
        SimpleHttp.Response response = generateTokenRequest(authorizationCode, clientId).asResponse();

        if (response.getStatus() > 299) {
            logger.warn("Error response from apple: status=" + response.getStatus() + ", body=" + response.asString());
            return null;
        }

        BrokeredIdentityContext federatedIdentity = getFederatedIdentity(userDataJson, response.asString());
        federatedIdentity.setIdpConfig(getConfig());
        federatedIdentity.setIdp(AppleIdentityProvider.this);
        federatedIdentity.setAuthenticationSession(authSession);
        return federatedIdentity;
    }

    public BrokeredIdentityContext getFederatedIdentity(String userData, String response) throws JsonProcessingException {
        BrokeredIdentityContext user = AppleIdentityProvider.this.getFederatedIdentity(response);

        if (userData != null) {
            JsonNode profile = mapper.readTree(userData);
            JsonNode nameNode = profile.get("name");
            if (nameNode != null) {
                JsonNode firstNameNode = nameNode.get("firstName");
                if (firstNameNode != null) {
                    user.setFirstName(firstNameNode.asText());
                }
                JsonNode lastNameNode = nameNode.get("lastName");
                if (lastNameNode != null) {
                    user.setLastName(lastNameNode.asText());
                }
            }

            AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        }

        return user;
    }

    public SimpleHttp generateTokenRequest(String authorizationCode, String clientId) {
        KeycloakContext context = session.getContext();
        VaultStringSecret clientSecret = session.vault().getStringSecret(getConfig().getClientSecret());
        return SimpleHttp.doPost(getConfig().getTokenUrl(), session)
                         .param(OAUTH2_PARAMETER_CODE, authorizationCode)
                         .param(OAUTH2_PARAMETER_REDIRECT_URI, Urls.identityProviderAuthnResponse(context.getUri().getBaseUri(), getConfig().getAlias(), context.getRealm().getName()).toString())
                         .param(OAUTH2_PARAMETER_GRANT_TYPE, OAUTH2_GRANT_TYPE_AUTHORIZATION_CODE)
                         .param(OAUTH2_PARAMETER_CLIENT_ID, clientId)
                         .param(OAUTH2_PARAMETER_CLIENT_SECRET, clientSecret.get().orElse(getConfig().getClientSecret()));
    }

    private String generateJWS(String p8Content, String keyId, String teamId, String clientId) {
        try {
            KeyFactory kf = KeyFactory.getInstance("ECDSA");

            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.decode(
                    p8Content
                            .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                            .replaceAll("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\n", "")
                            .replaceAll(" ", "")
            ));
            PrivateKey privateKey = kf.generatePrivate(keySpecPKCS8);
            KeyWrapper keyWrapper = new KeyWrapper();
            keyWrapper.setAlgorithm("ES256");
            keyWrapper.setPrivateKey(privateKey);
            keyWrapper.setKid(keyId);

            return new JWSBuilder()
                    .jsonContent(generateClientToken(teamId, clientId))
                    .sign(new ServerECDSASignatureSignerContext(keyWrapper));
        } catch (Exception e) {
            logger.error("Unable to generate JWS");
        }
        return null;
    }

    private boolean isValidSecret(String clientSecret) {
        if (clientSecret != null && clientSecret.length() > 0) {
            try {
                JWSInput jws = new JWSInput(clientSecret);
                JsonWebToken token = jws.readJsonContent(JsonWebToken.class);
                return !token.isExpired();
            } catch (JWSInputException e) {
                logger.debug("Secret is not a valid JWS");
            }
        }
        return false;
    }

    private JsonWebToken generateClientToken(String teamId, String clientId) {
        JsonWebToken jwt = new JsonWebToken();
        jwt.issuer(teamId);
        jwt.subject(clientId);
        jwt.audience(ISSUER);
        jwt.iat((long) Time.currentTime());
        jwt.exp(jwt.getIat() + 86400 * 180);
        return jwt;
    }

    private BrokeredIdentityContext exchangeAuthorizationCode(String authorizationCode, String userJson, String appIdentifier) {
        String clientId = appIdentifier != null && !appIdentifier.isBlank() ? appIdentifier : getConfig().getClientId();
        try {
            prepareClientSecret(clientId);
            return sendTokenRequest(authorizationCode, clientId, userJson, null);
        } catch (IOException e) {
            logger.warn("Error exchanging apple authorization_code. clientId=" + clientId, e);
            return null;
        }
    }
}