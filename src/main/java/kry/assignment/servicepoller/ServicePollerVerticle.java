package kry.assignment.servicepoller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ServicePollerVerticle extends AbstractVerticle {

    private HashMap<String, String> services = new HashMap<>();

    private AutoServicePoller poller;
    private DatabaseOperator databaseOperator;
    private Logger LOGGER = LoggerFactory.getLogger(ServicePollerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        poller = new AutoServicePoller(vertx);
        databaseOperator = new DatabaseOperator(new DatabaseConnector(vertx));
        databaseOperator.setup().setHandler(result -> {
            if (result.succeeded()) {
                vertx.setPeriodic(60000, timerId -> poller.pollServices(databaseOperator.getAllServices()));
                configureRoutes(router);
                vertx
                        .createHttpServer()
                        .requestHandler(router)
                        .listen(8080, r -> {
                            if (r.succeeded()) {
                                LOGGER.info("KRY code test service started");
                                startFuture.complete();
                            } else {
                                startFuture.fail(r.cause());
                            }
                        });
            } else {
                startFuture.fail(result.cause());
            }
        });

    }

    private void configureRoutes(Router router) {
        configureStaticHandlerRoute(router);
        configureGetServices(router);
        configureInsertService(router);
        configureDeleteService(router);
    }

    private void configureStaticHandlerRoute(Router router) {
        router.route("/*").handler(StaticHandler.create());
    }

    private void configureGetServices(Router router) {
        router.get("/service").handler(req -> {
            LOGGER.info("Fetching all services");
            List<JsonObject> jsonServices = databaseOperator.getAllServices();
            sendResponse(req,new JsonArray(jsonServices).encode(),"application/json");
        });
    }

    private void sendResponse(RoutingContext routingContext,String obj,String contentType){
        routingContext.response()
                .putHeader("content-type", "application/json")
                .end(obj);
    }

    private void sendErrorResponse(RoutingContext routingContext,String obj,String contentType,int status){
        routingContext.response()
                .setStatusCode(status)
                .putHeader("content-type", "application/json")
                .end(obj);
    }
    private void configureInsertService(Router router) {
        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            JsonObject service;
            try {
                service = createServiceFromReq(jsonBody.getString("url"), jsonBody.getString("name"));
            } catch (MalformedURLException e) {
                sendErrorResponse(req,"Invalid url: " + jsonBody.getString("url"),"text/plain",400);
                return;
            }
            databaseOperator.insertOrUpdate(service).setHandler(asyncResult -> {
                if (asyncResult.succeeded()) {
                    LOGGER.info("Service inserted/updated successfully.");
                    sendResponse(req,"OK","text/plain");
                } else {
                    LOGGER.error("Service insert/update failed:", asyncResult.cause());
                    sendErrorResponse(req,"Internal error","text/plain",500);
                }
            });
        });
    }

    private void configureDeleteService(Router router) {
        router.delete("/delete").handler(req -> {
            try {
                JsonObject jsonBody = req.getBodyAsJson();
                String service =  jsonBody.getString("url");
                LOGGER.info("Deleting service : " + service);
                databaseOperator.delete(service).setHandler(asyncResult -> {
                    if (asyncResult.succeeded()) {
                        LOGGER.info("Delete successful");
                        sendResponse(req,"OK","text/plain");
                    } else {
                        LOGGER.error("Delete failed : ", asyncResult.cause());
                        sendErrorResponse(req,"Internal error","text/plain",500);
                    }
                });
            }catch (Exception e){
                LOGGER.error("Delete failed.");
                sendErrorResponse(req,"Internal error","text/plain",500);
            }
        });
    }



    private JsonObject createServiceFromReq(String url, String name) throws MalformedURLException {
        return new JsonObject()
                .put("url", new URL(url).toString())
                .put("name", name != null ? name : url)
                .put("status", "UNKNOWN");
    }
}
