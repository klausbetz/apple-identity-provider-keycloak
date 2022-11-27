package at.klausbetz.provider;

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
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static at.klausbetz.provider.AppleIdentityProvider.OAUTH2_PARAMETER_CODE;

public class AppleIdentityProviderEndpoint {

    protected static final Logger logger = Logger.getLogger(AppleIdentityProviderEndpoint.class);

    private static final String OAUTH2_PARAMETER_STATE = "state";
    private static final String OAUTH2_PARAMETER_USER = "user";
    private static final String ACCESS_DENIED = "access_denied";

    private final AppleIdentityProvider appleIdentityProvider;
    private final RealmModel realm;
    private final IdentityProvider.AuthenticationCallback callback;
    private final EventBuilder event;

    @Context
    protected KeycloakSession session;

    @Context
    protected ClientConnection clientConnection;

    @Context
    protected HttpHeaders headers;

    public AppleIdentityProviderEndpoint(AppleIdentityProvider appleIdentityProvider, RealmModel realm, IdentityProvider.AuthenticationCallback callback, EventBuilder event) {
        this.appleIdentityProvider = appleIdentityProvider;
        this.realm = realm;
        this.callback = callback;
        this.event = event;
    }

    @POST
    public Response authResponse(@FormParam(OAUTH2_PARAMETER_STATE) String state, @FormParam(OAUTH2_PARAMETER_CODE) String authorizationCode, @FormParam(OAUTH2_PARAMETER_USER) String user, @FormParam(OAuth2Constants.ERROR) String error) {
        IdentityBrokerState idpState = IdentityBrokerState.encoded(state, realm);
        String clientId = idpState.getClientId();
        String tabId = idpState.getTabId();

        if (clientId == null || tabId == null) {
            logger.errorf("Invalid state parameter: %s", state);
            event.event(EventType.LOGIN);
            event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
            return ErrorPage.error(session, null, Response.Status.BAD_REQUEST, Messages.INVALID_REQUEST);
        }
        if (error != null) {
            logger.error(error + " for broker login " + appleIdentityProvider.getConfig().getProviderId());
            if (error.equals(ACCESS_DENIED)) {
                return callback.cancelled();
            } else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
                return callback.error(error);
            } else {
                return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
            }
        }

        ClientModel client = realm.getClientByClientId(clientId);
        AuthenticationSessionModel authSession = ClientSessionCode.getClientSession(state, tabId, session, realm, client, event, AuthenticationSessionModel.class);

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
        event.event(EventType.LOGIN);
        event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
        return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
    }
}
