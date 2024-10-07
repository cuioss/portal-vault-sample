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

import de.cuioss.portal.client.vault.*;
import de.cuioss.portal.core.test.mocks.configuration.PortalTestConfiguration;
import de.cuioss.test.juli.junit5.EnableTestLogger;
import de.cuioss.test.valueobjects.junit5.contracts.ShouldBeNotNull;
import de.cuioss.uimodel.service.ServiceState;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnableVaultTest
@EnableTestLogger(debug = KeyValueNavigatorTest.class)
class KeyValueNavigatorTest implements ShouldBeNotNull<KeyValueNavigator> {

    static final String FIRST_ELEMENT = "hello";
    static final String FIRST_ELEMENT_PATH = "/" + FIRST_ELEMENT;
    static final String SECOND_ELEMENT = "world";
    static final String NESTED_PATH = FIRST_ELEMENT_PATH + "/" + SECOND_ELEMENT;

    @Inject
    private PortalTestConfiguration configuration;

    @Inject
    @PortalVaultContext(VaultEndpoint.KEY_VALUE)
    private Provider<VaultContext> vault;

    @Override
    public KeyValueNavigator getUnderTest() {
        return new KeyValueNavigator(vault.get(), "/secret");
    }

    @Test
    void shouldReturnParentOnParent() {
        var underTest = new KeyValueNavigator(vault.get(), "/");
        assertEquals(underTest, underTest.getParent());
    }

    @Test
    void shouldReturnParentForChild() {
        var underTest = new KeyValueNavigator(vault.get(), NESTED_PATH);
        assertEquals(NESTED_PATH, underTest.getPath());
        assertEquals(SECOND_ELEMENT, underTest.getContext());
        var parent = underTest.getParent();
        assertEquals(FIRST_ELEMENT, parent.getContext());
        assertEquals(FIRST_ELEMENT_PATH, parent.getPath());
        // Root-Element
        assertEquals("/", parent.getParent().getPath());
        assertEquals("/", parent.getParent().getContext());
    }

    @Test
    void shouldHandleDisabled() {
        configuration.update(VaultClientConfigKeys.VAULT_CLIENT_ENABLED, "false");
        var underTest = getUnderTest();
        assertEquals(ServiceState.NOT_CONFIGURED, underTest.getServiceState());
        assertFalse(underTest.list().isValid());
        assertFalse(underTest.list("/").isValid());
        assertFalse(underTest.create("/new").isValid());
        assertFalse(underTest.getNodeManager().isValid());
        assertFalse(underTest.delete().isValid());
    }

}
