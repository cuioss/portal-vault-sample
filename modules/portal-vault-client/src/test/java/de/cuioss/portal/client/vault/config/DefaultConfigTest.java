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

import de.cuioss.portal.client.vault.VaultClientConfigKeys;
import de.cuioss.portal.configuration.util.ConfigurationHelper;
import de.cuioss.portal.core.test.tests.configuration.AbstractConfigurationKeyVerifierTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_CONNECTION_BASE;
import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_DEFAULT_CONFIG_LOCATION;
import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultConfigTest extends AbstractConfigurationKeyVerifierTest {


    @Test
    void shouldProvideDefaultConfiguration() {
        assertEquals("vault-client", ConfigurationHelper.resolveConfigPropertyOrThrow(VAULT_CONNECTION_BASE + ".id"));
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

    @Override
    public String getConfigSourceName() {
        return "portal-vault-client";
    }

}
