package de.cuioss.portal.client.vault.util;

import java.util.Optional;

import com.bettercloud.vault.json.Json;
import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.json.JsonValue;
import com.bettercloud.vault.rest.RestResponse;

import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.tools.string.MoreStrings;
import lombok.experimental.UtilityClass;

/**
 * Sitting on the top of the Json-tooling provided by vault-java-driver
 *
 * @author Oliver Wolff
 *
 */
@UtilityClass
public class VaultJsonHelper {

    private static final CuiLogger log = new CuiLogger(VaultJsonHelper.class);

    /**
     * @param response may be null. The actual {@link RestResponse} to be extracted from
     * @return an {@link JsonValue} representation of the body-content if available
     */
    public static Optional<JsonValue> fromResponse(RestResponse response) {
        if (null == response || 0 == response.getBody().length) {
            log.debug("No response given to extract from");
            return Optional.empty();
        }
        return Optional.of(Json.parse(new String(response.getBody())));
    }

    /**
     * @param jsonValue
     * @return an {@link JsonObject} representation of the given {@link JsonValue} if available
     */
    public static Optional<JsonObject> fromJsonValue(JsonValue jsonValue) {
        if (null == jsonValue || !jsonValue.isObject()) {
            log.debug("No jsonValue given to convert");
            return Optional.empty();
        }
        return Optional.ofNullable(jsonValue.asObject());
    }

    /**
     * @param parent identifying the container to be searched.
     * @param elementName identifying the child element to be looked up. If it is null or empty the
     *            parent element will be returned
     * @return The {@link JsonValue} representation of the the contained object identified by the
     *         given elementName if available
     */
    public static Optional<JsonValue> getAsJsonValue(JsonObject parent, String elementName) {
        if (null == parent) {
            return Optional.empty();
        }
        if (MoreStrings.isEmpty(elementName)) {
            return Optional.of(parent);
        }
        return Optional.ofNullable(parent.get(elementName));
    }

    /**
     * @param response The actual {@link RestResponse} to be extracted from
     * @param elementName identifying the child element to be looked up. If it is null or empty the
     *            parent element will be returned
     * @return The {@link JsonValue} representation of the the contained object identified by the
     *         given elementName if available
     */
    public static Optional<JsonValue> getAsJsonValue(RestResponse response, String elementName) {
        var element = fromJsonValue(fromResponse(response).orElse(null));
        if (!element.isPresent()) {
            return Optional.empty();
        }
        return getAsJsonValue(element.get(), elementName);
    }

    /**
     * @param response The actual {@link RestResponse} to be extracted from
     * @param elementName identifying the child element to be looked up. If it is null or empty the
     *            parent element will be returned
     * @return The {@link JsonValue} representation of the the contained object identified by the
     *         given elementName if available
     */
    public static Optional<String> getAsStringValue(RestResponse response, String elementName) {
        var element = getAsJsonValue(response, elementName);
        if (element.isPresent()) {
            return Optional.ofNullable(element.get().toString());
        }
        return Optional.empty();
    }

    /**
     * @param response The actual {@link RestResponse} to be extracted from
     * @param elementName identifying the child element to be looked up. If it is null or empty the
     *            parent element will be returned
     * @return The {@link JsonValue} representation of the the contained object identified by the
     *         given elementName if available
     */
    public static Optional<JsonObject> getAsJsonObject(RestResponse response, String elementName) {
        return fromJsonValue(getAsJsonValue(response, elementName).orElse(null));
    }

    /**
     * @param parent identifying the container to be searched.
     * @param elementName identifying the child element to be looked up. If it is null or empty the
     *            parent element will be returned
     * @return The {@link JsonObject} representation of the the contained object identified by the
     *         given elementName if available
     */
    public static Optional<JsonObject> getAsJsonObject(JsonObject parent, String elementName) {
        return fromJsonValue(getAsJsonValue(parent, elementName).orElse(null));
    }
}
