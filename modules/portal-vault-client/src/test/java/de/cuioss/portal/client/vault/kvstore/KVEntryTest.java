package de.cuioss.portal.client.vault.kvstore;

import static de.cuioss.test.generator.Generators.booleans;
import static de.cuioss.test.generator.Generators.doubles;
import static de.cuioss.test.generator.Generators.floats;
import static de.cuioss.test.generator.Generators.integers;
import static de.cuioss.test.generator.Generators.letterStrings;
import static de.cuioss.test.generator.Generators.nonEmptyStrings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Test;

import de.cuioss.test.valueobjects.ValueObjectTest;
import de.cuioss.test.valueobjects.api.contracts.VerifyBuilder;
import de.cuioss.test.valueobjects.api.object.ObjectTestConfig;
import de.cuioss.test.valueobjects.api.property.PropertyConfig;
import de.cuioss.test.valueobjects.api.property.PropertyReflectionConfig;
import de.cuioss.tools.io.FileLoader;
import de.cuioss.tools.io.FileSystemLoader;
import de.cuioss.tools.io.IOStreams;

@PropertyReflectionConfig(skip = true)
@ObjectTestConfig(equalsAndHashCodeBasicOnly = true)
@PropertyConfig(name = "key", propertyClass = String.class, required = true)
@PropertyConfig(name = "value", propertyClass = Serializable.class, required = false)
@PropertyConfig(name = "metadata", propertyClass = Metadata.class, required = true)
@VerifyBuilder
class KVEntryTest extends ValueObjectTest<KVEntry> {

    private static final String KEY = "key";

    @Test
    void shouldTranslateToString() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsString().isPresent());
        var test = nonEmptyStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertTrue(entry.getValueAsString().isPresent());
        assertEquals(test, entry.getValueAsString().get());
    }

    @Test
    void shouldTranslateToBoolean() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsBoolean().isPresent());
        var test = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertTrue(entry.getValueAsBoolean().isPresent());
        // usually false if not recognized
        assertFalse(entry.getValueAsBoolean().get());

        var test2 = booleans().next();
        var entry2 = KVEntry.of(KEY, test2);
        assertTrue(entry2.getValueAsBoolean().isPresent());
        assertEquals(test2, entry2.getValueAsBoolean().get());
    }

    @Test
    void shouldTranslateToInteger() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsInteger().isPresent());
        var test = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertFalse(entry.getValueAsInteger().isPresent());

        var test2 = integers().next();
        var entry2 = KVEntry.of(KEY, test2);
        assertTrue(entry2.getValueAsInteger().isPresent());
        assertEquals(test2, entry2.getValueAsInteger().get());
    }

    @Test
    void shouldTranslateToDouble() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsDouble().isPresent());
        var test = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertFalse(entry.getValueAsDouble().isPresent());

        var test2 = doubles().next();
        var entry2 = KVEntry.of(KEY, test2);
        assertTrue(entry2.getValueAsDouble().isPresent());
        assertEquals(test2, entry2.getValueAsDouble().get());
    }

    @Test
    void shouldTranslateToFloat() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsFloat().isPresent());
        var test = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertFalse(entry.getValueAsFloat().isPresent());

        var test2 = floats().next();
        var entry2 = KVEntry.of(KEY, test2);
        assertTrue(entry2.getValueAsFloat().isPresent());
        assertEquals(test2, entry2.getValueAsFloat().get());
    }

    @Test
    void shouldTranslateToInputStream() {
        assertFalse(KVEntry.of(KEY, (String) null).getValueAsInputStream().isPresent());
        var test = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        assertTrue(entry.getValueAsInputStream().isPresent());

        entry = KVEntry.of(KEY, new byte[] { 0, 1, 2 });
        assertTrue(entry.getValueAsInputStream().isPresent());

        var test2 = floats().next();
        var entry2 = KVEntry.of(KEY, test2);
        assertFalse(entry2.getValueAsInputStream().isPresent());
    }

    @Test
    @SuppressWarnings("resource")
    void shouldTranslateFromInputStream() throws IOException {
        assertFalse(KVEntry.of(KEY, (InputStream) null).getValueAsInputStream().isPresent());
        var test = IOStreams.toInputStream(letterStrings().next());
        var entry = KVEntry.of(KEY, test);
        assertTrue(entry.getValueAsInputStream().isPresent());

        entry = entry.newValue(new BufferedInputStream(new FileInputStream(new File("pom.xml"))));
        assertTrue(entry.getValueAsInputStream().isPresent());
    }

    @Test
    void shouldTranslateFromFileHandler() throws IOException {
        assertFalse(KVEntry.of(KEY, (FileLoader) null).getValueAsInputStream().isPresent());
        var entry = KVEntry.of(KEY, new FileSystemLoader("pom.xml"));
        assertTrue(entry.getValueAsInputStream().isPresent());
        assertThrows(IOException.class, () -> {
            KVEntry.of(KEY, new FileSystemLoader("notThere.xml"));
        });
    }

    @Test
    void shouldCreateCopyValue() {
        var test = letterStrings().next();
        var test2 = letterStrings().next();
        var entry = KVEntry.of(KEY, test);
        var copy = entry.newValue(test2);
        assertEquals(test2, copy.getValue());
        assertEquals(entry.getKey(), copy.getKey());
        assertEquals(entry.getMetadata(), copy.getMetadata());
    }
}
