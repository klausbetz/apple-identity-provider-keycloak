package at.klausbetz.provider;

import org.keycloak.broker.oidc.mappers.ClaimToUserSessionNoteMapper;

public class AppleUserSessionNoteMapper extends ClaimToUserSessionNoteMapper {
    private static final String[] cp = new String[] {AppleIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "apple-claim-user-session-note-mapper";
    }
}
