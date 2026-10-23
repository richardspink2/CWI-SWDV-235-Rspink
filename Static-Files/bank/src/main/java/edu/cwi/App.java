package edu.cwi;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.routes.post("/account", ctx -> {
                ctx.redirect("/account.html");
            });
            config.routes.get("/logout", ctx -> {
                ctx.redirect("/");
            });
        }).start(7070);
    }
}
