package at.klausbetz.provider;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

public class AppleIdentityProviderFactory extends AbstractIdentityProviderFactory<AppleIdentityProvider> implements SocialIdentityProviderFactory<AppleIdentityProvider> {

    public static final String PROVIDER_ID = "apple";

    @Override
    public String getName() {
        return "Apple";
    }

    @Override
    public AppleIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new AppleIdentityProvider(session, new AppleIdentityProviderConfig(model));
    }

    @Override
    public AppleIdentityProviderConfig createConfig() {
        return new AppleIdentityProviderConfig();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}