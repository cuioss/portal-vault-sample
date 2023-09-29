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
package de.cuioss.portal.client.vault.kvstore.impl;

import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.Test;

import de.cuioss.portal.client.vault.EnableVaultTest;
import de.cuioss.portal.client.vault.PortalVaultContext;
import de.cuioss.portal.client.vault.VaultClientConfigKeys;
import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.VaultEndpoint;
import de.cuioss.portal.client.vault.kvstore.KVEntry;
import de.cuioss.portal.configuration.PortalConfigurationSource;
import de.cuioss.portal.core.test.mocks.configuration.PortalTestConfiguration;
import de.cuioss.test.valueobjects.junit5.contracts.ShouldBeNotNull;

@EnableVaultTest
class KeyValueNodeManagerTest implements ShouldBeNotNull<KeyValueNodeManager> {

    @Inject
    @PortalConfigurationSource
    private PortalTestConfiguration configuration;

    @Inject
    @PortalVaultContext(VaultEndpoint.KEY_VALUE)
    private Provider<VaultContext> vault;

    public KeyValueNavigator getNavigator() {
        return new KeyValueNavigator(vault.get(), "/secret");
    }

    @Override
    public KeyValueNodeManager getUnderTest() {
        return new KeyValueNodeManager(getNavigator(), vault.get());
    }

    @Test
    void shouldHandleDisabled() {
        configuration.fireEvent(VaultClientConfigKeys.VAULT_CLIENT_ENABLED, "false");
        var underTest = getUnderTest();
        assertFalse(underTest.read().isValid());
        assertFalse(underTest.write(immutableList(KVEntry.EMPTY)).isValid());
        assertFalse(underTest.delete("test").isValid());
    }

}
