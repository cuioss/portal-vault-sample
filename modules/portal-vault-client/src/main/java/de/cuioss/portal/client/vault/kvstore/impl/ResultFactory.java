
package de.cuioss.portal.client.vault.kvstore.impl;

import static de.cuioss.tools.collect.CollectionLiterals.immutableList;

import java.io.Serializable;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.rest.RestResponse;

import de.cuioss.uimodel.nameprovider.DisplayName;
import de.cuioss.uimodel.nameprovider.LabeledKey;
import de.cuioss.uimodel.result.ResultDetail;
import de.cuioss.uimodel.result.ResultErrorCodes;
import de.cuioss.uimodel.result.ResultObject;
import de.cuioss.uimodel.result.ResultState;
import de.cuioss.uimodel.service.ServiceState;
import lombok.experimental.UtilityClass;

/**
 * Factory for default {@link ResultObject}s including some Messages
 *
 * @author Oliver Wolff
 *
 */
@UtilityClass
public class ResultFactory {

    /**
     * The service '{0}' is not available due to '{1}'. If this error persists contact your
     * administrator.
     */
    static final String SERVICE_NOT_AVAILABLE_KEY = "service.not.available";

    /**
     * The element '{0}' identified by '{1}' was not found.
     */
    static final String ELEMENT_NOT_FOUND_KEY = "service.element.not_found";

    /**
     * Shorthand for creating a result object indicating that a concrete service is not available.
     * The handling strategy {@link ResultErrorCodes#SERVICE_NOT_AVAILABLE} will be added as well.
     *
     * @param <T> identifying the type of the result.
     * @param defaultResult
     * @param serviceName
     * @param state
     * @return the created result-object.
     */
    public static final <T> ResultObject<T> serviceNotAvailable(T defaultResult, String serviceName,
            ServiceState state) {
        return ResultObject.<T> builder().validDefaultResult(defaultResult).state(ResultState.ERROR)
                .resultDetail(ResultDetail.builder()
                        .detail(new LabeledKey(SERVICE_NOT_AVAILABLE_KEY, immutableList(serviceName, state))).build())
                .errorCode(ResultErrorCodes.SERVICE_NOT_AVAILABLE)
                .build();
    }

    /**
     * Shorthand for creating a result object indicating that a concrete service is not available.
     * The handling strategy {@link ResultErrorCodes#SERVICE_NOT_AVAILABLE} will be added as well.
     * The default result will be {@link Boolean#FALSE}
     *
     * @param serviceName
     * @param state
     * @return the created result-object.
     */
    public static final ResultObject<Boolean> serviceNotAvailableBoolean(String serviceName, ServiceState state) {
        return serviceNotAvailable(Boolean.FALSE, serviceName, state);
    }

    /**
     * Shorthand for creating a result object indicating that a concrete service is not available.
     * The handling strategy {@link ResultErrorCodes#SERVICE_NOT_AVAILABLE} will be added as well.
     * The the default result will be an empty String
     *
     * @param serviceName
     * @param state
     * @return the created result-object.
     */
    public static final ResultObject<String> serviceNotAvailableString(String serviceName, ServiceState state) {
        return serviceNotAvailable("", serviceName, state);
    }

    /**
     * Shorthand for creating a valid result object.
     *
     * @param <T> identifying the type of the result.
     * @param result to be wrapped
     * @return the created result-object.
     */
    public static final <T> ResultObject<T> valid(T result) {
        return ResultObject.<T> builder().result(result).state(ResultState.VALID).build();
    }

    /**
     * Shorthand for creating an invalid result object (WARNING) communicating that a certain
     * element was not found. The message created is "The element '{0}' identified by '{1}' was not
     * found". The handling strategy {@link ResultErrorCodes#NOT_FOUND} will be added as well.
     *
     * @param <T> identifying the type of the result.
     * @param defaultResult to be used
     * @param elementName identifying the logical name of the element, like 'policy'
     * @param identifier the search identifier passed for the lookup.
     * @return the created result-object.
     */
    public static final <T> ResultObject<T> notFound(T defaultResult, String elementName, Serializable identifier) {
        return ResultObject.<T> builder().validDefaultResult(defaultResult).state(ResultState.WARNING)
                .resultDetail(ResultDetail.builder()
                        .detail(new LabeledKey(ELEMENT_NOT_FOUND_KEY, immutableList(elementName, identifier))).build())
                .errorCode(ResultErrorCodes.NOT_FOUND)
                .build();
    }

    /**
     * Creates an error result of the given {@link VaultException}
     *
     * @param <T>
     * @param defaultResult
     * @param exception
     * @return the created error result
     */
    public static final <T> ResultObject<T> vaultException(T defaultResult, VaultException exception) {

        return ResultObject.<T> builder().validDefaultResult(defaultResult).state(ResultState.ERROR)
                .resultDetail(ResultDetail.builder()
                        .detail(new DisplayName(exception.getMessage())).build())
                .errorCode(ResultErrorCodes.parseHttpCode(exception.getHttpStatusCode()))
                .build();
    }

    /**
     * Creates an error result of the given {@link RestResponse}
     *
     * @param <T>
     * @param defaultResult
     * @param response
     * @return the created error result
     */
    public static final <T> ResultObject<T> vaultHttpError(T defaultResult, RestResponse response) {

        return ResultObject.<T> builder().validDefaultResult(defaultResult).state(ResultState.ERROR)
                .resultDetail(ResultDetail.builder()
                        .detail(new DisplayName(new String(response.getBody()))).build())
                .errorCode(ResultErrorCodes.parseHttpCode(response.getStatus()))
                .build();
    }

}
