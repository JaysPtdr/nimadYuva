package NimadYuva.Registration;

import io.netty.util.internal.StringUtil;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;




public class SignUpAPI {
  public static void attach(Router parent, Vertx vertx){
    parent.post("/signUp").handler(context -> {

      if (context.request().method() == HttpMethod.POST ) {
        String requestBody = context.body().asString();
        if(StringUtil.isNullOrEmpty(requestBody)){
          // Handle other HTTP methods or paths
          context.response().setStatusCode(404).end();
        }
        else{
          JsonObject requestBodyAsJson = context.body().asJsonObject();
          try{
            String mobNum = requestBodyAsJson.getString("mobNum");
            String password = requestBodyAsJson.getString("password");
            Tuple data = Tuple.of(mobNum, password);
            MySQLConnectOptions connectOptions = new MySQLConnectOptions()
              .setPort(3306)
              .setHost("youngyouthmission.c5ey0eg44uss.us-east-1.rds.amazonaws.com")
              .setDatabase("young_youth_db")
              .setUser("admin")
              .setPassword("youngyouth!2020");

            // MySQL pool options
            PoolOptions poolOptions = new PoolOptions()
              .setMaxSize(5); // Set the maximum pool size

            // Create the client pool
            MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);

            client.getConnection(ar -> {
              SqlConnection connection = ar.result();
              // Perform queries
              connection
                .preparedQuery("INSERT INTO user_credentials (mobNum, password) VALUES (?, ?)")
                .execute(data, res -> {
                  try{
                    RowSet<Row> result = res.result();
                    System.out.println("Rows: " + result.rowCount());
                    // Process the rows
                    for (Row row : result) {
                      System.out.println(row);
                    }
                    // Send a response
                    context.response()
                      .putHeader("content-type", "application/json")
                      .end(new JsonObject().put("message", "Data added Successfully").put("success",true).encode());

                    connection.close();
                  }
                  catch (Exception e){
                    context.response().setStatusCode(404).end(new JsonObject().put("error", e.toString()).put("success", false).encode());
                  }
                });
            });
          }catch(Exception e){
            context.response().setStatusCode(404).end(new JsonObject().put("error", e.toString()).put("success", false).encode());
          }
        }
      }
    });
  }
}
