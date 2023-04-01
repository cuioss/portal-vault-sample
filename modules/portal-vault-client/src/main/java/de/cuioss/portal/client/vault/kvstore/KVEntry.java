package de.cuioss.portal.client.vault.kvstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Optional;

import de.cuioss.tools.io.FileLoader;
import de.cuioss.tools.io.IOStreams;
import de.cuioss.tools.logging.CuiLogger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Represents a Key / Value entry with the corresponding metadata
 *
 * @author Oliver Wolff
 *
 */
@RequiredArgsConstructor
@Builder
@Value
@EqualsAndHashCode(of = { "key", "value" })
public class KVEntry implements Serializable {

    private static final long serialVersionUID = -25755245193850618L;

    private static final CuiLogger log = new CuiLogger(KVEntry.class);

    /** Defines an empty not null instance of {@link KVEntry}. */
    public static final KVEntry EMPTY = new KVEntry("EMPTY", "EMPTY", Metadata.EMPTY);

    @NonNull
    private final String key;

    private final Serializable value;

    @NonNull
    private final Metadata metadata;

    /**
     * @return a String representation of the contained value if available.
     */
    public Optional<String> getValueAsString() {
        if (null == value) {
            return Optional.empty();
        }
        return Optional.of(String.valueOf(value));
    }

    /**
     * @return a Boolean representation of the contained value if available.
     */
    public Optional<Boolean> getValueAsBoolean() {
        if (null == value) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(String.valueOf(value)));
    }

    /**
     * @return an Integer representation of the contained value if available.
     */
    public Optional<Integer> getValueAsInteger() {
        if (null == value) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(String.valueOf(value)));
        } catch (NumberFormatException e) {
            log.warn(e, "Unable to parse {} to Integer, key={}", value, key);
            return Optional.empty();
        }
    }

    /**
     * @return a Double representation of the contained value if available.
     */
    public Optional<Double> getValueAsDouble() {
        if (null == value) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(String.valueOf(value)));
        } catch (NumberFormatException e) {
            log.warn(e, "Unable to parse {} to Double, key={}", value, key);
            return Optional.empty();
        }
    }

    /**
     * @return a Float representation of the contained value if available.
     */
    public Optional<Float> getValueAsFloat() {
        if (null == value) {
            return Optional.empty();
        }
        try {
            return Optional.of(Float.parseFloat(String.valueOf(value)));
        } catch (NumberFormatException e) {
            log.warn(e, "Unable to parse {} to Float, key={}", value, key);
            return Optional.empty();
        }
    }

    /**
     * @return a {@link InputStream} representation of the contained value if available.
     */
    public Optional<InputStream> getValueAsInputStream() {
        if (null == value) {
            return Optional.empty();
        }
        if (value instanceof String) {
            return Optional.of(IOStreams.toInputStream((String) value));
        }
        if (value instanceof byte[]) {
            return Optional.of(new ByteArrayInputStream((byte[]) value));
        }
        log.error("Unknown type detected key='{}', type='{}'", key, value.getClass());
        return Optional.empty();
    }

    /**
     * Shorthand for creating a new instance
     *
     * @param newValue
     * @return a new instance of this {@link KVEntry} with the given value but containing the
     *         previous metadata
     */
    public KVEntry newValue(Serializable newValue) {
        return builder().key(key).metadata(metadata).value(newValue).build();
    }

    /**
     * Shorthand for creating a new instance
     *
     * @param newValue
     * @return a new instance of this {@link KVEntry} with the given value but containing the
     *         previous metadata
     * @throws IOException if an I/O error occurs
     */
    public KVEntry newValue(InputStream newValue) throws IOException {
        return newValue(toByteArray(newValue));
    }

    /**
     * Shorthand for creating a key-value filled {@link KVEntry} with implicitly passed
     * {@link Metadata#EMPTY}
     *
     * @param key must not be null
     * @param value may be null
     * @return the create {@link KVEntry}
     */
    public static KVEntry of(String key, Serializable value) {
        return builder().key(key).value(value).metadata(Metadata.EMPTY).build();
    }

    /**
     * Shorthand for creating a key-value filled {@link KVEntry} with implicitly passed
     * {@link Metadata#EMPTY}
     *
     * @param key must not be null
     * @param input may be null
     * @return the create {@link KVEntry}
     * @throws IOException if an I/O error occurs
     */
    public static KVEntry of(String key, InputStream input) throws IOException {
        return of(key, toByteArray(input));
    }

    /**
     * Shorthand for creating a key-value filled {@link KVEntry} with implicitly passed
     * {@link Metadata#EMPTY}
     *
     * @param key must not be null
     * @param loader may be null
     * @return the create {@link KVEntry}
     * @throws IOException if an I/O error occurs of {@link FileLoader#isReadable()} returning
     *             {@code false}
     */
    public static KVEntry of(String key, FileLoader loader) throws IOException {
        if (null == loader) {
            return of(key, (Serializable) null);
        }
        if (!loader.isReadable()) {
            throw new FileNotFoundException("Unable to load file " + loader.getFileName());
        }
        return of(key, toByteArray(loader.inputStream()));

    }

    /**
     * Translates an {@link InputStream} to an {@link Byte} array in order to serialize
     *
     * @param input containing the content. if it null or not available {@code null} will be
     *            returned
     * @return the content of the stream read into byte-array. may be null
     * @throws IOException
     */
    @SuppressWarnings("squid:S1168") // owolff: returning null is sensible here because the null
                                     // value is a marker
    public static byte[] toByteArray(InputStream input) throws IOException {
        if (null != input && 0 != input.available()) {
            try (final var output = new ByteArrayOutputStream()) {
                IOStreams.copy(input, output);
                return output.toByteArray();
            }
        }
        return null;
    }
}
