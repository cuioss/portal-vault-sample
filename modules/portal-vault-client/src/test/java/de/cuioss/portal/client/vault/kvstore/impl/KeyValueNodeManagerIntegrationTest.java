package de.cuioss.portal.client.vault.kvstore.impl;

import static de.cuioss.test.generator.Generators.bytes;
import static de.cuioss.test.generator.Generators.strings;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

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
import de.cuioss.portal.client.vault.kvstore.KVEntry;
import de.cuioss.portal.client.vault.kvstore.Navigator;
import de.cuioss.test.juli.junit5.EnableTestLogger;
import de.cuioss.tools.io.FileSystemLoader;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.uimodel.result.ResultErrorCodes;
import de.cuioss.uimodel.result.ResultObject;

@EnableVaultTest
@EnableTestLogger(debug = KeyValueNodeManagerIntegrationTest.class,
        trace = { KeyValueNavigator.class, KeyValueNodeManager.class })
// Can be run if integration tests are working or locally
@EnabledIfVaultIsReachable(url = "http://127.0.0.1:8200")
class KeyValueNodeManagerIntegrationTest {

    private static final int LARGE_ARRAY = 100000;
    private static final FileSystemLoader POM_LOADER = new FileSystemLoader("pom.xml");
    private static final String NOT_THERE = "not.there";
    private static final String NEW_PATH = "dynamicCreatedPath";
    private static final String PROPERTY_1 = "some.property.key";
    private static final String PROPERTY_2 = "some.other.property.key";

    private static final CuiLogger log = new CuiLogger(KeyValueNodeManagerIntegrationTest.class);

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
                // Delete all possible Contents
                log.debug("Cleaning element {}", element);
                var manager = navigator.getNodeManager().getResult();
                var entries = manager.read();
                if (entries.isValid()) {
                    entries.getResult()
                            .forEach(entry -> verifyDeletion(manager.delete(entry.getKey()), entry.getKey()));
                } else {
                    log.error("Unable to detect attributes for deletion, due to {}",
                            entries.getResultDetail().orElse(null));
                }

                verifyDeletion(element.delete(), element);
            }
        } else {
            log.error("Unable to detect elements for deletion, due to {}", list.getResultDetail().orElse(null));
        }
    }

    void verifyDeletion(ResultObject<Boolean> deleted, Object element) {
        if (!deleted.isValid() || !deleted.getResult().booleanValue()) {
            log.error("Unable to delete {}", element);
        } else {
            log.info("Deleted path {}", element);
        }
    }

    KeyValueNavigator rootNavigator() {
        return new KeyValueNavigator(vault.get(), "/");
    }

    KeyValueNavigator elementNavigator() {
        return new KeyValueNavigator(vault.get(), "/" + NEW_PATH);
    }

    KeyValueNodeManager elementManager() {
        return new KeyValueNodeManager(elementNavigator(), vault.get());
    }

    @Test
    void shouldListEmptyRead() {
        var underTest = elementManager();
        var allread = underTest.read();
        assertTrue(allread.isValid());
        assertTrue(allread.getResult().isEmpty());
    }

    @Test
    void shouldHandleNotExisitingProperty() {
        var underTest = elementManager();
        assertPropertyNotFound(underTest.read(NOT_THERE));
    }

    @Test
    void shouldRoundtripSingleProperty() {
        var underTest = elementManager();

        // Should not be there at the beginning
        assertPropertyNotFound(underTest.read(PROPERTY_1));
        var value = strings().next();
        var written = underTest.write(KVEntry.of(PROPERTY_1, value));
        assertTrue(written.isValid());
        assertEquals(value, written.getResult().getValueAsString().get());

        // read should result in the same object
        var read = underTest.read(PROPERTY_1);
        assertTrue(read.isValid());
        assertEquals(written.getResult().getValue(), read.getResult().getValue());

        // Should be the same result as for the read all operation
        var allread = underTest.read();
        assertTrue(allread.isValid());
        assertEquals(1, allread.getResult().size());
        assertEquals(written.getResult().getValue(), allread.getResult().iterator().next().getValue());

        // Now should be deleted successfully
        assertPropertyDeleted(underTest.delete(PROPERTY_1));

        // VerifyDeletion by read
        assertPropertyNotFound(underTest.read(PROPERTY_1));
    }

    @Test
    void shouldHandleMultipleProperties() {
        var underTest = elementManager();

        // Should not be there at the beginning
        assertPropertyNotFound(underTest.read(PROPERTY_1));
        assertPropertyNotFound(underTest.read(PROPERTY_2));
        var value = strings().next();
        var value2 = strings().next();
        var written = underTest.write(KVEntry.of(PROPERTY_1, value));
        assertTrue(written.isValid());
        assertEquals(value, written.getResult().getValueAsString().get());

        written = underTest.write(KVEntry.of(PROPERTY_2, value2));
        assertTrue(written.isValid());
        assertEquals(value2, written.getResult().getValueAsString().get());

        // read should result in the same object
        var read = underTest.read();
        assertTrue(read.isValid());
        assertEquals(2, read.getResult().size());
    }

    void assertPropertyDeleted(ResultObject<Boolean> deleted) {
        assertTrue(deleted.isValid());
        assertTrue(deleted.getResult().booleanValue());
    }

    static void assertPropertyNotFound(ResultObject<?> resultObject) {
        assertFalse(resultObject.isValid(), "ResultObject is supposed not to be valid");
        assertTrue(resultObject.containsErrorCode(ResultErrorCodes.NOT_FOUND),
                "ResultObject is expected to contain ResultErrorCodes.NOT_FOUND");
    }

    @Test
    void shouldHandleDelete() {
        var underTest = elementManager();

        // Should not be there at the beginning
        assertPropertyNotFound(underTest.read(PROPERTY_1));
        var deleted = underTest.delete(PROPERTY_1);
        assertFalse(deleted.isValid());
        assertFalse(deleted.getResult().booleanValue());

        underTest.write(KVEntry.of(PROPERTY_1, strings().next()));
        assertTrue(underTest.read(PROPERTY_1).isValid());

        assertPropertyDeleted(underTest.delete(PROPERTY_1));
        assertPropertyNotFound(underTest.read(PROPERTY_1));
    }

    @Test
    void shouldHandleBinaryData() throws IOException {
        var underTest = elementManager();

        // Should not be there at the beginning
        assertPropertyNotFound(underTest.read(PROPERTY_1));
        var written = underTest.write(KVEntry.of(PROPERTY_1, POM_LOADER));
        assertTrue(written.isValid());

        var pomContent = KVEntry.toByteArray(POM_LOADER.inputStream());
        assertArrayEquals(pomContent, (byte[]) written.getResult().getValue());
    }

    @Test
    void shouldHandleLargeBinaryData() {
        var underTest = elementManager();

        var generator = bytes();
        var large = new byte[LARGE_ARRAY];
        for (var i = 0; i < large.length; i++) {
            large[i] = generator.next();
        }

        var written = underTest.write(KVEntry.of(PROPERTY_1, large));
        assertTrue(written.isValid());

        assertArrayEquals(large, (byte[]) written.getResult().getValue());
    }

}
