package at.klausbetz.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppleMapperTest {

    // ========== AppleUsernameTemplateMapper ==========

    @Test
    void usernameTemplateMapper_getCompatibleProviders_returnsApple() {
        AppleUsernameTemplateMapper mapper = new AppleUsernameTemplateMapper();
        String[] providers = mapper.getCompatibleProviders();
        assertArrayEquals(new String[]{"apple"}, providers);
    }

    @Test
    void usernameTemplateMapper_getId_returnsExpectedId() {
        AppleUsernameTemplateMapper mapper = new AppleUsernameTemplateMapper();
        assertEquals("apple-username-template-mapper", mapper.getId());
    }

    // ========== AppleUserAttributeMapper ==========

    @Test
    void userAttributeMapper_getCompatibleProviders_returnsApple() {
        AppleUserAttributeMapper mapper = new AppleUserAttributeMapper();
        String[] providers = mapper.getCompatibleProviders();
        assertArrayEquals(new String[]{"apple"}, providers);
    }

    @Test
    void userAttributeMapper_getId_returnsExpectedId() {
        AppleUserAttributeMapper mapper = new AppleUserAttributeMapper();
        assertEquals("apple-user-attribute-mapper", mapper.getId());
    }

    // ========== AppleJsonUserAttributeMapper ==========

    @Test
    void jsonUserAttributeMapper_getCompatibleProviders_returnsApple() {
        AppleJsonUserAttributeMapper mapper = new AppleJsonUserAttributeMapper();
        String[] providers = mapper.getCompatibleProviders();
        assertArrayEquals(new String[]{"apple"}, providers);
    }

    @Test
    void jsonUserAttributeMapper_getId_returnsExpectedId() {
        AppleJsonUserAttributeMapper mapper = new AppleJsonUserAttributeMapper();
        assertEquals("apple-json-user-attribute-mapper", mapper.getId());
    }

    // ========== AppleUserSessionNoteMapper ==========

    @Test
    void userSessionNoteMapper_getCompatibleProviders_returnsApple() {
        AppleUserSessionNoteMapper mapper = new AppleUserSessionNoteMapper();
        String[] providers = mapper.getCompatibleProviders();
        assertArrayEquals(new String[]{"apple"}, providers);
    }

    @Test
    void userSessionNoteMapper_getId_returnsExpectedId() {
        AppleUserSessionNoteMapper mapper = new AppleUserSessionNoteMapper();
        assertEquals("apple-claim-user-session-note-mapper", mapper.getId());
    }
}
