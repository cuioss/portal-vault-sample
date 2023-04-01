package de.cuioss.portal.client.vault.config;

import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CONNECTION_BASE;
import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_DEFAULT_CONFIG_LOCATION;
import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.cuioss.portal.configuration.FileConfigurationSource;
import de.cuioss.portal.configuration.VaultClientConfigKeys;
import de.cuioss.portal.configuration.impl.source.LoaderUtils;
import de.cuioss.portal.configuration.yaml.YamlDefaultConfigSource;
import de.cuioss.portal.core.test.tests.configuration.AbstractConfigurationKeyVerifierTest;
import lombok.Getter;

class DefaultConfigTest extends AbstractConfigurationKeyVerifierTest {

    @Getter
    private final FileConfigurationSource underTest = new YamlDefaultConfigSource();

    @Test
    void shouldProvideDefaultConfiguration() {
        final var source = LoaderUtils.loadConfigurationFromSource(underTest);
        assertEquals("vault-client", source.get(VAULT_CONNECTION_BASE + ".id"));
    }

    @Override
    public List<String> getKeysIgnoreList() {
        return immutableList(VAULT_DEFAULT_CONFIG_LOCATION, VAULT_CONNECTION_BASE);
    }

    @Override
    public List<String> getConfigurationKeysIgnoreList() {
        return immutableList(VAULT_CONNECTION_BASE);
    }

    @Override
    public Class<?> getKeyHolder() {
        return VaultClientConfigKeys.class;
    }

}
