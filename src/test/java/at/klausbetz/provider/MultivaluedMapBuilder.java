package at.klausbetz.provider;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.OAuth2Constants;

import java.util.Collections;
import java.util.List;

public class MultivaluedMapBuilder {

    private final MultivaluedMap<String, String> params = new MultivaluedHashMap<>();

    public MultivaluedMapBuilder subjectToken(String subjectToken) {
        params.put(OAuth2Constants.SUBJECT_TOKEN, List.of(subjectToken));
        return this;
    }

    public MultivaluedMapBuilder subjectTokenType(String subjectTokenType) {
        params.put(OAuth2Constants.SUBJECT_TOKEN_TYPE, List.of(subjectTokenType));
        return this;
    }

    public MultivaluedMapBuilder userJson(String userJson) {
        params.put("user_profile", userJson != null ? List.of(userJson) : Collections.emptyList());
        return this;
    }

    public MultivaluedMapBuilder appIdentifier(String appIdentifier) {
        params.put("app_identifier", List.of(appIdentifier));
        return this;
    }

    public MultivaluedMap<String, String> build() {
        return params;
    }
}
