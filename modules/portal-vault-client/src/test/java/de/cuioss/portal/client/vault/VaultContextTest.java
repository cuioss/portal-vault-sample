package de.cuioss.portal.client.vault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;

@EnableVaultTest
class VaultContextTest {

    @Inject
    @PortalVaultContext(VaultEndpoint.KEY_VALUE)
    private Provider<VaultContext> contextProviderKV;

    @Inject
    @PortalVaultContext(VaultEndpoint.HEALTH)
    private Provider<VaultContext> contextProviderHealth;

    @Test
    void shouldBeEnabledByAnnotationConfiguration() {
        assertTrue(contextProviderKV.get().isEnabled());
    }

    @Test
    void shouldProduceHealthProvider() {
        assertNotNull(contextProviderHealth.get());
    }

    @Test
    void shouldStripEndpointName() {
        var underTest = contextProviderKV.get();
        assertEquals("/", underTest.stripEndpointName("/secret"));
        assertEquals("/", underTest.stripEndpointName("/secret/"));
        assertEquals("/a", underTest.stripEndpointName("/secret/a"));
        assertEquals("/a/b", underTest.stripEndpointName("/secret/a/b"));

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.stripEndpointName("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            underTest.stripEndpointName("/nosecret");
        });
    }

    @Test
    void shouldAppendEndpointName() {
        var underTest = contextProviderKV.get();
        assertEquals("/secret/", underTest.appendToEndpointName("/"));
        assertEquals("/secret/a", underTest.appendToEndpointName("/a"));
        assertEquals("/secret/a/b", underTest.appendToEndpointName("/a/b"));
    }
}
