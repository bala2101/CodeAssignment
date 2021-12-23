package kry.assignment.servicepoller;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class AutoServicePoller {
    private final WebClient client;
    private Logger LOGGER = LoggerFactory.getLogger(AutoServicePoller.class);
    private static String URL = "url";
    private static String STATUS = "status";
    private static String OK = "OK";
    private static String FAIL = "FAIL";

    public AutoServicePoller(Vertx vertx) {
        client = WebClient.create(vertx);
    }

    public List<Future<JsonObject>> pollServices(List<JsonObject> services) {
        return services.parallelStream().map(this::checkService).collect(Collectors.toList());
    }

    private Future<JsonObject> checkService(JsonObject service) {
        String url = service.getString(URL);
        LOGGER.info("Checking service with url : " + url);
        Future<JsonObject> statusFuture = Future.future();
        try {
            client.getAbs(url)
                    .send(response -> {
                        if (response.succeeded()) {
                            statusFuture.complete(service.put(STATUS, 200 == response.result().statusCode() ? OK : FAIL));
                        } else {
                            statusFuture.complete(service.put(STATUS, FAIL));
                        }
                    });
        } catch (Exception e) {
            LOGGER.error("Service check failed " + url, e);
            statusFuture.complete(service.put(STATUS, FAIL));
        }
        return statusFuture;
    }
}
