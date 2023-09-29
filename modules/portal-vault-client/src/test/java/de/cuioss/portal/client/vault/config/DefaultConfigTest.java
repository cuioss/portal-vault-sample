/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cuioss.portal.client.vault.config;

import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_CONNECTION_BASE;
import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_DEFAULT_CONFIG_LOCATION;
import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.cuioss.portal.client.vault.VaultClientConfigKeys;
import de.cuioss.portal.configuration.FileConfigurationSource;
import de.cuioss.portal.configuration.impl.source.LoaderUtils;
import de.cuioss.portal.configuration.source.PropertiesConfigSource;
import de.cuioss.portal.core.test.tests.configuration.AbstractConfigurationKeyVerifierTest;
import lombok.Getter;

class DefaultConfigTest extends AbstractConfigurationKeyVerifierTest {

    @Getter
    private final FileConfigurationSource underTest = new PropertiesConfigSource(
            "classpath:/META-INF/microprofile-config.properties");

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
