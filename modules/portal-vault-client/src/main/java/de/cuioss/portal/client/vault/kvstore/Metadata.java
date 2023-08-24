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

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Represents a certain Metadata-element
 *
 * @author Oliver Wolff
 *
 */
@RequiredArgsConstructor
@Builder
@Value
public class Metadata implements Serializable {

    private static final long serialVersionUID = -5519008712013013134L;

    /** Defines an empty Metadata. */
    public static final Metadata EMPTY = Metadata.builder().created(LocalDateTime.now()).destroyed(false).version(1)
            .build();

    @NonNull
    private final LocalDateTime created;

    private final LocalDateTime deleted;

    private final boolean destroyed;

    private final Integer version;

    private final String path;
}
