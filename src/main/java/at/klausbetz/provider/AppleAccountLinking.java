package at.klausbetz.provider;

import org.keycloak.representations.IDToken;
import org.keycloak.representations.JsonWebToken;

final class AppleAccountLinking {

    private AppleAccountLinking() {
    }

    static boolean isVerifiedTokenEmail(JsonWebToken validatedIdToken, String email) {
        if (validatedIdToken == null || email == null || email.isBlank() || validatedIdToken.getOtherClaims() == null) {
            return false;
        }

        Object tokenEmail = validatedIdToken.getOtherClaims().get(IDToken.EMAIL);
        Object emailVerified = validatedIdToken.getOtherClaims().get(IDToken.EMAIL_VERIFIED);

        return tokenEmail != null
                && email.equalsIgnoreCase(tokenEmail.toString())
                && emailVerified != null
                && Boolean.parseBoolean(emailVerified.toString());
    }
}
