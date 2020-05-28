package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppServiceHelper {

    private final static Logger LOGGER = Logger.getLogger(AppServiceHelper.class.getName());

    private final AppServiceRepository appServiceRepository;

    public AppServiceHelper(DBConnector dbConnector) {
        this.appServiceRepository = new AppServiceRepository(dbConnector);
    }

    public void deleteAppServiceByUrl(JsonArray params) {
        appServiceRepository.deleteAppServiceByUrl(params);
    }

    public void updateAppService(JsonArray params) {
        appServiceRepository.updateAppService(params);
    }

    public void saveAppService(JsonArray params) {
        appServiceRepository.saveAppService(params);
    }

    private Map<String, AppService> serviceCache = new HashMap<>();

    public Map<String, AppService> getServiceCache() {
        return serviceCache;
    }

    public void loadServiceCache() {
        Future<ResultSet> services = appServiceRepository.getAllServices();
        Map<String, AppService> result = new HashMap<>();
        services.setHandler(done -> {
            if (done.succeeded()) {
                done.result().getResults()
                        .forEach(json -> result.put(json.getString(0), convertResultToAppService(json)));
                LOGGER.log(Level.INFO, "Cache load complete");
            } else {
                LOGGER.log(Level.SEVERE, "Error in loading cache");
                throw new RuntimeException("Error in loading cache");
            }
            serviceCache = result;
        });
    }

    private AppService convertResultToAppService(JsonArray json) {
        String url = json.getString(0);
        String name = json.getString(1);
        String creationDate = json.getString(2);
        AppService service =  new AppService(name, url);
        service.setCreationDate(creationDate);
        return service;
    }

}
