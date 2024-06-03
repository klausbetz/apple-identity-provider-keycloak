package at.klausbetz.provider;

import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

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

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                                           .property().name("displayName").label("Display name").helpText("Text that is shown on the login page. Defaults to 'Sign in with Apple'").type(ProviderConfigProperty.STRING_TYPE).add()
                                           .property().name("teamId").label("Team ID").helpText("Your 10-character Team ID obtained from your Apple developer account.").type(ProviderConfigProperty.STRING_TYPE).add()
                                           .property().name("keyId").label("Key ID").helpText("A 10-character key identifier obtained from your Apple developer account.").type(ProviderConfigProperty.STRING_TYPE).add()
                                           .property().name("p8Content").label("p8 Key").helpText("Raw content of Apple's p8 key file. Example (without quotes): \"-----BEGIN PRIVATE KEY-----!CONTENT!-----END PRIVATE KEY-----\" (may contain line-breaks '\\\\n' as well).").type(ProviderConfigProperty.PASSWORD).secret(true)
                                           .add().build();
    }
}