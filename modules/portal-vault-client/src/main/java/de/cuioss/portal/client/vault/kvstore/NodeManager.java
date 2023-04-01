package de.cuioss.portal.client.vault.kvstore;

import java.util.Collection;

import de.cuioss.uimodel.result.ResultObject;

/**
 * Manager Object for reading / updating the Key Values on a given Path identified by an
 * {@link Navigator}
 *
 * @author Oliver Wolff
 *
 */
public interface NodeManager {

    /**
     * @return the Navigator belonging the NodeManager
     */
    Navigator getNavigator();

    /**
     * @return the actual content of the key / values at this node
     */
    ResultObject<Collection<KVEntry>> read();

    /**
     * @param entry to be written
     * @return the written entry with updated Metadata
     */
    ResultObject<KVEntry> write(KVEntry entry);

    /**
     * @param entries to be written
     * @return the written entries with updated Metadata
     */
    ResultObject<Collection<KVEntry>> write(Collection<KVEntry> entries);

    /**
     * Reads a specified key
     *
     * @param key
     * @return the specified {@link KVEntry}
     */
    ResultObject<KVEntry> read(String key);

    /**
     * Deletes a specified key
     *
     * @param key
     * @return boolean indicating whether the deletion was successful.
     */
    ResultObject<Boolean> delete(String key);
}
