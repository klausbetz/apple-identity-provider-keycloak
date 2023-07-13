package at.klausbetz.provider;

import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.OAuth2Constants;

public class TokenExchangeParams {
    private String appIdentifier;
    private String subjectToken;
    private String subjectTokenType;
    private String userJson;

    public TokenExchangeParams(MultivaluedMap<String, String> params) {
        this.subjectToken = params.getFirst(OAuth2Constants.SUBJECT_TOKEN);
        this.subjectTokenType = params.getFirst(OAuth2Constants.SUBJECT_TOKEN_TYPE);
        this.userJson = params.getFirst("user_profile");
        this.appIdentifier = params.getFirst("app_identifier");

        this.normalizeAppIdentifier();
        this.setTypeDefaultIfNull();
        this.normalizeUserJson();
    }

    private void normalizeAppIdentifier() {
        if (this.appIdentifier != null && this.appIdentifier.isBlank()) {
            this.appIdentifier = null;
        }
    }

    private void setTypeDefaultIfNull() {
        if (this.subjectTokenType == null || this.subjectTokenType.isBlank()) {
            this.subjectTokenType = AppleIdentityProvider.APPLE_AUTHZ_CODE;
        }
    }

    private void normalizeUserJson() {
        if (this.userJson != null && (this.userJson.isBlank() || this.userJson.equals("null"))) {
            this.userJson = null;
        }
    }

    public String getAppIdentifier() {
        return this.appIdentifier;
    }

    public String getSubjectToken() {
        return this.subjectToken;
    }

    public String getSubjectTokenType() {
        return this.subjectTokenType;
    }

    public String getUserJson() {
        return this.userJson;
    }
}
