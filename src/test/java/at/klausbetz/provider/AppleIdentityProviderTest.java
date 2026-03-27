package at.klausbetz.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.*;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.ErrorResponseException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppleIdentityProviderTest {

    @Mock(lenient = true)
    private KeycloakSession session;

    @Mock(lenient = true)
    private EventBuilder event;

    private AppleIdentityProviderConfig config;
    private AppleIdentityProvider provider;
    private IdentityProviderModel idpModel;

    @BeforeEach
    void setUp() {
        idpModel = new IdentityProviderModel();
        idpModel.setProviderId("apple");
        config = new AppleIdentityProviderConfig(idpModel);
        config.setAlias("apple");
        config.setClientId("com.example.app");
        config.setTeamId("ABCDE12345");
        config.setKeyId("KEY1234567");
        provider = new AppleIdentityProvider(session, config);
    }

    // ========== Constructor tests ==========

    @Nested
    class ConstructorTests {
        @Test
        void setsAuthorizationUrl() {
            assertEquals("https://appleid.apple.com/auth/authorize?response_mode=form_post", config.getAuthorizationUrl());
        }

        @Test
        void setsTokenUrl() {
            assertEquals("https://appleid.apple.com/auth/token", config.getTokenUrl());
        }

        @Test
        void setsJwksUrl() {
            assertEquals("https://appleid.apple.com/auth/keys", config.getJwksUrl());
        }

        @Test
        void setsValidateSignatureToTrue() {
            assertTrue(config.isValidateSignature());
        }

        @Test
        void setsUseJwksUrlToTrue() {
            assertTrue(config.isUseJwksUrl());
        }

        @Test
        void setsClientAuthMethodToPost() {
            assertEquals(OIDCLoginProtocol.CLIENT_SECRET_POST, config.getClientAuthMethod());
        }

        @Test
        void setsIssuer() {
            assertEquals("https://appleid.apple.com", config.getIssuer());
        }
    }

    // ========== Basic method tests ==========

    @Test
    void getDefaultScopes_returnsNameAndEmail() {
        // The parent constructor sets defaultScope from getDefaultScopes() and prepends "openid"
        String scope = config.getDefaultScope();
        assertTrue(scope.contains("name%20email"));
        assertTrue(scope.contains("openid"));
    }

    @Test
    void getConfig_returnsAppleIdentityProviderConfig() {
        assertInstanceOf(AppleIdentityProviderConfig.class, provider.getConfig());
        assertSame(config, provider.getConfig());
    }

    @Test
    void callback_returnsAppleIdentityProviderEndpoint() {
        RealmModel realm = mock(RealmModel.class);
        EventBuilder eventBuilder = mock(EventBuilder.class);
        Object endpoint = provider.callback(realm, mock(org.keycloak.broker.provider.UserAuthenticationIdentityProvider.AuthenticationCallback.class), eventBuilder);
        assertInstanceOf(AppleIdentityProviderEndpoint.class, endpoint);
    }

    // ========== parseUser tests (via reflection) ==========

    @Nested
    class ParseUserTests {

        @Test
        void withFullUserJson_parsesAllFields() throws Exception {
            String userJson = "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"email\":\"john@example.com\"}";
            AppleUserRepresentation result = invokeParseUser(userJson);

            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("john@example.com", result.getEmail());
            assertNotNull(result.getProfile());
        }

        @Test
        void withNameOnly_parsesNameFieldsAndEmailIsNull() throws Exception {
            String userJson = "{\"name\":{\"firstName\":\"Jane\",\"lastName\":\"Smith\"}}";
            AppleUserRepresentation result = invokeParseUser(userJson);

            assertEquals("Jane", result.getFirstName());
            assertEquals("Smith", result.getLastName());
            assertNull(result.getEmail());
        }

        @Test
        void withFirstNameOnly_parsesFirstNameOnly() throws Exception {
            String userJson = "{\"name\":{\"firstName\":\"Jane\"}}";
            AppleUserRepresentation result = invokeParseUser(userJson);

            assertEquals("Jane", result.getFirstName());
            assertNull(result.getLastName());
        }

        @Test
        void withEmailButNoName_emailIsNotParsed() throws Exception {
            // This documents a known bug: email extraction is nested inside the nameNode != null block
            String userJson = "{\"email\":\"john@example.com\"}";
            AppleUserRepresentation result = invokeParseUser(userJson);

            assertNull(result.getFirstName());
            assertNull(result.getLastName());
            assertNull(result.getEmail()); // BUG: email is not parsed when name is absent
            assertNotNull(result.getProfile());
        }

        @Test
        void withEmptyJson_returnsEmptyRepresentation() throws Exception {
            AppleUserRepresentation result = invokeParseUser("{}");

            assertNull(result.getFirstName());
            assertNull(result.getLastName());
            assertNull(result.getEmail());
            assertNotNull(result.getProfile());
        }

        @Test
        void withEmptyNameObject_returnsEmptyRepresentation() throws Exception {
            String userJson = "{\"name\":{},\"email\":\"john@example.com\"}";
            AppleUserRepresentation result = invokeParseUser(userJson);

            assertNull(result.getFirstName());
            assertNull(result.getLastName());
            // email IS parsed here because nameNode is not null (it's an empty object)
            assertEquals("john@example.com", result.getEmail());
        }

        @Test
        void withMalformedJson_throwsJsonProcessingException() {
            assertThrows(InvocationTargetException.class, () -> invokeParseUser("not json"));
        }
    }

    // ========== handleUserJson tests (via reflection) ==========

    @Nested
    class HandleUserJsonTests {

        @Test
        void setsFieldsWhenContextFieldsAreNull() throws Exception {
            BrokeredIdentityContext context = createContext();
            String userJson = "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"email\":\"john@example.com\"}";

            BrokeredIdentityContext result = invokeHandleUserJson(context, userJson);

            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("john@example.com", result.getEmail());
        }

        @Test
        void setsFieldsWhenContextFieldsAreBlank() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setFirstName("   ");
            context.setLastName("   ");
            context.setEmail("   ");
            String userJson = "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"email\":\"john@example.com\"}";

            BrokeredIdentityContext result = invokeHandleUserJson(context, userJson);

            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals("john@example.com", result.getEmail());
        }

        @Test
        void doesNotOverrideExistingFields() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setFirstName("Existing");
            context.setLastName("User");
            context.setEmail("existing@example.com");
            String userJson = "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"email\":\"john@example.com\"}";

            BrokeredIdentityContext result = invokeHandleUserJson(context, userJson);

            assertEquals("Existing", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals("existing@example.com", result.getEmail());
        }

        @Test
        void malformedJson_returnsContextUnchanged() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setFirstName("Existing");

            BrokeredIdentityContext result = invokeHandleUserJson(context, "not valid json");

            assertSame(context, result);
            assertEquals("Existing", result.getFirstName());
        }

        @Test
        void partialUserJson_setsOnlyAvailableFields() throws Exception {
            BrokeredIdentityContext context = createContext();
            String userJson = "{\"name\":{\"firstName\":\"John\"}}";

            BrokeredIdentityContext result = invokeHandleUserJson(context, userJson);

            assertEquals("John", result.getFirstName());
            assertNull(result.getLastName());
        }
    }

    // ========== isValidSecret tests (via reflection) ==========

    @Nested
    class IsValidSecretTests {

        @Test
        void withNull_returnsFalse() throws Exception {
            assertFalse(invokeIsValidSecret(null));
        }

        @Test
        void withEmptyString_returnsFalse() throws Exception {
            assertFalse(invokeIsValidSecret(""));
        }

        @Test
        void withNonJwsString_returnsFalse() throws Exception {
            assertFalse(invokeIsValidSecret("not-a-jws-token"));
        }

        @Test
        void withRandomString_returnsFalse() throws Exception {
            assertFalse(invokeIsValidSecret("abc.def.ghi"));
        }
    }

    // ========== generateClientToken tests (via reflection) ==========

    @Nested
    class GenerateClientTokenTests {

        @Test
        void setsCorrectClaims() throws Exception {
            JsonWebToken token = invokeGenerateClientToken("TEAM123", "com.example.app");

            assertEquals("TEAM123", token.getIssuer());
            assertEquals("com.example.app", token.getSubject());
            String[] audience = token.getAudience();
            assertNotNull(audience);
            assertEquals("https://appleid.apple.com", audience[0]);
            assertNotNull(token.getIat());
            assertTrue(token.getExp() > token.getIat());
        }

        @Test
        void expiresIn180Days() throws Exception {
            JsonWebToken token = invokeGenerateClientToken("TEAM123", "com.example.app");

            long expectedDuration = 86400L * 180;
            assertEquals(expectedDuration, token.getExp() - token.getIat());
        }
    }

    // ========== exchangeExternalTokenV1Impl tests ==========

    @Nested
    class ExchangeExternalTokenTests {

        @Test
        void nullSubjectToken_throwsErrorResponseWithInvalidToken() {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

            ErrorResponseException ex = assertThrows(ErrorResponseException.class,
                    () -> provider.exchangeExternalTokenV1Impl(event, params));

            assertEquals(400, ex.getResponse().getStatus());
            verify(event).error(Errors.INVALID_TOKEN);
        }

        @Test
        void invalidTokenType_throwsErrorResponseWithInvalidTokenType() {
            MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
            params.putSingle(OAuth2Constants.SUBJECT_TOKEN, "someToken");
            params.putSingle(OAuth2Constants.SUBJECT_TOKEN_TYPE, "unsupported-type");

            ErrorResponseException ex = assertThrows(ErrorResponseException.class,
                    () -> provider.exchangeExternalTokenV1Impl(event, params));

            assertEquals(400, ex.getResponse().getStatus());
            verify(event).error(Errors.INVALID_TOKEN_TYPE);
        }
    }

    // ========== autoLinkIfPossible tests (via reflection) ==========

    @Nested
    class AutoLinkIfPossibleTests {

        @Mock(lenient = true)
        private KeycloakContext keycloakContext;

        @Mock(lenient = true)
        private RealmModel realm;

        @Mock(lenient = true)
        private UserProvider userProvider;

        @BeforeEach
        void setUpMocks() {
            lenient().when(session.getContext()).thenReturn(keycloakContext);
            lenient().when(keycloakContext.getRealm()).thenReturn(realm);
            lenient().when(session.users()).thenReturn(userProvider);
        }

        @Test
        void blankEmail_doesNotQueryUsers() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setEmail("   ");

            invokeAutoLinkIfPossible(context);

            verifyNoInteractions(userProvider);
        }

        @Test
        void nullEmail_doesNotQueryUsers() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setEmail(null);

            invokeAutoLinkIfPossible(context);

            verifyNoInteractions(userProvider);
        }

        @Test
        void noExistingUser_doesNotLink() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setEmail("unknown@example.com");
            when(userProvider.getUserByEmail(realm, "unknown@example.com")).thenReturn(null);

            invokeAutoLinkIfPossible(context);

            verify(userProvider, never()).addFederatedIdentity(any(), any(), any());
        }

        @Test
        void alreadyLinked_doesNotAddNewLink() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setEmail("linked@example.com");
            UserModel existingUser = mock(UserModel.class);
            FederatedIdentityModel existingLink = mock(FederatedIdentityModel.class);
            when(userProvider.getUserByEmail(realm, "linked@example.com")).thenReturn(existingUser);
            when(userProvider.getFederatedIdentity(realm, existingUser, "apple")).thenReturn(existingLink);

            invokeAutoLinkIfPossible(context);

            verify(userProvider, never()).addFederatedIdentity(any(), any(), any());
        }

        @Test
        void existingUserNotLinked_addsNewFederatedIdentity() throws Exception {
            BrokeredIdentityContext context = createContext();
            context.setId("apple-sub-123");
            context.setUsername("appleuser");
            context.setEmail("user@example.com");
            UserModel existingUser = mock(UserModel.class);
            when(userProvider.getUserByEmail(realm, "user@example.com")).thenReturn(existingUser);
            when(userProvider.getFederatedIdentity(realm, existingUser, "apple")).thenReturn(null);

            invokeAutoLinkIfPossible(context);

            verify(userProvider).addFederatedIdentity(eq(realm), eq(existingUser), any(FederatedIdentityModel.class));
        }
    }

    // ========== getFederatedIdentity(String, String) tests ==========

    @Nested
    class GetFederatedIdentityWithUserDataTests {

        @Test
        void withUserData_setsNameAndStoresProfile() throws Exception {
            AppleIdentityProvider spy = spy(provider);
            BrokeredIdentityContext mockContext = createContext();
            doReturn(mockContext).when(spy).getFederatedIdentity(anyString());

            String userData = "{\"name\":{\"firstName\":\"John\",\"lastName\":\"Doe\"},\"email\":\"john@example.com\"}";
            BrokeredIdentityContext result = spy.getFederatedIdentity(userData, "{}");

            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
        }

        @Test
        void withoutUserData_doesNotSetNames() throws Exception {
            AppleIdentityProvider spy = spy(provider);
            BrokeredIdentityContext mockContext = createContext();
            doReturn(mockContext).when(spy).getFederatedIdentity(anyString());

            BrokeredIdentityContext result = spy.getFederatedIdentity(null, "{}");

            assertNull(result.getFirstName());
            assertNull(result.getLastName());
        }

        @Test
        void withUserData_storesProfileInContextData() throws Exception {
            AppleIdentityProvider spy = spy(provider);
            BrokeredIdentityContext mockContext = createContext();
            doReturn(mockContext).when(spy).getFederatedIdentity(anyString());

            String userData = "{\"name\":{\"firstName\":\"John\"}}";
            BrokeredIdentityContext result = spy.getFederatedIdentity(userData, "{}");

            // AbstractJsonUserAttributeMapper stores profile under OIDCIdentityProvider.USER_INFO key
            assertNotNull(result.getContextData().get("UserInfo"));
        }
    }

    // ========== Reflection helper methods ==========

    private AppleUserRepresentation invokeParseUser(String userJson) throws Exception {
        Method method = AppleIdentityProvider.class.getDeclaredMethod("parseUser", String.class);
        method.setAccessible(true);
        return (AppleUserRepresentation) method.invoke(provider, userJson);
    }

    private BrokeredIdentityContext invokeHandleUserJson(BrokeredIdentityContext context, String userJson) throws Exception {
        Method method = AppleIdentityProvider.class.getDeclaredMethod("handleUserJson", BrokeredIdentityContext.class, String.class);
        method.setAccessible(true);
        return (BrokeredIdentityContext) method.invoke(provider, context, userJson);
    }

    private boolean invokeIsValidSecret(String clientSecret) throws Exception {
        Method method = AppleIdentityProvider.class.getDeclaredMethod("isValidSecret", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(provider, clientSecret);
    }

    private JsonWebToken invokeGenerateClientToken(String teamId, String clientId) throws Exception {
        Method method = AppleIdentityProvider.class.getDeclaredMethod("generateClientToken", String.class, String.class);
        method.setAccessible(true);
        return (JsonWebToken) method.invoke(provider, teamId, clientId);
    }

    private void invokeAutoLinkIfPossible(BrokeredIdentityContext context) throws Exception {
        Method method = AppleIdentityProvider.class.getDeclaredMethod("autoLinkIfPossible", BrokeredIdentityContext.class);
        method.setAccessible(true);
        method.invoke(provider, context);
    }

    private BrokeredIdentityContext createContext() {
        return new BrokeredIdentityContext("test-id", idpModel);
    }
}
