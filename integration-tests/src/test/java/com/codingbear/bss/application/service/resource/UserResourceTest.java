/*
 * Copyright (c) 2019 Coding Bear s.r.o.
 */
package com.codingbear.bss.application.service.resource;

import com.codingbear.bss.application.service.DbClearingExtension;
import com.codingbear.bss.application.service.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"/sql/test-data.sql"})
@ExtendWith(SpringExtension.class)
@ExtendWith(DbClearingExtension.class)
@DirtiesContext
class UserResourceTest {
    @LocalServerPort
    private int port;

    private String resourceUrl;

    @BeforeEach
    void setUp() {
        resourceUrl = URI.create(String.format("http://localhost:%d/v1/user", port)).toString();
    }

    @Test
    void getUserWorks() throws IOException {
        final String response = given()
                .when()
                .get(resourceUrl + "/1")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        String expected = IOUtils.toString(this.getClass().getResourceAsStream("/resource/user-resource/user-response.json"), StandardCharsets.UTF_8);
        JsonUtils.assertEqualsJson(expected, response);
    }
}
