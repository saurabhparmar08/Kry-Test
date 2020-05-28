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

public class MainVerticle extends AbstractVerticle {

  private AppServiceHelper appServiceHelper;
  private DBMigration migration;
  private final BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    appServiceHelper = new AppServiceHelper(new DBConnector(vertx));
    appServiceHelper.loadServiceCache();
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    setRoutes(router);
    configureServer(startFuture, router);
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
    router.get("/service").handler(req -> setSuccessResponse(req, new JsonArray(new ArrayList<>(appServiceHelper.getServiceCache().values()))));
  }

  private void enableGetPollServiceRoute(Router router) {
    router.get("/poll").handler(req -> {
      List<AppService> services = new ArrayList<>(appServiceHelper.getServiceCache().values());
      poller.pollServicesAndUpdateCache(services);
      setSuccessResponse(req, new JsonArray(services));
    });
  }

  private void enableDeleteServiceRouteAsync(Router router) {
    router.delete("/serviceAsync").handler(req -> {
      String url = req.getBodyAsJson().getString("url");
      JsonArray params = new JsonArray().add(url);
      appServiceHelper.deleteAppServiceByUrl(params);
      appServiceHelper.getServiceCache().remove(req.getBodyAsJson().getString("url"));
      setSuccessResponse(req);
    });
  }

  private void enableDeleteServiceRoute(Router router) {
    router.delete("/service").handler(req -> appServiceHelper.deleteAppServiceByUrl(req));
  }


  private void enablePostServiceRouteAsync(Router router) {
    router.post("/serviceAsync").handler(req -> {
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
      setSuccessResponse(req);
    });
  }

  private void enablePostServiceRoute(Router router) {
    router.post("/service").handler(req -> {
      if(appServiceHelper.getServiceCache().containsKey(req.getBodyAsJson().getString("url"))){
        setErrorResponse(req, "URL already exist");
        return;
      }
      appServiceHelper.saveAppService(req);
    });
  }

  private void enablePutServiceRouteAsync(Router router) {
    router.put("/serviceAsync").handler(req -> {
      String name = req.getBodyAsJson().getString("name");
      String url = req.getBodyAsJson().getString("url");
      JsonArray params = new JsonArray()
              .add(name)
              .add(url);
      appServiceHelper.updateAppService(params);
      appServiceHelper.getServiceCache().put(url, new AppService(name, url));
      setSuccessResponse(req);
    });
  }

  private void enablePutServiceRoute(Router router) {
    router.put("/service").handler(req -> appServiceHelper.updateAppService(req));
  }

  private void setSuccessResponse(RoutingContext req, JsonArray result) {
    req.response()
            .putHeader("content-type", "application/json");
      req.response().end(result.encode());
  }

  private void setSuccessResponse(RoutingContext req) {
    req.response()
            .putHeader("content-type", "text/plain");
      req.response().end("OK");
  }

  private void setErrorResponse(RoutingContext req, String message) {
    req.response().setStatusCode(HttpResponseStatus.CONFLICT.code()).end(message);
  }
}