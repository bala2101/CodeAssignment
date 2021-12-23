package kry.assignment.servicepoller;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

public class DatabaseConnector {
    private final SQLClient client;

    public DatabaseConnector(Vertx vertx){
        JsonObject config = new JsonObject()
                .put("url", "jdbc:sqlite:servicepoller.db")
                .put("driver_class", "org.sqlite.JDBC")
                .put("max_pool_size", 30);

        client = JDBCClient.createShared(vertx, config);
    }

    public void close() {
        client.close();
    }

    public Future<ResultSet> query(String query) {
        return query(query, new JsonArray());
    }

    public Future<ResultSet> query(String query, JsonArray params) {
        if(query == null || query.isEmpty()) {
            return Future.failedFuture("Query is null or empty");
        }
        if(!query.endsWith(";")) {
            query = query + ";";
        }

        Future<ResultSet> queryResultFuture = Future.future();

        client.queryWithParams(query, params, result -> {
            if(result.failed()){
                queryResultFuture.fail(result.cause());
            } else {
                queryResultFuture.complete(result.result());
            }
        });
        return queryResultFuture;
    }


}
