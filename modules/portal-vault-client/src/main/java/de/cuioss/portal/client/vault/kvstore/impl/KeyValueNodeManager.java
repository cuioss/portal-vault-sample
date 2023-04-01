package de.cuioss.portal.client.vault.kvstore.impl;

import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.notFound;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.serviceNotAvailable;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.valid;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.vaultException;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.vaultHttpError;
import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static de.cuioss.tools.collect.MoreCollections.isEmpty;
import static de.cuioss.tools.string.MoreStrings.requireNotEmpty;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.rest.RestResponse;

import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.kvstore.KVEntry;
import de.cuioss.portal.client.vault.kvstore.Metadata;
import de.cuioss.portal.client.vault.kvstore.NodeManager;
import de.cuioss.portal.client.vault.util.VaultJsonHelper;
import de.cuioss.tools.collect.CollectionBuilder;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.tools.string.MoreStrings;
import de.cuioss.uimodel.result.ResultObject;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author Oliver Wolff
 *
 */
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class KeyValueNodeManager implements NodeManager {

    private static final String BASE64_BIN = "base64-binary:";
    private static final String SERVICE_NAME = "KeyValueNodeManager";
    private static final CuiLogger log = new CuiLogger(KeyValueNodeManager.class);

    private static final List<Integer> WRITE_OK =
        immutableList(HttpServletResponse.SC_CREATED, HttpServletResponse.SC_OK);

    @NonNull
    @Getter
    private final KeyValueNavigator navigator;

    @Getter(AccessLevel.PROTECTED)
    private final VaultContext vault;

    @Override
    public ResultObject<Collection<KVEntry>> read() {
        log.debug("Calling read() on '{}' ", navigator.getFullPath());
        if (!navigator.isServiceAvailable()) {
            return serviceNotAvailable(Collections.emptyList(), SERVICE_NAME, navigator.getServiceState());
        }
        try {
            var response = vault.getVault().logical().read(navigator.getFullPath());
            var meta = extractMetadata(response.getRestResponse());
            var builder = new CollectionBuilder<KVEntry>();
            for (Entry<String, String> entry : response.getData().entrySet()) {
                builder.add(
                        KVEntry.builder().key(entry.getKey()).value(decodePayload(entry.getValue())).metadata(meta)
                                .build());
            }
            return valid(builder.toImmutableList());
        } catch (VaultException e) {
            return vaultException(Collections.emptyList(), e);
        }
    }

    Metadata extractMetadata(RestResponse response) {
        var dataOptional = VaultJsonHelper.getAsJsonObject(response, "data");
        if (!dataOptional.isPresent()) {
            log.debug("No data-object given to extract from");
            return Metadata.EMPTY;
        }
        var metaObjectOption = VaultJsonHelper.getAsJsonObject(dataOptional.get(), "metadata");
        if (!metaObjectOption.isPresent()) {
            log.debug("No metadata given to extract from");
            return Metadata.EMPTY;
        }
        var metaObject = metaObjectOption.get();
        var creationTime = metaObject.getString("created_time", "");
        var deletionTime = metaObject.getString("deletion_time", "");
        Boolean destroyed = metaObject.getBoolean("destroyed", false);
        Integer version = metaObject.getInt("version", 1);
        var builder = Metadata.builder();
        if (!MoreStrings.isEmpty(creationTime)) {
            try {
                builder.created(ZonedDateTime.parse(creationTime)
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toLocalDateTime());
            } catch (DateTimeParseException e) {
                log.warn(e, "Unable to determine Creation date from {}, derived by key 'created_time'", dataOptional.get());
                builder.created(LocalDateTime.now());
            }
        } else {
            log.warn("Unable to determine Creation date from {}, defaulting to 'now'", dataOptional.get());
        }
        if (!MoreStrings.isEmpty(deletionTime)) {
            try {
                builder.deleted(ZonedDateTime.parse(deletionTime)
                        .truncatedTo(ChronoUnit.SECONDS)
                        .toLocalDateTime());
            } catch (DateTimeParseException e) {
                log.warn(e, "Unable to determine deletion date from {}, derived by key 'deletion_time'", dataOptional.get());
            }
        }
        return builder.destroyed(destroyed).version(version).path(navigator.getFullPath()).build();
    }

    @Override
    public ResultObject<KVEntry> write(KVEntry entry) {
        requireNonNull(entry);
        log.debug("Calling write() on '{}' with entry '{}'", navigator.getFullPath(), entry);

        var written = write(immutableList(entry));
        if (!written.isValid()) {
            return ResultObject.<KVEntry> builder().extractStateAndDetailsAndErrorCodeFrom(written)
                    .validDefaultResult(KVEntry.EMPTY).build();
        }
        var readAgain =
            written.getResult().stream().filter(read -> entry.getKey().equals(read.getKey())).findFirst();

        if (readAgain.isPresent()) {
            return valid(readAgain.get());
        }
        return notFound(KVEntry.EMPTY, "KeyValue", entry.getKey());
    }

    @Override
    public ResultObject<Collection<KVEntry>> write(Collection<KVEntry> entries) {
        log.debug("Calling write() on '{}' with entries '{}'", navigator.getFullPath(), entries);
        if (!navigator.isServiceAvailable()) {
            return serviceNotAvailable(Collections.emptyList(), SERVICE_NAME, navigator.getServiceState());
        }
        if (isEmpty(entries)) {
            return valid(Collections.emptyList());
        }
        Map<String, Object> parameter = new HashMap<>();
        entries.forEach(entry -> parameter.put(entry.getKey(), parse(entry)));
        // Now fetch existing entries: the write method acts as overwrite and not as append for the
        // complete path
        var existing = read();
        if (existing.isValid() && !existing.getResult().isEmpty()) {
            log.debug("Taking over already persisted properties, {}", existing.getResult());
            existing.getResult().forEach(entry -> parameter.putIfAbsent(entry.getKey(), parse(entry)));
        }
        // Write through
        try {
            var response = vault.getVault().logical().write(navigator.getFullPath(), parameter);
            if (!WRITE_OK.contains(response.getRestResponse().getStatus())) {
                return vaultHttpError(Collections.emptyList(), response.getRestResponse());
            }
            log.debug("Wrote on '{}' with entries '{}'", navigator.getFullPath(), entries);
        } catch (VaultException e) {
            return vaultException(Collections.emptyList(), e);
        }
        // Read again from backend
        var builder = new CollectionBuilder<KVEntry>();
        var allentries = read();
        if (!allentries.isValid()) {
            return ResultObject.<Collection<KVEntry>> builder().extractStateAndDetailsAndErrorCodeFrom(allentries)
                    .validDefaultResult(Collections.emptyList()).build();
        }
        log.debug("Read all entries on '{}' with entries '{}'", navigator.getFullPath(), entries);
        var written = parameter.keySet();
        allentries.getResult().stream().filter(read -> written.contains(read.getKey())).forEach(builder::add);

        return valid(builder.toImmutableList());
    }

    private Object parse(KVEntry entry) {
        var value = entry.getValue();
        if (null == value) {
            return null;
        }
        if (value instanceof byte[]) {
            log.debug("Found byte[] as payload");
            var valueArray = (byte[]) value;
            if (0 == valueArray.length) {
                return null;
            }
            return BASE64_BIN + new String(Base64.getEncoder().encode(valueArray));
        }
        return value;
    }

    private Serializable decodePayload(String value) {
        if (MoreStrings.isEmpty(value)) {
            return value;
        }
        if (value.startsWith(BASE64_BIN)) {
            return Base64.getDecoder().decode(value.substring(BASE64_BIN.length()));
        }
        return value;
    }

    @Override
    public ResultObject<KVEntry> read(String key) {
        log.debug("Calling read() on '{}' with key '{}'", navigator.getFullPath(), key);
        requireNotEmpty(key);
        var allentries = read();
        if (!allentries.isValid()) {
            return ResultObject.<KVEntry> builder().extractStateAndDetailsAndErrorCodeFrom(allentries)
                    .validDefaultResult(KVEntry.EMPTY).build();
        }
        var found =
            allentries.getResult().stream().filter(entry -> key.equals(entry.getKey())).findFirst();
        if (found.isPresent()) {
            return valid(found.get());
        }
        return notFound(KVEntry.EMPTY, "KeyValue", key);
    }

    @Override
    public ResultObject<Boolean> delete(String key) {
        log.debug("Calling delete() on '{}' with key '{}'", navigator.getFullPath(), key);
        var read = read();
        if (!read.isValid()) {
            return ResultObject.<Boolean> builder().extractStateAndDetailsAndErrorCodeFrom(read)
                    .validDefaultResult(Boolean.FALSE).build();
        }
        Map<String, Object> parameter = new HashMap<>();
        read.getResult().forEach(entry -> parameter.putIfAbsent(entry.getKey(), entry.getValue()));
        if (!parameter.containsKey(key)) {
            return notFound(Boolean.FALSE, key, key);
        }
        parameter.remove(key);
        try {
            var response = vault.getVault().logical().write(navigator.getFullPath(), parameter);
            if (!WRITE_OK.contains(response.getRestResponse().getStatus())) {
                return vaultHttpError(Boolean.FALSE, response.getRestResponse());
            }
            log.debug("Deletion of '{}' result in Status '{}' ", key, response.getRestResponse().getStatus());
            return valid(Boolean.TRUE);
        } catch (VaultException e) {
            return vaultException(Boolean.FALSE, e);
        }
    }

}
