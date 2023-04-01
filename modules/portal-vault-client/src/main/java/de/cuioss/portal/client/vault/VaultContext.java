package de.cuioss.portal.client.vault;

import static de.cuioss.tools.base.Preconditions.checkArgument;
import static de.cuioss.tools.net.UrlHelper.splitPath;

import com.bettercloud.vault.Vault;

import de.cuioss.tools.net.UrlHelper;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Provides an instance of Vault including some additional information and utilities
 *
 * @author Oliver Wolff
 *
 */
@Value
@Builder
public class VaultContext {

    @NonNull
    private final Vault vault;

    private final boolean enabled;

    @NonNull
    private final String endpointName;

    private final String url;

    /**
     * Translates a fullPath, like "/secrets/myKeys" to a relative Path "/mykeys"
     *
     * @param fullPath must start with "/{@link #getEndpointName()}"
     * @return the path without the "/{@link #getEndpointName()}"
     */
    public String stripEndpointName(String fullPath) {
        var pathElements = splitPath(fullPath);
        checkArgument(!pathElements.isEmpty(), "No path given");
        checkArgument(endpointName.equals(pathElements.get(0)), "Given path must start with /" + endpointName);
        if (1 == pathElements.size()) {
            return "/";
        }
        return "/" + String.join("/", pathElements.subList(1, pathElements.size()));
    }

    /**
     * Adds "/{@link #getEndpointName()}" to the given partialPath
     *
     * @param partialPath
     * @return the prefixed path
     */
    public String appendToEndpointName(String partialPath) {
        return "/" + getEndpointName() + UrlHelper.addPrecedingSlashToPath(partialPath);
    }
}
