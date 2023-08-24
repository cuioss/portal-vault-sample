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
package de.cuioss.portal.client.vault.util;

import static de.cuioss.portal.client.vault.util.VaultJsonHelper.fromResponse;
import static de.cuioss.portal.client.vault.util.VaultJsonHelper.getAsJsonObject;
import static de.cuioss.portal.client.vault.util.VaultJsonHelper.getAsJsonValue;
import static de.cuioss.portal.client.vault.util.VaultJsonHelper.getAsStringValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import com.bettercloud.vault.json.JsonObject;
import com.bettercloud.vault.rest.RestResponse;

class VaultJsonHelperTest {

    private static final String OTHER_ID = "otherId";
    private static final String REQUEST_ID = "request_id";
    private static final RestResponse CONTENT_RESPONSE = new RestResponse(HttpServletResponse.SC_OK, "application/json",
            "{\"request_id\": \"75c0ebac-5412-5ac8-90f0-fa8267752c7c\"}".getBytes());
    private static final RestResponse EMPTY_RESPONSE = new RestResponse(HttpServletResponse.SC_NO_CONTENT,
            "application/json", new byte[] {});

    @Test
    void shouldHandleResponse() {
        assertFalse(fromResponse(null).isPresent());
        assertFalse(fromResponse(EMPTY_RESPONSE).isPresent());
        assertTrue(fromResponse(CONTENT_RESPONSE).isPresent());
    }

    @Test
    void shouldExtractStringValue() {
        assertFalse(getAsStringValue(null, REQUEST_ID).isPresent());
        assertFalse(getAsStringValue(EMPTY_RESPONSE, REQUEST_ID).isPresent());
        assertTrue(getAsStringValue(CONTENT_RESPONSE, REQUEST_ID).isPresent());
        assertFalse(getAsStringValue(CONTENT_RESPONSE, OTHER_ID).isPresent());
        assertTrue(getAsStringValue(CONTENT_RESPONSE, "").isPresent());
    }

    @Test
    void shouldExtractJsonValue() {
        assertFalse(getAsJsonValue((RestResponse) null, REQUEST_ID).isPresent());
        assertFalse(getAsJsonValue(EMPTY_RESPONSE, REQUEST_ID).isPresent());
        assertTrue(getAsJsonValue(CONTENT_RESPONSE, REQUEST_ID).isPresent());
        assertFalse(getAsJsonValue(CONTENT_RESPONSE, OTHER_ID).isPresent());
        assertTrue(getAsJsonValue(CONTENT_RESPONSE, "").isPresent());
    }

    @Test
    void shouldExtractJsonObject() {
        assertFalse(getAsJsonObject((RestResponse) null, REQUEST_ID).isPresent());
        assertFalse(getAsJsonObject(EMPTY_RESPONSE, REQUEST_ID).isPresent());
        assertFalse(getAsJsonObject(CONTENT_RESPONSE, REQUEST_ID).isPresent());
        assertFalse(getAsJsonObject(CONTENT_RESPONSE, OTHER_ID).isPresent());
        assertTrue(getAsJsonObject(CONTENT_RESPONSE, "").isPresent());
    }

    @Test
    void shouldExtractFromJsonObject() {
        assertFalse(getAsJsonObject((JsonObject) null, REQUEST_ID).isPresent());
        var template = getAsJsonObject(CONTENT_RESPONSE, "").get();

        assertFalse(getAsJsonObject(template, REQUEST_ID).isPresent());
        assertFalse(getAsJsonObject(template, REQUEST_ID).isPresent());
        assertFalse(getAsJsonObject(template, OTHER_ID).isPresent());
        assertTrue(getAsJsonObject(template, "").isPresent());
    }

}
