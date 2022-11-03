package at.klausbetz.provider.loginicon;

import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.Theme;

import javax.ws.rs.core.UriBuilder;
import java.util.Locale;
import java.util.Properties;

public class AppleFreeMarkerLoginFormsProvider extends FreeMarkerLoginFormsProvider implements LoginFormsProvider {

    private static final String SOCIAL_MAP_KEY = "social";

    public AppleFreeMarkerLoginFormsProvider(KeycloakSession session) {
        super(session);
    }

    @Override
    protected void createCommonAttributes(Theme theme, Locale locale, Properties messagesBundle, UriBuilder baseUriBuilder, LoginFormsPages page) {
        super.createCommonAttributes(theme, locale, messagesBundle, baseUriBuilder, page);

        if (attributes.containsKey(SOCIAL_MAP_KEY) && attributes.get(SOCIAL_MAP_KEY) instanceof IdentityProviderBean) {
            var identityProviderBean = (IdentityProviderBean) attributes.get(SOCIAL_MAP_KEY);
            var newIdentityProviderBean = new AppleFreeMarkerIdentityProviderBean(identityProviderBean, realm);
            attributes.remove(SOCIAL_MAP_KEY);
            attributes.put(SOCIAL_MAP_KEY, newIdentityProviderBean);
        }
    }
}
