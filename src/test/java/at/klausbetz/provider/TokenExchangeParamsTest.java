package at.klausbetz.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenExchangeParamsTest {

    private static final String VALID_USER_JSON = "{ \"name\": { \"firstName\": \"Klaus\", \"lastName\": \"Betz\" }, \"email\": \"my.fancy@email.com\" }";

    @Test
    void givenValidExchangeRequestWithAppIdentifier_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").subjectTokenType(AppleIdentityProvider.APPLE_AUTHZ_CODE).userJson(VALID_USER_JSON).appIdentifier("my.fancy.app").build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertEquals(VALID_USER_JSON, exchangeParams.getUserJson());
        assertEquals("my.fancy.app", exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithoutAppIdentifier_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").subjectTokenType(AppleIdentityProvider.APPLE_AUTHZ_CODE).userJson(VALID_USER_JSON).build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertEquals(VALID_USER_JSON, exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithInvalidAppIdentifier_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").subjectTokenType(AppleIdentityProvider.APPLE_AUTHZ_CODE).userJson(VALID_USER_JSON).appIdentifier("  ").build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertEquals(VALID_USER_JSON, exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithoutSubjectTokenType_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").userJson(VALID_USER_JSON).build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertEquals(VALID_USER_JSON, exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithInvalidSubjectTokenType_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").subjectTokenType("   ").userJson(VALID_USER_JSON).build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertEquals(VALID_USER_JSON, exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithInvalidUserJson_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").userJson("  ").build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertNull(exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenValidExchangeRequestWithNullUserJson_whenCreatingTokenExchangeParams_thenParamsAreParsedAndNormalized() {
        MultivaluedMap<String, String> params = new MultivaluedMapBuilder().subjectToken("myFancyAppleAuthorizationCode").userJson(null).build();
        TokenExchangeParams exchangeParams = new TokenExchangeParams(params);

        assertEquals("myFancyAppleAuthorizationCode", exchangeParams.getSubjectToken());
        assertEquals(AppleIdentityProvider.APPLE_AUTHZ_CODE, exchangeParams.getSubjectTokenType());
        assertNull(exchangeParams.getUserJson());
        assertNull(exchangeParams.getAppIdentifier());
    }

    @Test
    void givenIdTokenOrJwtTokenType_whenCreatingTokenExchangeParams_thenTreatedAsIdToken() {
        assertTrue(new TokenExchangeParams(new MultivaluedMapBuilder().subjectToken("appleIdToken").subjectTokenType(OAuth2Constants.ID_TOKEN_TYPE).build()).isIdToken());
        assertTrue(new TokenExchangeParams(new MultivaluedMapBuilder().subjectToken("appleIdToken").subjectTokenType(OAuth2Constants.JWT_TOKEN_TYPE).build()).isIdToken());
        assertFalse(new TokenExchangeParams(new MultivaluedMapBuilder().subjectToken("appleAuthzCode").build()).isIdToken());
        assertFalse(new TokenExchangeParams(new MultivaluedMapBuilder().subjectToken("token").subjectTokenType(OAuth2Constants.ACCESS_TOKEN_TYPE).build()).isIdToken());
    }
}
