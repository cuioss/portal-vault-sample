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
package de.cuioss.portal.client.vault.server;

import com.bettercloud.vault.VaultException;

import de.cuioss.portal.client.vault.PortalVaultContext;
import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.VaultEndpoint;
import de.cuioss.portal.client.vault.util.VaultJsonHelper;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.uimodel.service.OptionalService;
import de.cuioss.uimodel.service.ServiceState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * @author Oliver Wolff
 *
 */
@ApplicationScoped
public class BackendHealthCheck implements OptionalService {

    static final String UNKNOWN = "Unknown";
    private static final String VAULT_VERSION_TEMPLATE = "Vault Server, version: %s";
    private static final CuiLogger log = new CuiLogger(BackendHealthCheck.class);

    @Inject
    @PortalVaultContext(VaultEndpoint.HEALTH)
    private Provider<VaultContext> contextProviderHealth;

    /**
     * @return An Instance of {@link ServerInfo} providing information derived from
     *         the vault health endpoint
     */
    public ServerInfo retrieveServerInfo() {
        var vaultContext = contextProviderHealth.get();
        if (!vaultContext.isEnabled()) {
            return ServerInfo.NOT_ENABLED;
        }
        var builder = ServerInfo.builder();
        builder.url(vaultContext.getUrl());
        try {
            var health = vaultContext.getVault().debug().health();
            var healthy = AttributeStatus.parse(500 != health.getRestResponse().getStatus());
            builder.healthy(healthy).initialized(AttributeStatus.parse(health.getInitialized()))
                    .unsealed(AttributeStatus.parse(health.getSealed()).negate())
                    .serverTimeUTC(health.getServerTimeUTC());
            builder.information(String.format(VAULT_VERSION_TEMPLATE,
                    VaultJsonHelper.getAsStringValue(health.getRestResponse(), "version").orElse(UNKNOWN)));
            if (AttributeStatus.TRUE.equals(healthy)) {
                builder.serviceState(ServiceState.ACTIVE);
            } else {
                builder.serviceState(ServiceState.TEMPORARILY_UNAVAILABLE);
            }
        } catch (VaultException e) {
            log.error(e, "Unable to access vault, due to");
            builder.serviceState(ServiceState.TEMPORARILY_UNAVAILABLE);
        }
        return builder.build();
    }

}
