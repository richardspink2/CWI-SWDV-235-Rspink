package edu.cwi;

import static io.javalin.apibuilder.ApiBuilder.*;

import io.javalin.Javalin;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import java.util.List;

public class App {

  public static void main(String[] args) {
    TransactionsController controller = new TransactionsController();

    Javalin.create(
            config -> {
              config.staticFiles.add("/public", Location.CLASSPATH);
              config.fileRenderer(new JavalinJte());
              config.routes.beforeMatched(ctx -> {
                if (ctx.routeRoles().isEmpty()) {
                  return;
                }
                String role = ctx.cookieStore().get("user-role");

                if (role == null) {
                  throw new UnauthorizedResponse();
                }
                AppRole appRole = AppRole.valueOf(role);
                if (!ctx.routeRoles().contains(appRole)) {
                  throw new UnauthorizedResponse();
                }
              });

              config.routes.post("/account", AuthController::authenticate);

              config.routes.get("/logout", AuthController::logout);
              config.routes.apiBuilder(() -> {
                path(
                    "/account",
                    List.of(AppRole.CUSTOMER),
                    () -> {
                      get(controller::getAll);
                    });
                path(
                    "/transactions",
                    () -> {
                      get("/manage", controller::showTransactions);
                      crud("/{id}", controller);
                      post("/{id}/update", ctx -> controller.update(ctx, ctx.pathParam("id")));
                      post("/{id}/delete", ctx -> controller.delete(ctx, ctx.pathParam("id")));
                    });
              });
            })
        .start(7070);
  }
}
