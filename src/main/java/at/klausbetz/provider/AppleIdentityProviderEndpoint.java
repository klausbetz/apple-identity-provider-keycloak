package at.klausbetz.provider;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.OAuth2Constants;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import static at.klausbetz.provider.AppleIdentityProvider.OAUTH2_PARAMETER_CODE;

public class AppleIdentityProviderEndpoint {

    protected static final Logger logger = Logger.getLogger(AppleIdentityProviderEndpoint.class);

    private static final String OAUTH2_PARAMETER_STATE = "state";
    private static final String OAUTH2_PARAMETER_USER = "user";
    private static final String ACCESS_DENIED = "access_denied";
    private static final String USER_CANCELLED_AUTHORIZE = "user_cancelled_authorize";

    private final AppleIdentityProvider appleIdentityProvider;
    private final RealmModel realm;
    private final IdentityProvider.AuthenticationCallback callback;
    private final EventBuilder event;

    protected KeycloakSession session;

    protected ClientConnection clientConnection;

    @Context
    protected HttpHeaders headers;

    public AppleIdentityProviderEndpoint(AppleIdentityProvider appleIdentityProvider, RealmModel realm, IdentityProvider.AuthenticationCallback callback, EventBuilder event, KeycloakSession session, ClientConnection clientConnection) {
        this.appleIdentityProvider = appleIdentityProvider;
        this.realm = realm;
        this.callback = callback;
        this.event = event;
        this.session = session;
        this.clientConnection = clientConnection;
    }

    @POST
    public Response authResponse(@FormParam(OAUTH2_PARAMETER_STATE) String state, @FormParam(OAUTH2_PARAMETER_CODE) String authorizationCode, @FormParam(OAUTH2_PARAMETER_USER) String user, @FormParam(OAuth2Constants.ERROR) String error) {
        if (state == null) {
            return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_MISSING_STATE_ERROR);
        }

        IdentityBrokerState idpState = IdentityBrokerState.encoded(state, realm);
        String clientId = idpState.getClientId();
        String tabId = idpState.getTabId();
        if (clientId == null || tabId == null) {
            logger.errorf("Invalid state parameter: %s", state);
            return errorIdentityProviderLogin(Messages.INVALID_REQUEST, Response.Status.BAD_REQUEST);
        }

        AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
        session.getContext().setAuthenticationSession(authSession);

        if (error != null) {
            logger.warn(error + " for broker login " + appleIdentityProvider.getConfig().getProviderId());
            if (error.equals(ACCESS_DENIED) || error.equals(USER_CANCELLED_AUTHORIZE)) {
                return callback.cancelled(this.appleIdentityProvider.getConfig());
            } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
                return callback.error(error);
            } else {
                return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            }
        }

        try {
            if (authorizationCode != null) {
                appleIdentityProvider.prepareClientSecret(appleIdentityProvider.getConfig().getClientId());
                BrokeredIdentityContext federatedIdentity = appleIdentityProvider.sendTokenRequest(authorizationCode, appleIdentityProvider.getConfig().getClientId(), user, authSession);
                return callback.authenticated(federatedIdentity);
            }
        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            logger.error("Failed to complete apple identity provider oauth callback", e);
        }
        return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
    }

    private Response errorIdentityProviderLogin(String message) {
        return errorIdentityProviderLogin(message, Response.Status.BAD_GATEWAY);
    }

    private Response errorIdentityProviderLogin(String message, Response.Status status) {
        event.event(EventType.IDENTITY_PROVIDER_LOGIN);
        event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
        return ErrorPage.error(session, null, status, message);
    }
}
