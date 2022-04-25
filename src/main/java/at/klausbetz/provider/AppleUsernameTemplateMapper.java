package at.klausbetz.provider;

import org.keycloak.broker.oidc.mappers.UsernameTemplateMapper;

public class AppleUsernameTemplateMapper extends UsernameTemplateMapper {
    private static final String[] cp = new String[] {AppleIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "apple-username-template-mapper";
    }
}
