package at.klausbetz.provider;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppleAccountLinkingTest {

    private static JsonWebToken token(Object email, Object emailVerified) {
        JsonWebToken token = new JsonWebToken();
        if (email != null) {
            token.setOtherClaims(IDToken.EMAIL, email);
        }
        if (emailVerified != null) {
            token.setOtherClaims(IDToken.EMAIL_VERIFIED, emailVerified);
        }
        return token;
    }

    @Test
    void givenVerifiedEmailInToken_whenChecking_thenLinkAllowed() {
        assertTrue(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", true), "user@example.com"));
        assertTrue(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", "true"), "USER@example.com"));
    }

    @Test
    void givenUnverifiedEmailInToken_whenChecking_thenLinkDenied() {
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", false), "user@example.com"));
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", "false"), "user@example.com"));
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", null), "user@example.com"));
    }

    @Test
    void givenEmailNotFromToken_whenChecking_thenLinkDenied() {
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token(null, true), "victim@example.com"));
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token("attacker@example.com", true), "victim@example.com"));
    }

    @Test
    void givenNoToken_whenChecking_thenLinkDenied() {
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(null, "user@example.com"));
        assertFalse(AppleAccountLinking.isVerifiedTokenEmail(token("user@example.com", true), null));
    }
}
