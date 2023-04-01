package de.cuioss.portal.client.vault.server;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Provider;

import com.bettercloud.vault.VaultException;

import de.cuioss.portal.client.vault.PortalVaultContext;
import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.VaultEndpoint;
import de.cuioss.portal.client.vault.util.VaultJsonHelper;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.uimodel.service.OptionalService;
import de.cuioss.uimodel.service.ServiceState;

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
     * @return An Instance of {@link ServerInfo} providing information derived from the vault health
     *         endpoint
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
            builder.healthy(healthy)
                    .initialized(AttributeStatus.parse(health.getInitialized()))
                    .unsealed(AttributeStatus.parse(health.getSealed()).negate())
                    .serverTimeUTC(health.getServerTimeUTC());
            builder.information(
                    String.format(VAULT_VERSION_TEMPLATE,
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
