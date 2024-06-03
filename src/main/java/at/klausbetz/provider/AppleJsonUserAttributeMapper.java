package at.klausbetz.provider;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

public class AppleJsonUserAttributeMapper extends AbstractJsonUserAttributeMapper {
    private static final String[] cp = new String[] {AppleIdentityProviderFactory.PROVIDER_ID};

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }

    @Override
    public String getId() {
        return "apple-json-user-attribute-mapper";
    }
}
