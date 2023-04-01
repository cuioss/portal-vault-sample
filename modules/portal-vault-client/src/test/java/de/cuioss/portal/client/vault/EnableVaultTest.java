package de.cuioss.portal.client.vault;

import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CLIENT_ENABLED;
import static de.cuioss.portal.configuration.VaultClientConfigKeys.VAULT_CONNECTION_BASE;
import static de.cuioss.portal.configuration.connections.impl.ConnectionMetadataKeys.AUTH_TOKEN_APPLICATION_TOKEN;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;

import de.cuioss.portal.configuration.impl.producer.ConnectionMetadataProducer;
import de.cuioss.portal.core.test.junit5.EnablePortalConfiguration;

/**
 * Using this annotations at type-level of a junit 5 test provides the basic types for handling
 * configuration of the portal and the Vault default configuration
 *
 * @author Oliver Wolff
 *
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@EnableAutoWeld
@EnablePortalConfiguration(configuration = {
    VAULT_CONNECTION_BASE + "." + AUTH_TOKEN_APPLICATION_TOKEN + ":s.77uZJFAV0HSdWrID1AeOBKdP",
    VAULT_CLIENT_ENABLED + ":true"})
@AddBeanClasses({VaultProducer.class, ConnectionMetadataProducer.class, VaultContext.class})
public @interface EnableVaultTest {

}
