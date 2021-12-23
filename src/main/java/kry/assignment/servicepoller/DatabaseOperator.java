package kry.assignment.servicepoller;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseOperator {
    private final Logger LOGGER = LoggerFactory.getLogger(DatabaseOperator.class);

    private final DatabaseConnector connector;

    private final HashMap<String, JsonObject> services = new HashMap<>();
    private static String URL = "url";
    private static String STATUS = "status";
    private static String NAME = "name";
    private static String UNKNOWN = "UNKNOWN";
    private static String QUERY_GET_ALL_SERVICES = "SELECT * FROM kry;";
    private static String QUERY_DELETE_SERVICE = "DELETE FROM kry WHERE url=?";
    private static String QUERY_INSERT_UPDATE_SERVICE =  "INSERT OR REPLACE INTO kry (url, name, last_status) values (?,?,?)";

    public DatabaseOperator(DatabaseConnector connector) {
        this.connector = connector;
    }

    public Future<Boolean> setup() {
        Future<Boolean> setupFuture = Future.future();
        connector.query(QUERY_GET_ALL_SERVICES).setHandler(result -> {
            if (result.succeeded()) {
                result.result().getRows().forEach(row -> services.put(row.getString(URL), row.put(STATUS, UNKNOWN)));
                LOGGER.info("Services already present :" + services.keySet());
                setupFuture.complete(true);
            } else {
                LOGGER.error("Error while connecting database", result.cause());
                setupFuture.fail(result.cause());
            }
        });
        return setupFuture;
    }
    public Future<ResultSet> insertOrUpdate(JsonObject service) {
        services.put(service.getString(URL), service);
        return connector.query(QUERY_INSERT_UPDATE_SERVICE,
                new JsonArray()
                        .add(service.getString(URL))
                        .add(service.getString(NAME))
                        .add("UNKNOWN")
        );
    }


    public Future<ResultSet> delete(String service) {
        services.remove(service);
        return connector.query(QUERY_DELETE_SERVICE, new JsonArray().add(service));
    }
    public List<JsonObject> getAllServices() {
        return new ArrayList<>(services.values());
    }

}
