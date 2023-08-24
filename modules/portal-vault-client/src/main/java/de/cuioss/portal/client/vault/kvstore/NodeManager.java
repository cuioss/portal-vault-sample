/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cuioss.portal.client.vault.kvstore;

import java.util.Collection;

import de.cuioss.uimodel.result.ResultObject;

/**
 * Manager Object for reading / updating the Key Values on a given Path
 * identified by an {@link Navigator}
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
