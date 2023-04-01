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
    private static final RestResponse CONTENT_RESPONSE =
        new RestResponse(HttpServletResponse.SC_OK, "application/json",
                "{\"request_id\": \"75c0ebac-5412-5ac8-90f0-fa8267752c7c\"}".getBytes());
    private static final RestResponse EMPTY_RESPONSE =
        new RestResponse(HttpServletResponse.SC_NO_CONTENT, "application/json", new byte[] {});

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
