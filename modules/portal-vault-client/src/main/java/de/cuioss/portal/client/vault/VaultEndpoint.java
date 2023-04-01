package de.cuioss.portal.client.vault;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the different types of endpoints that are described with concrete instances of
 * {@link VaultContext}
 *
 * @author Oliver Wolff
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum VaultEndpoint {

    /** Identifies the debug-endpoint for accessing health information. */
    HEALTH("health"),

    /** Identifies the Key-Value-endpoint. */
    KEY_VALUE("secret");

    @Getter
    private final String defaultValue;
}
