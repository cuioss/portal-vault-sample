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
package de.cuioss.portal.client.vault;

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

    private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = ConditionEvaluationResult
            .enabled("@EnabledIfVaultIsReachable is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var element = context.getElement().orElseThrow(IllegalStateException::new);
        return AnnotationUtils.findAnnotation(element, EnabledIfVaultIsReachable.class)
                .map(annotation -> disableIfUnreachable(annotation, element)).orElse(ENABLED_BY_DEFAULT);
    }

    private ConditionEvaluationResult disableIfUnreachable(EnabledIfVaultIsReachable annotation,
            AnnotatedElement element) {
        var url = annotation.url();
        var reachable = true;
        try {
            new URL(url).openConnection().connect();
        } catch (IOException e) {
            reachable = false;
            log.info(() -> "Unable to connect due to " + e.getMessage());
        }

        if (reachable) {
            return enabled("%s is enabled because %s is reachable".formatted(element, url));
        }
        return disabled("%s is disabled because %s could not be reached".formatted(element, url));
    }
}
