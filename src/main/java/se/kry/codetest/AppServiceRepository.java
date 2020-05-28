package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

import java.util.function.Consumer;
import java.util.function.Function;

public class AppServiceRepository {

    private DBConnector connector;

    public AppServiceRepository(DBConnector connector) {
        this.connector = connector;
    }

    public void deleteAppServiceByUrl(JsonArray params) {
        connector.query("DELETE FROM service where url = ?;", params);
    }

    public void deleteAppServiceByUrl(RoutingContext req, Function<RoutingContext, JsonArray> initializationOperation, Consumer<RoutingContext> successOperation) {
        connector.query("DELETE FROM service where url = ?;", req, initializationOperation, successOperation);
    }

    public void saveAppService(JsonArray params) {
        connector.query("INSERT INTO service (name, url, creationDate) VALUES(?, ?, ?);", params);
    }

    public void saveAppService(RoutingContext req, Function<RoutingContext, JsonArray> initializationOperation, Consumer<RoutingContext> successOperation) {
        connector.query("INSERT INTO service (name, url, creationDate) VALUES(?, ?, ?);", req, initializationOperation, successOperation);
    }

    public void updateAppService(JsonArray params) {
        connector.query("UPDATE service SET name = ? where url = ?;", params);
    }

    public void updateAppService(RoutingContext req, Function<RoutingContext, JsonArray> initializationOperation, Consumer<RoutingContext> successOperation) {
        connector.query("UPDATE service SET name = ? where url = ?;",  req, initializationOperation, successOperation);
    }

    public Future<ResultSet> getAllServices() {
        return connector.query("select * from service");
    }

    public Future<ResultSet> getAllServices(Consumer<ResultSet> successOperation) {
        return connector.query("select * from service", successOperation);
    }
}
