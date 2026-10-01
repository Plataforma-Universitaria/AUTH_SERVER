package br.ueg.tc.auth.server.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthControllerSecurityTest {

    @Test
    void tokenAndLogoutRequireConfiguredManagementKey() {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "managementKey", "a".repeat(40));

        assertEquals(401, controller.getToken("123", null).getStatusCode().value());
        assertEquals(401, controller.logout("123", "wrong-key").getStatusCode().value());
    }

    @Test
    void shortManagementKeyCannotEnableTokenRetrieval() {
        AuthController controller = new AuthController(null);
        ReflectionTestUtils.setField(controller, "managementKey", "short-key");

        assertEquals(401, controller.getToken("123", "short-key").getStatusCode().value());
    }
}
