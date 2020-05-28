package se.kry.codetest;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import se.kry.codetest.migrate.DBMigration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainVerticle extends AbstractVerticle {

  private AppServiceHelper appServiceHelper;
  private DBMigration migration;
  private final BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    appServiceHelper = new AppServiceHelper(new DBConnector(vertx));
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    setRoutes(router);
    configureServer(startFuture, router);
    appServiceHelper.loadServiceCache();
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
    router.get("/service").handler(req -> setSuccessResponse(req, Optional.of(new JsonArray(new ArrayList<>(appServiceHelper.getServiceCache().values())))));
  }

  private void enableGetPollServiceRoute(Router router) {
    router.get("/poll").handler(req -> {
      List<AppService> services = new ArrayList<>(appServiceHelper.getServiceCache().values());
      poller.pollServicesAndUpdateCache(services);
      setSuccessResponse(req, Optional.of(new JsonArray(services)));
    });
  }

  private void enableDeleteServiceRoute(Router router) {
    router.delete("/service").handler(req -> {
      String url = req.getBodyAsJson().getString("url");
      JsonArray params = new JsonArray().add(url);
      appServiceHelper.deleteAppServiceByUrl(params);
      appServiceHelper.getServiceCache().remove(url);
      setSuccessResponse(req, Optional.empty());
    });
  }

  private void enablePostServiceRoute(Router router) {
    router.post("/service").handler(req -> {
      String name = req.getBodyAsJson().getString("name");
      String url = req.getBodyAsJson().getString("url");
      if(appServiceHelper.getServiceCache().containsKey(url)){
        setErrorResponse(req, "URL already exist");
        return;
      }
      JsonArray params = new JsonArray()
                            .add(name)
                            .add(url)
                            .add(LocalDateTime.now().toString());
      appServiceHelper.saveAppService(params);
      appServiceHelper.getServiceCache().put(url, new AppService(name, url));
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
      appServiceHelper.updateAppService(params);
      appServiceHelper.getServiceCache().put(url, new AppService(name, url));
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





}