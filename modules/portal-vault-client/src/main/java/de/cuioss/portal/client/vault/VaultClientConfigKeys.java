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

import de.cuioss.portal.configuration.PortalConfigurationKeys;
import lombok.experimental.UtilityClass;

/**
 * Defines the configuration keys for using within vault-client
 *
 * @author Oliver Wolff
 *
 */
@UtilityClass
public class VaultClientConfigKeys {

    /**
     * Location of the property file for the default-configuration
     */
    public static final String VAULT_DEFAULT_CONFIG_LOCATION = "classpath:/META-INF/vault_client_default_configuration.yml";

    private static final String VAULT_BASE = PortalConfigurationKeys.INTEGRATION_BASE + "vault.";
    private static final String VAULT_ENDPOINT_BASE = VAULT_BASE + "endpoint.";

    /** Base name for identifying a vault-connection. */
    public static final String VAULT_CONNECTION_BASE = VAULT_BASE + "connection";

    /** Base name for identifying a vault-connection. */
    public static final String VAULT_CLIENT_ENABLED = VAULT_BASE + "enabled";

    /** Default name for the Key-Value endpoint, default value is 'secret'. */
    public static final String VAULT_ENDPOINT_KEY_VALUE = VAULT_ENDPOINT_BASE + "key_value";

}
