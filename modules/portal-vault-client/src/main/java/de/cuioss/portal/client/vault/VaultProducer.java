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

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import de.cuioss.portal.configuration.connections.impl.ConnectionMetadata;
import de.cuioss.portal.configuration.types.ConfigAsConnectionMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_CLIENT_ENABLED;
import static de.cuioss.portal.client.vault.VaultClientConfigKeys.VAULT_ENDPOINT_KEY_VALUE;

/**
 * Produces Instance of {@link VaultConfig}
 *
 * @author Oliver Wolff
 */
@ApplicationScoped
class VaultProducer {

    @Inject
    @ConfigAsConnectionMetadata(baseName = VaultClientConfigKeys.VAULT_CONNECTION_BASE)
    private Provider<ConnectionMetadata> metadataProducer;

    @Inject
    @ConfigProperty(name = VAULT_CLIENT_ENABLED)
    @Getter
    private Provider<Boolean> enabled;

    @Inject
    @ConfigProperty(name = VAULT_ENDPOINT_KEY_VALUE)
    @Getter
    private Provider<String> keyValueEndpoint;

    @Produces
    @Dependent
    VaultConfig produceVaultConfig() {
        var meta = metadataProducer.get();
        var vaultConfig = new VaultConfig();
        vaultConfig.sslConfig(new SslConfig());
        vaultConfig.engineVersion(2);
        vaultConfig.address(meta.getServiceUrl()).token(meta.getTokenResolver().resolve());
        return vaultConfig;
    }

    @Produces
    @Dependent
    Vault produceVault() {
        return new Vault(produceVaultConfig());
    }

    @Produces
    @Dependent
    @PortalVaultContext(VaultEndpoint.KEY_VALUE)
    VaultContext produceVaultContextKV() {
        return VaultContext.builder().enabled(enabled.get()).endpointName(keyValueEndpoint.get())
                .vault(produceVault()).url(metadataProducer.get().getServiceUrl()).build();
    }

    /**
     * @return a {@link VaultContext} to be used in the context of Health checks.
     * Therefore, the token will be overridden with the value "unauthorized"
     */
    @Produces
    @Dependent
    @PortalVaultContext(VaultEndpoint.HEALTH)
    VaultContext produceVaultContextHealth() {
        var config = produceVaultConfig();
        config.token("unauthorized");
        return VaultContext.builder().enabled(enabled.get())
                .endpointName(VaultEndpoint.HEALTH.getDefaultValue()).vault(new Vault(config))
                .url(metadataProducer.get().getServiceUrl()).build();

    }
}
