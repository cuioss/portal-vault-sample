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
