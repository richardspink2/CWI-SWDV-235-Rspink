package edu.cwi;

import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;

public class App {

  public static void main(String[] args) {
    TransactionsController controller = new TransactionsController();

    Javalin.create(
            config -> {
              config.staticFiles.add("/public", Location.CLASSPATH);
              config.fileRenderer(new JavalinJte());

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
              config.routes.apiBuilder(() -> {
                path(
                    "/transactions",
                    () -> {
                      crud("/{id}", controller);
                      post("/{id}/update", ctx -> controller.update(ctx, ctx.pathParam("id")));
                      post("/{id}/delete", ctx -> controller.delete(ctx, ctx.pathParam("id")));
                    });
              });
            })
        .start(7070);
  }
}
