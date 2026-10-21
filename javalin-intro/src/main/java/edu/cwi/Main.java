package edu.cwi;

import io.javalin.Javalin;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {
        Javalin.create(config -> {

            config.routes.get("/hello/{name}", ctx -> {
                String name = ctx.pathParam("name");
                ctx.result("Hello, " + name);
            });

            config.routes.get("/hello/{name}/html", ctx -> {
                String name = ctx.pathParam("name");

                String html = """
                        <html>
                            <head>
                                <title>Javalin Intro</title>
                            </head>
                            <body>
                                <h1>Hello, {name}</h1>
                            </body>
                        </html>
                        """;
                html = html.replace("{name}", name);
                ctx.html(html);
            });

        }).start(7070);

    }
}
