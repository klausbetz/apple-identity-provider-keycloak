package at.klausbetz.provider;

import org.keycloak.theme.ClasspathThemeResourceProviderFactory;

/**
 * Remove after V19.0.2
 */
public class DummyResourceProviderFactory extends ClasspathThemeResourceProviderFactory {

    @Override
    public String getId() {
        return "apple-dummy-theme-provider";
    }
}
