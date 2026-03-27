package at.klausbetz.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.IdentityProviderModel;

import static org.junit.jupiter.api.Assertions.*;

class AppleIdentityProviderConfigTest {

    private AppleIdentityProviderConfig config;

    @BeforeEach
    void setUp() {
        config = new AppleIdentityProviderConfig(new IdentityProviderModel());
    }

    @Test
    void givenTeamId_whenGetTeamId_thenReturnsTeamId() {
        config.setTeamId("ABCDE12345");
        assertEquals("ABCDE12345", config.getTeamId());
    }

    @Test
    void givenNoTeamId_whenGetTeamId_thenReturnsNull() {
        assertNull(config.getTeamId());
    }

    @Test
    void givenKeyId_whenGetKeyId_thenReturnsKeyId() {
        config.setKeyId("KEY1234567");
        assertEquals("KEY1234567", config.getKeyId());
    }

    @Test
    void givenNoKeyId_whenGetKeyId_thenReturnsNull() {
        assertNull(config.getKeyId());
    }

    @Test
    void givenTokenExchangeAccountLinkingEnabled_whenIsEnabled_thenReturnsTrue() {
        config.setTokenExchangeAccountLinkingEnabled(true);
        assertTrue(config.isTokenExchangeAccountLinkingEnabled());
    }

    @Test
    void givenTokenExchangeAccountLinkingDisabled_whenIsEnabled_thenReturnsFalse() {
        config.setTokenExchangeAccountLinkingEnabled(false);
        assertFalse(config.isTokenExchangeAccountLinkingEnabled());
    }

    @Test
    void givenNoTokenExchangeAccountLinkingSetting_whenIsEnabled_thenReturnsFalse() {
        assertFalse(config.isTokenExchangeAccountLinkingEnabled());
    }

    @Test
    void givenCustomDisplayName_whenGetDisplayName_thenReturnsCustomName() {
        config.setDisplayName("Apple Login");
        assertEquals("Apple Login", config.getDisplayName());
    }

    @Test
    void givenNoDisplayName_whenGetDisplayName_thenReturnsDefault() {
        assertEquals("Sign in with Apple", config.getDisplayName());
    }

    @Test
    void givenBlankDisplayName_whenGetDisplayName_thenReturnsDefault() {
        config.setDisplayName("   ");
        assertEquals("Sign in with Apple", config.getDisplayName());
    }

    @Test
    void whenIsDisableUserInfoService_thenReturnsTrue() {
        assertTrue(config.isDisableUserInfoService());
    }

    @Test
    void whenGetDisplayIconClasses_thenReturnsFaApple() {
        assertEquals("fa fa-apple", config.getDisplayIconClasses());
    }

    @Test
    void givenDefaultConstructor_whenCreated_thenConfigIsUsable() {
        AppleIdentityProviderConfig defaultConfig = new AppleIdentityProviderConfig();
        assertNotNull(defaultConfig);
        assertEquals("Sign in with Apple", defaultConfig.getDisplayName());
    }
}
