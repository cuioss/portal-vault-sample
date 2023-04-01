package de.cuioss.portal.client.vault.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import de.cuioss.portal.client.vault.EnableVaultTest;
import de.cuioss.portal.client.vault.EnabledIfVaultIsReachable;
import de.cuioss.portal.configuration.PortalConfigurationSource;
import de.cuioss.portal.core.test.mocks.configuration.PortalTestConfiguration;
import de.cuioss.test.valueobjects.junit5.contracts.ShouldBeNotNull;
import de.cuioss.tools.string.MoreStrings;
import de.cuioss.uimodel.service.ServiceState;
import lombok.Getter;

@EnableVaultTest
// Can be run if integration tests are working or locally
@EnabledIfVaultIsReachable(url = "http://127.0.0.1:8200")
class BackendHealthCheckIntegrationTest implements ShouldBeNotNull<BackendHealthCheck> {

    @Inject
    @Getter
    private BackendHealthCheck underTest;

    @Inject
    @PortalConfigurationSource
    private PortalTestConfiguration configuration;

    @Test
    void shouldAccessHealthCheck() {
        var serverInfo = underTest.retrieveServerInfo();
        assertEquals(ServiceState.ACTIVE, serverInfo.getServiceState());
        assertFalse(MoreStrings.isEmpty(serverInfo.getInformation()));
        assertFalse(serverInfo.getInformation().contains(BackendHealthCheck.UNKNOWN),
                serverInfo.getInformation() + " Must not contain the String " + BackendHealthCheck.UNKNOWN);
    }
}
