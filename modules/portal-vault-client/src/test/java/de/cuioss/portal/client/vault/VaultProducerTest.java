package de.cuioss.portal.client.vault;

import static de.cuioss.tools.string.MoreStrings.isEmpty;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

@EnableVaultTest
class VaultProducerTest {

    @Inject
    private Provider<VaultConfig> vaultConfigProvider;

    @Inject
    private Provider<Vault> vaultProvider;

    @Test
    void shouldProduceVaultConfig() {
        var underTest = vaultConfigProvider.get();
        assertNotNull(underTest);
        assertFalse(isEmpty(underTest.getToken()));
    }

    @Test
    void shouldProduceVault() {
        assertNotNull(vaultProvider.get());
    }
}
