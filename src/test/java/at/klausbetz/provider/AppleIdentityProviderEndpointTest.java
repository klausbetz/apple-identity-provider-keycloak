package at.klausbetz.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.UserAuthenticationIdentityProvider;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.keycloak.common.util.Base64Url;

import static org.mockito.Mockito.*;

/**
 * Tests for AppleIdentityProviderEndpoint.
 * <p>
 * Note: jakarta.ws.rs.core.Response cannot be mocked without a JAX-RS RuntimeDelegate
 * implementation on the classpath. The callback mock methods return null by default,
 * which is sufficient for interaction verification.
 */
@ExtendWith(MockitoExtension.class)
class AppleIdentityProviderEndpointTest {

    @Mock(lenient = true)
    private AppleIdentityProvider appleIdentityProvider;

    @Mock(lenient = true)
    private AppleIdentityProviderConfig appleConfig;

    @Mock(lenient = true)
    private RealmModel realm;

    @Mock(lenient = true)
    private UserAuthenticationIdentityProvider.AuthenticationCallback callback;

    @Mock(lenient = true)
    private EventBuilder event;

    @Mock(lenient = true)
    private KeycloakSession session;

    @Mock(lenient = true)
    private KeycloakContext keycloakContext;

    @Mock(lenient = true)
    private KeycloakUriInfo uriInfo;

    @Mock(lenient = true)
    private AuthenticationSessionModel authSession;

    private AppleIdentityProviderEndpoint endpoint;

    @BeforeEach
    void setUp() {
        when(appleIdentityProvider.getConfig()).thenReturn(appleConfig);
        when(appleConfig.getProviderId()).thenReturn("apple");
        when(appleConfig.getAlias()).thenReturn("apple");
        when(appleConfig.getClientId()).thenReturn("com.example.app");

        when(session.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getUri()).thenReturn(uriInfo);
        when(keycloakContext.getRealm()).thenReturn(realm);
        when(realm.getName()).thenReturn("myRealm");
        when(uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost:8080/auth"));

        endpoint = new AppleIdentityProviderEndpoint(appleIdentityProvider, realm, callback, event, session);
    }

    // ========== Error parameter handling ==========

    @Test
    void authResponse_accessDenied_callsCancelled() {
        String state = createValidState();
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

        endpoint.authResponse(state, null, null, "access_denied");

        verify(callback).cancelled(appleConfig);
        verify(event).event(EventType.IDENTITY_PROVIDER_LOGIN);
        verify(event).error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
    }

    @Test
    void authResponse_userCancelledAuthorize_callsCancelled() {
        String state = createValidState();
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

        endpoint.authResponse(state, null, null, "user_cancelled_authorize");

        verify(callback).cancelled(appleConfig);
    }

    @Test
    void authResponse_loginRequired_callsCallbackError() {
        String state = createValidState();
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

        endpoint.authResponse(state, null, null, OAuthErrorException.LOGIN_REQUIRED);

        verify(callback).error(appleConfig, OAuthErrorException.LOGIN_REQUIRED);
    }

    @Test
    void authResponse_interactionRequired_callsCallbackError() {
        String state = createValidState();
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

        endpoint.authResponse(state, null, null, OAuthErrorException.INTERACTION_REQUIRED);

        verify(callback).error(appleConfig, OAuthErrorException.INTERACTION_REQUIRED);
    }

    @Test
    void authResponse_unknownError_callsCallbackErrorWithUnexpectedMessage() {
        String state = createValidState();
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

        endpoint.authResponse(state, null, null, "some_unknown_error");

        verify(callback).error(eq(appleConfig), eq("identityProviderUnexpectedErrorMessage"));
    }

    // ========== Successful code exchange ==========

    @Test
    void authResponse_withAuthorizationCode_preparesSecretAndExchangesToken() throws Exception {
        String state = createValidState();
        BrokeredIdentityContext federatedIdentity = mock(BrokeredIdentityContext.class);
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);
        when(appleIdentityProvider.sendTokenRequest(anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(federatedIdentity);

        endpoint.authResponse(state, "auth-code-123", null, null);

        verify(appleIdentityProvider).prepareClientSecret("com.example.app");
        verify(appleIdentityProvider).sendTokenRequest(eq("auth-code-123"), eq("com.example.app"), isNull(), eq(authSession), anyString());
        verify(callback).authenticated(federatedIdentity);
    }

    @Test
    void authResponse_withAuthorizationCodeAndUserData_passesUserToSendTokenRequest() throws Exception {
        String state = createValidState();
        BrokeredIdentityContext federatedIdentity = mock(BrokeredIdentityContext.class);
        when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);
        when(appleIdentityProvider.sendTokenRequest(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(federatedIdentity);

        String userJson = "{\"name\":{\"firstName\":\"John\"}}";
        endpoint.authResponse(state, "auth-code-123", userJson, null);

        verify(appleIdentityProvider).sendTokenRequest(eq("auth-code-123"), eq("com.example.app"), eq(userJson), eq(authSession), anyString());
    }

    @Test
    void authResponse_sendTokenRequestReturnsNull_doesNotCallAuthenticated() throws Exception {
        try (MockedStatic<ErrorPage> errorPageMock = mockStatic(ErrorPage.class)) {
            String state = createValidState();
            when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);
            when(appleIdentityProvider.sendTokenRequest(anyString(), anyString(), any(), any(), anyString()))
                    .thenReturn(null);

            endpoint.authResponse(state, "auth-code-123", null, null);

            verify(callback, never()).authenticated(any());
        }
    }

    @Test
    void authResponse_sendTokenRequestThrowsException_doesNotCallAuthenticated() throws Exception {
        try (MockedStatic<ErrorPage> errorPageMock = mockStatic(ErrorPage.class)) {
            String state = createValidState();
            when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);
            when(appleIdentityProvider.sendTokenRequest(anyString(), anyString(), any(), any(), anyString()))
                    .thenThrow(new RuntimeException("Network error"));

            endpoint.authResponse(state, "auth-code-123", null, null);

            verify(callback, never()).authenticated(any());
        }
    }

    @Test
    void authResponse_nullAuthorizationCode_doesNotCallSendTokenRequest() throws Exception {
        try (MockedStatic<ErrorPage> errorPageMock = mockStatic(ErrorPage.class)) {
            String state = createValidState();
            when(callback.getAndVerifyAuthenticationSession(state)).thenReturn(authSession);

            endpoint.authResponse(state, null, null, null);

            verify(appleIdentityProvider, never()).sendTokenRequest(any(), any(), any(), any(), any());
            verify(callback, never()).authenticated(any());
        }
    }

    // ========== Helpers ==========

    private String createValidState() {
        String encodedClientId = Base64Url.encode("client".getBytes(StandardCharsets.UTF_8));
        return "state123.tab456." + encodedClientId;
    }
}
