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
