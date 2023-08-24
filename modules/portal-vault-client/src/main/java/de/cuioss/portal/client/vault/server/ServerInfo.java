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
package de.cuioss.portal.client.vault.server;

import java.io.Serializable;

import de.cuioss.uimodel.service.ServiceState;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * Represents the remote endpoint, currently vault server
 *
 * @author Oliver Wolff
 *
 */
@Builder
@Value
@SuppressWarnings("squid:S1170") // owolff: False Positive
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = -1828174429679998138L;

    /**
     * Constant providing the information that the server is disabled by
     * configuration.
     */
    public static final ServerInfo NOT_ENABLED = builder().serviceState(ServiceState.NOT_CONFIGURED).build();

    /**
     * Constant providing the information that the server is currently not
     * accessible.
     */
    public static final ServerInfo NOT_ACCESSIBLE = builder().serviceState(ServiceState.TEMPORARILY_UNAVAILABLE)
            .build();

    private final ServiceState serviceState;

    @Default
    private final AttributeStatus healthy = AttributeStatus.UNKOWN;

    @Default
    private final AttributeStatus unsealed = AttributeStatus.UNKOWN;

    @Default
    private final AttributeStatus initialized = AttributeStatus.UNKOWN;

    private final Long serverTimeUTC;

    private final String url;

    private final String information;
}
