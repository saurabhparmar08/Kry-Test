package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;

import java.time.LocalDateTime;
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

    public void deleteAppServiceByUrl(RoutingContext req) {
        appServiceRepository.deleteAppServiceByUrl(req,
                (RoutingContext request)-> {
                    String url = request.getBodyAsJson().getString("url");
                    return new JsonArray().add(url);
                },
                (RoutingContext request) -> {
                    getServiceCache().remove(request.getBodyAsJson().getString("url"));
                    setSuccessResponse(req);
                }
        );
    }

    public void updateAppService(JsonArray params) {
        appServiceRepository.updateAppService(params);
    }

    public void updateAppService(RoutingContext req) {
        appServiceRepository.updateAppService(req,
                (RoutingContext request)-> {
                    String name = req.getBodyAsJson().getString("name");
                    String url = req.getBodyAsJson().getString("url");
                    return new JsonArray()
                            .add(name)
                            .add(url);
                },
                (RoutingContext request) -> {
                    String name = req.getBodyAsJson().getString("name");
                    String url = req.getBodyAsJson().getString("url");
                    getServiceCache().put(url, new AppService(name, url));
                    setSuccessResponse(req);
                }
        );
    }

    public void saveAppService(JsonArray params) {
        appServiceRepository.saveAppService(params);
    }

    public void saveAppService(RoutingContext req) {
        appServiceRepository.saveAppService(req,
                (RoutingContext request)-> {
                    String name = req.getBodyAsJson().getString("name");
                    String url = req.getBodyAsJson().getString("url");
                    return new JsonArray()
                            .add(name)
                            .add(url)
                            .add(LocalDateTime.now().toString());
                },
                (RoutingContext request) -> {
                    String name = req.getBodyAsJson().getString("name");
                    String url = req.getBodyAsJson().getString("url");
                    getServiceCache().put(url, new AppService(name, url));
                    setSuccessResponse(req);
                }
        );
    }

    private Map<String, AppService> serviceCache = new HashMap<>();

    public Map<String, AppService> getServiceCache() {
        return serviceCache;
    }

    public void loadServiceCacheAsync() {
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

    public void loadServiceCache() {
        serviceCache.putIfAbsent("KryTest", new AppService("KryTest", "https://www.kry.se"));
        appServiceRepository.getAllServices(
                (ResultSet resultSet) -> {
                    resultSet.getResults().forEach(json -> serviceCache.put(json.getString(0), convertResultToAppService(json)));
                    LOGGER.log(Level.INFO, "Cache load complete");
                }
        );
    }

    private AppService convertResultToAppService(JsonArray json) {
        String url = json.getString(0);
        String name = json.getString(1);
        String creationDate = json.getString(2);
        AppService service =  new AppService(name, url);
        service.setCreationDate(creationDate);
        return service;
    }

    private void setSuccessResponse(RoutingContext req) {
        req.response()
                .putHeader("content-type", "text/plain");
        req.response().end("OK");
    }


}
