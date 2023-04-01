package de.cuioss.portal.client.vault.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.cuioss.portal.client.vault.EnableVaultTest;
import de.cuioss.portal.configuration.PortalConfigurationSource;
import de.cuioss.portal.configuration.VaultClientConfigKeys;
import de.cuioss.portal.core.test.mocks.configuration.PortalTestConfiguration;
import de.cuioss.test.valueobjects.junit5.contracts.ShouldBeNotNull;
import de.cuioss.uimodel.service.ServiceState;
import lombok.Getter;

@EnableVaultTest
class BackendHealthCheckTest implements ShouldBeNotNull<BackendHealthCheck> {

    @Inject
    @Getter
    private BackendHealthCheck underTest;

    @Inject
    @PortalConfigurationSource
    private PortalTestConfiguration configuration;

    @Test
    void shouldHandleDisabledByConfiguration() {
        configuration.fireEvent(VaultClientConfigKeys.VAULT_CLIENT_ENABLED, "false");
        assertEquals(ServerInfo.NOT_ENABLED, underTest.retrieveServerInfo());
    }

    @Test
    void shouldFailWithInvalidUrl() {
        configuration.fireEvent(VaultClientConfigKeys.VAULT_CONNECTION_BASE + ".url", "http://NotThere");
        assertEquals(ServiceState.TEMPORARILY_UNAVAILABLE, underTest.retrieveServerInfo().getServiceState());
    }

}
