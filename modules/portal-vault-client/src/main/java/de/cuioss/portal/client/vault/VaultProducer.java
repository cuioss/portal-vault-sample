package de.cuioss.portal.client.vault;

import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CLIENT_ENABLED;
import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_ENDPOINT_KEY_VALUE;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

import de.cuioss.portal.configuration.VaultClientConfigKeys;
import de.cuioss.portal.configuration.connections.impl.ConnectionMetadata;
import de.cuioss.portal.configuration.types.ConfigAsConnectionMetadata;
import lombok.Getter;

/**
 * Produces Instance of {@link VaultConfig}
 *
 * @author Oliver Wolff
 *
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
    private Provider<String> keyVauleEndpoint;

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
        return VaultContext.builder().enabled(enabled.get().booleanValue()).endpointName(keyVauleEndpoint.get())
                .vault(produceVault()).url(metadataProducer.get().getServiceUrl()).build();
    }

    /**
     * @return a {@link VaultContext} to be used in the context of Healthchecks. Therefore the token
     *         will be overridden with the value "unauthorized"
     */
    @Produces
    @Dependent
    @PortalVaultContext(VaultEndpoint.HEALTH)
    VaultContext produceVaultContextHealth() {
        var config = produceVaultConfig();
        config.token("unauthorized");
        return VaultContext.builder().enabled(enabled.get().booleanValue())
                .endpointName(VaultEndpoint.HEALTH.getDefaultValue())
                .vault(new Vault(config)).url(metadataProducer.get().getServiceUrl()).build();

    }
}
