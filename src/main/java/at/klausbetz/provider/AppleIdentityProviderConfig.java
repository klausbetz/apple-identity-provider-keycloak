package at.klausbetz.provider;

import org.keycloak.broker.oidc.OIDCIdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class AppleIdentityProviderConfig extends OIDCIdentityProviderConfig {

    private static final String TEAM_ID = "teamId";
    private static final String KEY_ID = "keyId";
    private static final String P8_CONTENT = "p8Content";
    private static final String DISPLAY_ICON_CLASSES = "fa fa-apple";
    private static final String DISPLAY_NAME = "displayName";
    private static final String DEFAULT_DISPLAY_NAME = "Sign in with Apple";

    public AppleIdentityProviderConfig(IdentityProviderModel identityProviderModel) {
        super(identityProviderModel);
    }

    public AppleIdentityProviderConfig() {
    }

    public String getTeamId() {
        return getConfig().get(TEAM_ID);
    }

    public void setTeamId(String teamId) {
        getConfig().put(TEAM_ID, teamId);
    }

    public String getKeyId() {
        return getConfig().get(KEY_ID);
    }

    public void setKeyId(String keyId) {
        getConfig().put(KEY_ID, keyId);
    }

    public String getP8Content() {
        return getConfig().get(P8_CONTENT);
    }

    public void setP8Content(String p8Content) {
        getConfig().put(P8_CONTENT, p8Content);
    }

    @Override
    public void setDisplayName(String displayName) {
        getConfig().put(DISPLAY_NAME, displayName);
    }

    @Override
    public String getDisplayName() {
        var displayName = getConfig().get(DISPLAY_NAME);
        if (displayName == null || displayName.isBlank()) {
            return DEFAULT_DISPLAY_NAME;
        }
        return displayName;
    }

    @Override
    public boolean isDisableUserInfoService() {
        return true;
    }

    @Override
    public String getDisplayIconClasses() {
        return DISPLAY_ICON_CLASSES;
    }
}