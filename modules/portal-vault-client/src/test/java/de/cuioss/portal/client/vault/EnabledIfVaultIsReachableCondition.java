package de.cuioss.portal.client.vault;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.net.URL;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * @author Oliver Wolff
 *
 */
public class EnabledIfVaultIsReachableCondition implements ExecutionCondition {

    private static final Logger log = LoggerFactory.getLogger(EnabledIfVaultIsReachableCondition.class);

    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT =
        ConditionEvaluationResult.enabled(
                "@EnabledIfVaultIsReachable is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var element = context
                .getElement()
                .orElseThrow(IllegalStateException::new);
        return AnnotationUtils.findAnnotation(element, EnabledIfVaultIsReachable.class)
                .map(annotation -> disableIfUnreachable(annotation, element))
                .orElse(ENABLED_BY_DEFAULT);
    }

    private ConditionEvaluationResult disableIfUnreachable(
            EnabledIfVaultIsReachable annotation, AnnotatedElement element) {
        var url = annotation.url();
        var reachable = true;
        try {
            new URL(url).openConnection().connect();
        } catch (IOException e) {
            reachable = false;
            log.info(() -> "Unable to connect due to " + e.getMessage());
        }

        if (reachable) {
            return enabled(format(
                    "%s is enabled because %s is reachable",
                    element, url));
        }
        return disabled(format(
                "%s is disabled because %s could not be reached",
                element, url));
    }
}
