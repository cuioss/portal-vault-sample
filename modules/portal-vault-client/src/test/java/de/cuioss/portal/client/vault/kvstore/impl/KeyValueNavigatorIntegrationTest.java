package de.cuioss.portal.client.vault.kvstore.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.cuioss.portal.client.vault.EnableVaultTest;
import de.cuioss.portal.client.vault.EnabledIfVaultIsReachable;
import de.cuioss.portal.client.vault.PortalVaultContext;
import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.VaultEndpoint;
import de.cuioss.portal.client.vault.kvstore.Navigator;
import de.cuioss.test.juli.junit5.EnableTestLogger;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.uimodel.result.ResultErrorCodes;

@EnableVaultTest
@EnableTestLogger(debug = KeyValueNavigatorIntegrationTest.class)
// Can be run if integration tests are working or locally
@EnabledIfVaultIsReachable(url = "http://127.0.0.1:8200")
class KeyValueNavigatorIntegrationTest {

    private static final String PATH_NOT_THERE = "pathNotThere";
    private static final String NEW_PATH = "dynamicCreatedPath";
    private static final String NEW_PATH_2 = "dynamicCreatedPath2";

    private static final CuiLogger log = new CuiLogger(KeyValueNavigatorIntegrationTest.class);

    @Inject
    @PortalVaultContext(VaultEndpoint.KEY_VALUE)
    private Provider<VaultContext> vault;

    @BeforeEach
    @AfterEach
    void deleteAllPaths() {
        var navigator = rootNavigator();
        var list = navigator.list();
        if (list.isValid()) {
            for (Navigator element : list.getResult()) {
                var deleted = element.delete();
                if (!deleted.isValid() || !deleted.getResult().booleanValue()) {
                    log.error("Unable to delete {}", element);
                } else {
                    log.info("Deleted path {}", element);
                }
            }
        } else {
            log.error("Unable to detect elements for deletion, due to {}", list.getResultDetail().orElse(null));
        }
    }

    KeyValueNavigator rootNavigator() {
        return new KeyValueNavigator(vault.get(), "/");
    }

    @Test
    void shouldListEmptyRoot() {
        var navigator = rootNavigator();
        var list = navigator.list();
        assertTrue(list.isValid());
        assertTrue(list.getResult().isEmpty());
    }

    @Test
    void shouldListRootWithTwoElements() {
        var navigator = rootNavigator();
        navigator.create(NEW_PATH);
        navigator.create(NEW_PATH_2);
        var list = navigator.list();
        assertTrue(list.isValid());
        assertEquals(2, list.getResult().size());

    }

    @Test
    void shouldFailToListNonExisiting() {
        var navigator = rootNavigator();
        var listed = navigator.list(PATH_NOT_THERE);
        assertFalse(listed.isValid());
        assertTrue(listed.containsErrorCode(ResultErrorCodes.NOT_FOUND));
    }

    @Test
    void shouldCreateNode() {
        var navigator = rootNavigator();
        assertFalse(navigator.pathExists(NEW_PATH));
        var created = navigator.create(NEW_PATH);
        assertTrue(created.isValid(), String.valueOf(created.getResultDetail().orElse(null)));
        log.info(created.getResult().toString());
        assertTrue(navigator.pathExists(NEW_PATH));
    }

    @Test
    void shouldHandleNonRootNode() {
        var navigator = rootNavigator();
        var created = navigator.create(NEW_PATH);
        assertTrue(created.isValid(), String.valueOf(created.getResultDetail().orElse(null)));
        var child = created.getResult();
        var grandChildren = child.list();
        assertTrue(grandChildren.isValid(), String.valueOf(created.getResultDetail().orElse(null)));
        assertTrue(grandChildren.getResult().isEmpty());
    }

    @Test
    void shouldNotDeleteNonExisting() {
        var navigator = new KeyValueNavigator(vault.get(), "/" + PATH_NOT_THERE);
        assertFalse(navigator.pathExists("/"));
        var deleteRequest = navigator.delete();
        assertFalse(deleteRequest.isValid());
        assertTrue(deleteRequest.containsErrorCode(ResultErrorCodes.NOT_FOUND));
        assertFalse(deleteRequest.getResult());
    }

    @Test
    void shouldDeleteExisting() {
        var navigator = rootNavigator();
        var created = navigator.create(NEW_PATH);
        assertTrue(created.isValid(), String.valueOf(created.getResultDetail().orElse(null)));

        var deleteRequest = created.getResult().delete();
        assertTrue(deleteRequest.isValid(), String.valueOf(deleteRequest.getResultDetail().orElse(null)));
        assertFalse(deleteRequest.containsErrorCode(ResultErrorCodes.NOT_FOUND));
        assertTrue(deleteRequest.getResult());
    }

}
