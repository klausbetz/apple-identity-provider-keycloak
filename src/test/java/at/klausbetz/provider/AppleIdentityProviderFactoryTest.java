package at.klausbetz.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AppleIdentityProviderFactoryTest {

    @Mock(lenient = true)
    private KeycloakSession session;

    private AppleIdentityProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new AppleIdentityProviderFactory();
    }

    @Test
    void whenGetName_thenReturnsApple() {
        assertEquals("Apple", factory.getName());
    }

    @Test
    void whenGetId_thenReturnsApple() {
        assertEquals("apple", factory.getId());
    }

    @Test
    void whenCreateConfig_thenReturnsAppleIdentityProviderConfig() {
        AppleIdentityProviderConfig config = factory.createConfig();
        assertNotNull(config);
        assertInstanceOf(AppleIdentityProviderConfig.class, config);
    }

    @Test
    void givenSessionAndModel_whenCreate_thenReturnsAppleIdentityProvider() {
        IdentityProviderModel model = new IdentityProviderModel();
        model.setProviderId("apple");
        AppleIdentityProvider provider = factory.create(session, model);
        assertNotNull(provider);
    }

    @Test
    void whenGetConfigProperties_thenContainsExpectedProperties() {
        List<ProviderConfigProperty> properties = factory.getConfigProperties();
        assertNotNull(properties);
        assertFalse(properties.isEmpty());

        List<String> propertyNames = properties.stream().map(ProviderConfigProperty::getName).toList();
        assertTrue(propertyNames.contains("displayName"));
        assertTrue(propertyNames.contains("teamId"));
        assertTrue(propertyNames.contains("keyId"));
        assertTrue(propertyNames.contains("tokenExchangeAccountLinkingEnabled"));
    }

    @Test
    void whenGetConfigProperties_thenTeamIdHasCorrectType() {
        List<ProviderConfigProperty> properties = factory.getConfigProperties();
        ProviderConfigProperty teamId = properties.stream()
                .filter(p -> "teamId".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(ProviderConfigProperty.STRING_TYPE, teamId.getType());
        assertNotNull(teamId.getLabel());
        assertNotNull(teamId.getHelpText());
    }

    @Test
    void whenGetConfigProperties_thenTokenExchangeAccountLinkingHasBooleanType() {
        List<ProviderConfigProperty> properties = factory.getConfigProperties();
        ProviderConfigProperty tokenExchange = properties.stream()
                .filter(p -> "tokenExchangeAccountLinkingEnabled".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(ProviderConfigProperty.BOOLEAN_TYPE, tokenExchange.getType());
    }

    @Test
    void whenProviderIdConstant_thenEqualsApple() {
        assertEquals("apple", AppleIdentityProviderFactory.PROVIDER_ID);
    }
}
