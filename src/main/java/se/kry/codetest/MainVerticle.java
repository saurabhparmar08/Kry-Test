package se.kry.codetest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.migrate.DBMigration;

import java.time.LocalDateTime;
import java.util.*;

public class MainVerticle extends AbstractVerticle {

  private Map<String, AppService> serviceCache = new HashMap<>();
  private AppServiceRepository appServiceRepository;
  private DBMigration migration;
  private final BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    appServiceRepository = new AppServiceRepository(new DBConnector(vertx));
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    setRoutes(router);
    configureServer(startFuture, router);
    loadServiceCache();
  }

  private void configureServer(Future<Void> startFuture, Router router) {
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    enableGetServiceRoute(router);
    enableGetPollServiceRoute(router);
    enablePostServiceRoute(router);
    enableDeleteServiceRoute(router);
    enablePutServiceRoute(router);
  }

  private void enableGetServiceRoute(Router router) {
    router.get("/service").handler(req -> setSuccessResponse(req, Optional.of(new JsonArray(new ArrayList<>(serviceCache.values())))));
  }

  private void enableGetPollServiceRoute(Router router) {
    router.get("/poll").handler(req -> {
      List<AppService> services = new ArrayList<>(serviceCache.values());
      poller.pollServicesAndUpdateCache(services);
      setSuccessResponse(req, Optional.of(new JsonArray(services)));
    });
  }

  private void enableDeleteServiceRoute(Router router) {
    router.delete("/service").handler(req -> {
      String url = req.getBodyAsJson().getString("url");
      JsonArray params = new JsonArray().add(url);
      deleteAppServiceByUrl(params);
      serviceCache.remove(url);
      setSuccessResponse(req, Optional.empty());
    });
  }

  private void enablePostServiceRoute(Router router) {
    router.post("/service").handler(req -> {
      String name = req.getBodyAsJson().getString("name");
      String url = req.getBodyAsJson().getString("url");
      if(serviceCache.containsKey(url)){
        setErrorResponse(req, "URL already exist");
        return;
      }
      JsonArray params = new JsonArray()
                            .add(name)
                            .add(url)
                            .add(LocalDateTime.now().toString());
      saveAppService(params);
      serviceCache.put(url, new AppService(name, url));
      setSuccessResponse(req, Optional.empty());
    });
  }

  private void enablePutServiceRoute(Router router) {
    router.put("/service").handler(req -> {
      String name = req.getBodyAsJson().getString("name");
      String url = req.getBodyAsJson().getString("url");
      JsonArray params = new JsonArray()
              .add(name)
              .add(url);
      appServiceRepository.updateAppService(params);
      serviceCache.put(url, new AppService(name, url));
      setSuccessResponse(req, Optional.empty());
    });
  }

  private void setSuccessResponse(RoutingContext req, Optional<JsonArray> result) {
    req.response()
            .putHeader("content-type", "text/plain");
    if(result.isPresent()){
      req.response().end(result.get().encode());
    }else{
      req.response().end("OK");
    }
  }

  private void setErrorResponse(RoutingContext req, String message) {
    req.response().setStatusCode(HttpResponseStatus.CONFLICT.code()).end(message);
  }

  public void deleteAppServiceByUrl(JsonArray params) {
    appServiceRepository.deleteAppServiceByUrl(params);
  }


  public void saveAppService(JsonArray params) {
    appServiceRepository.saveAppService(params);
  }

  private void loadServiceCache() {
    Future<ResultSet> services = appServiceRepository.getAllServices();
    Map<String, AppService> result = new HashMap<>();
    services.setHandler(done -> {
      if (done.succeeded()) {
        System.out.println(done.result().getResults() + "********done.result().getResults()");
        done.result().getResults()
                .forEach(json -> result.put(json.getString(0), convertResultToAppService(json)));
      } else {
        System.out.println(done.cause().getStackTrace());
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