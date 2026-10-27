package edu.cwi;

import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class App {

  public static void main(String[] args) {
    Javalin.create(
            config -> {
              config.staticFiles.add("/public", Location.CLASSPATH);

              config.routes.post(
                  "/account",
                  ctx -> {
                    ctx.redirect("/account.html");
                  });

              config.routes.get(
                  "/logout",
                  ctx -> {
                    ctx.redirect("/");
                  });
              config.routes.apiBuilder(
                  () -> {
                    path(
                        "/transactions",
                        () -> {
                          crud("/{id}", new TransactionsController());
                        });
                  });
            })
        .start(7070);
  }
}
