package de.cuioss.portal.client.vault;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Oliver Wolff
 *
 */
@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(EnabledIfVaultIsReachableCondition.class)
public @interface EnabledIfVaultIsReachable {

    /**
     * @return identifying the url under which vault is available
     */
    String url() default "http://127.0.0.1:8200";
}
