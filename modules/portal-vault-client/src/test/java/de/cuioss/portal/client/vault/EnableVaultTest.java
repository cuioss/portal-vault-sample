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

import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CLIENT_ENABLED;
import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CONNECTION_BASE;
import static de.cuioss.portal.configuration.connections.impl.ConnectionMetadataKeys.AUTH_TOKEN_APPLICATION_TOKEN;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;

import de.cuioss.portal.configuration.impl.producer.ConnectionMetadataProducer;
import de.cuioss.portal.core.test.junit5.EnablePortalConfiguration;

/**
 * Using this annotations at type-level of a junit 5 test provides the basic
 * types for handling configuration of the portal and the Vault default
 * configuration
 *
 * @author Oliver Wolff
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@EnableAutoWeld
@EnablePortalConfiguration(configuration = {
        VAULT_CONNECTION_BASE + "." + AUTH_TOKEN_APPLICATION_TOKEN + ":s.77uZJFAV0HSdWrID1AeOBKdP",
        VAULT_CLIENT_ENABLED + ":true" })
@AddBeanClasses({ VaultProducer.class, ConnectionMetadataProducer.class, VaultContext.class })
public @interface EnableVaultTest {

}
