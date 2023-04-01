package de.cuioss.portal.client.vault.kvstore.impl;

import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.notFound;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.serviceNotAvailable;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.valid;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.vaultException;
import static de.cuioss.portal.client.vault.kvstore.impl.ResultFactory.vaultHttpError;
import static de.cuioss.tools.base.Preconditions.checkArgument;
import static de.cuioss.tools.collect.CollectionLiterals.immutableList;
import static de.cuioss.tools.net.UrlHelper.splitPath;
import static de.cuioss.tools.string.MoreStrings.requireNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.bettercloud.vault.VaultException;

import de.cuioss.portal.client.vault.VaultContext;
import de.cuioss.portal.client.vault.kvstore.Navigator;
import de.cuioss.portal.client.vault.kvstore.NodeManager;
import de.cuioss.tools.logging.CuiLogger;
import de.cuioss.tools.net.UrlHelper;
import de.cuioss.tools.string.MoreStrings;
import de.cuioss.uimodel.result.ResultObject;
import de.cuioss.uimodel.service.ServiceState;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author Oliver Wolff
 *
 */
@EqualsAndHashCode(of = { "serviceState", "path", "context" })
@ToString(of = { "serviceState", "path", "context" })
public class KeyValueNavigator implements Navigator {

    private static final String SERVICE_NAME = "KeyValueService";

    private static final CuiLogger log = new CuiLogger(KeyValueNavigator.class);

    private static final List<Integer> CREATE_OK =
        immutableList(HttpServletResponse.SC_CREATED, HttpServletResponse.SC_OK);

    private static final List<Integer> DELETE_OK =
        immutableList(HttpServletResponse.SC_NO_CONTENT);

    @Getter(AccessLevel.PROTECTED)
    private final VaultContext vault;

    @Getter
    private final String path;

    @Getter
    private final String fullPath;

    @Getter
    private final String context;

    @Getter
    private final ServiceState serviceState;

    /**
     * @param vault must not be null
     * @param path must start with "/"
     */
    public KeyValueNavigator(VaultContext vault, String path) {
        this.vault = requireNonNull(vault);
        this.path = requireNotEmpty(path);
        checkArgument(path.startsWith("/"), "Paths must always start with '/'");
        var splitToList = splitPath(path);
        if (splitToList.isEmpty()) {
            context = "/";
        } else {
            context = splitToList.get(splitToList.size() - 1);
        }
        fullPath = vault.appendToEndpointName(path);
        if (vault.isEnabled()) {
            serviceState = ServiceState.ACTIVE;
        } else {
            serviceState = ServiceState.NOT_CONFIGURED;
        }
    }

    @Override
    public Navigator getParent() {
        log.trace("Accessing parent from '{}'", fullPath);
        if ("/".equals(path)) {
            return this;
        }
        var splitToList = splitPath(path);
        if (splitToList.size() < 2) {
            return new KeyValueNavigator(vault, "/");
        }
        var parent = path.substring(0, getContext().length() + 1);
        return new KeyValueNavigator(vault, parent);
    }

    @Override
    public ResultObject<List<Navigator>> list() {
        return list("/");
    }

    @Override
    public ResultObject<List<Navigator>> list(String path) {
        log.debug("Calling list on '{}' with '{}'", fullPath, path);
        if (!isServiceAvailable()) {
            return serviceNotAvailable(Collections.emptyList(), SERVICE_NAME, getServiceState());
        }
        var listPath = fullPath;
        if (!MoreStrings.isEmpty(path) && !"/".equals(path)) {
            listPath = getVault().appendToEndpointName(path);
        }
        listPath = UrlHelper.addTrailingSlashToUrl(listPath);
        try {
            var list = vault.getVault().logical().list(listPath);
            if (HttpServletResponse.SC_NOT_FOUND == list.getRestResponse().getStatus()) {
                if ("/".equals(path)) {
                    log.trace("Assuming no content for subsequent children, returning valid '/' mapping, context='{}'",
                            this);
                    return valid(Collections.emptyList());
                }
                return notFound(Collections.emptyList(), path, SERVICE_NAME);
            }
            var parentSlashed = UrlHelper.addTrailingSlashToUrl(getPath());
            List<Navigator> navigator =
                list.getListData().stream()
                        .map(pathElement -> new KeyValueNavigator(vault, parentSlashed + pathElement))
                        .collect(Collectors.toList());
            return valid(navigator);
        } catch (VaultException e) {
            return vaultException(Collections.emptyList(), e);
        }
    }

    @Override
    public ResultObject<NodeManager> getNodeManager() {
        log.debug("Calling retrieveNodeManager on '{}' with", fullPath);
        if (!isServiceAvailable()) {
            return serviceNotAvailable(new KeyValueNodeManager(this, vault), SERVICE_NAME, getServiceState());
        }
        return valid(new KeyValueNodeManager(this, vault));
    }

    @Override
    public ResultObject<Navigator> create(String path) {
        log.debug("Calling create on '{}' with '{}'", fullPath, path);
        if (!isServiceAvailable()) {
            return serviceNotAvailable(this, SERVICE_NAME, getServiceState());
        }
        requireNotEmpty(path);
        checkArgument(!"/".equals(path), "Path to be created must not be '/'");
        var listPath = getVault().appendToEndpointName(path);
        try {
            var response = vault.getVault().logical().write(listPath, null);
            log.info("Calling create new path '{}' within '{}', with result", fullPath, path,
                    response.getRestResponse().getStatus());
            if (!CREATE_OK.contains(response.getRestResponse().getStatus())) {
                return vaultHttpError(this, response.getRestResponse());
            }
            var parent = UrlHelper.addTrailingSlashToUrl(getPath());
            return valid(new KeyValueNavigator(vault, parent + path));
        } catch (VaultException e) {
            return vaultException(this, e);
        }
    }

    @Override
    public ResultObject<Boolean> delete() {
        log.debug("Calling delete on '{}'", fullPath);
        if (!isServiceAvailable()) {
            return serviceNotAvailable(Boolean.FALSE, SERVICE_NAME, getServiceState());
        }
        try {
            if (!pathExists(path)) {
                log.debug("Resource for delete not found '{}'", fullPath);
                return notFound(Boolean.FALSE, SERVICE_NAME, path);
            }
            var delete = vault.getVault().logical().delete(fullPath);
            if (DELETE_OK.contains(delete.getRestResponse().getStatus())) {
                return valid(Boolean.TRUE);
            }
            return vaultHttpError(Boolean.FALSE, delete.getRestResponse());
        } catch (VaultException e) {
            return vaultException(Boolean.FALSE, e);
        }
    }

    /**
     * Checks whether a given path actually exists on the remote server
     *
     * @param path to be checked
     * @return boolean indicating whether the actual path exists on the remote server.
     */
    boolean pathExists(String path) {
        var listFromParent = getParent().list();
        if (!listFromParent.isValid()) {
            log.debug("Unable to determine correct path from parent '{}'", this);
            return false;
        }
        var searchPath = UrlHelper.removePrecedingSlashFromPath(path);
        List<String> contextNames =
            listFromParent.getResult().stream().map(Navigator::getContext).collect(Collectors.toList());
        return contextNames.contains(searchPath);
    }

}
