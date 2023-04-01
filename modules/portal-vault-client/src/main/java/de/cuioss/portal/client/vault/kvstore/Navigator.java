package de.cuioss.portal.client.vault.kvstore;

import java.util.List;

import de.cuioss.uimodel.result.ResultObject;
import de.cuioss.uimodel.service.OptionalService;

/**
 * Navigate through a number of paths / nodes. Paths at this level are relative to the actual mount
 * point, e.g. "https://127.0.0.1:8200/v1/secret/platform/namespaces" will result in Navigator local
 * path "/platform/namespaces"
 *
 * @author Oliver Wolff
 *
 */
public interface Navigator extends OptionalService {

    /**
     * @return the path of the actual Navigator instance. In case of being the root path it returns
     *         "/"
     */
    String getPath();

    /**
     * @return the context-path of the actual Navigator instance. In case of the being the root-node
     *         it returns '/'. In case of e.g. "/platform/namespaces" it returns "namespaces"
     */
    String getContext();

    /**
     * @return the direct parent element of the current Node, in case of being the root node the
     *         node itself will be returned
     */
    Navigator getParent();

    /**
     * @return a list of {@link Navigator} that are direct children of the current
     *         {@link Navigator}. An empty List is a valid result for the case it does not provide
     *         any children
     */
    ResultObject<List<Navigator>> list();

    /**
     * @param path identifying the path to be navigated to. They are interpreted as absolute in the
     *            context, saying if you are within "/platform/namespaces" "/platform" will result
     *            in "/platform" not in "/platform/namespaces/platform"
     * @return a list of {@link Navigator} that are direct children of the current
     *         {@link Navigator}. An empty List is a valid result for the case it does not provide
     *         any children
     */
    ResultObject<List<Navigator>> list(String path);

    /**
     * Creates a new path element relative to the current {@link Navigator} instance
     *
     * @param path to be created, must not be null, nor empty nor "/"
     * @return A {@link Navigator} on the previously created path
     */
    ResultObject<Navigator> create(String path);

    /**
     * Deletes the currently active / selected node.
     *
     * @return Boolean indicating whether the deletion was successful.
     */
    ResultObject<Boolean> delete();

    /**
     * @return the concrete {@link NodeManager} for the current {@link Navigator}
     */
    ResultObject<NodeManager> getNodeManager();

}
