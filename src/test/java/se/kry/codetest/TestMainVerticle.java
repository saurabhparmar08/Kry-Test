package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {


    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertTrue(body.size() > 1);
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void insert_services(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(new JsonObject()
                                .put("name", "krytest")
                                .put("url", UUID.randomUUID().toString()),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void update_service(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .put(8080, "::1", "/service")
                .sendJsonObject(new JsonObject()
                                .put("name", "krytestupdated")
                                .put("url", "https://www.kry.se"),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_service(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .delete(8080, "::1", "/service")
                .sendJsonObject(new JsonObject()
                                .put("url", "https://www.kry.se"),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            testContext.completeNow();
                        }));
    }

}