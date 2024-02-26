package at.klausbetz.provider;

import org.keycloak.broker.oidc.mappers.UserAttributeMapper;

public class AppleUserAttributeMapper extends UserAttributeMapper {

    private static final String[] cp = new String[] { AppleIdentityProviderFactory.PROVIDER_ID };

    @Override
    public String[] getCompatibleProviders() {
        return cp;
    }


    @Override
    public String getId() {
        return "apple-user-attribute-mapper";
    }
}
