package at.klausbetz.provider.loginicon;

import org.keycloak.Config;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.LoginFormsProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class AppleFreeMarkerLoginFormsProviderFactory implements LoginFormsProviderFactory {

    @Override
    public LoginFormsProvider create(KeycloakSession session) {
        return new AppleFreeMarkerLoginFormsProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public String getId() {
        return "apple-icon-freemarker";
    }
}
