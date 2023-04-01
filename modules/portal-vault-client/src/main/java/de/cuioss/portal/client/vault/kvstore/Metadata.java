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
    public static final Metadata EMPTY =
        Metadata.builder().created(LocalDateTime.now()).destroyed(false).version(1).build();

    @NonNull
    private final LocalDateTime created;

    private final LocalDateTime deleted;

    private final boolean destroyed;

    private final Integer version;

    private final String path;
}
