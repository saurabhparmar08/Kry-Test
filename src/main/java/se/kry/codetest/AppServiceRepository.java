package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

public class AppServiceRepository {

    private DBConnector connector;

    public AppServiceRepository(DBConnector connector) {
        this.connector = connector;
    }

    public void deleteAppServiceByUrl(JsonArray params) {
        connector.query("DELETE FROM service where url = ?;", params);
    }

    public void saveAppService(JsonArray params) {
        connector.query("INSERT INTO service (name, url, creationDate) VALUES(?, ?, ?);", params);
    }

    public void updateAppService(JsonArray params) {
        connector.query("UPDATE service SET name = ? where url = ?;", params);
    }

    public Future<ResultSet> getAllServices() {
        return connector.query("select * from service");
    }
}
