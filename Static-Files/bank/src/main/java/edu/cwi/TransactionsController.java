package edu.cwi;

import static j2html.TagCreator.*;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionsController implements CrudHandler {

  private final Map<String, String> transactions = new ConcurrentHashMap<>();

  @Override
  public void getAll(Context ctx) {
    ctx.html(
        html(
                head(title("Bank Transactions")),
                body(
                    main(
                        attrs("#main.content"),
                        h1("All Transactions"),
                        ul(
                            each(
                                transactions.entrySet(),
                                transaction ->
                                    li(
                                        a("Transaction " + transaction.getKey())
                                            .withHref("/transactions/" + transaction.getKey()),
                                        p(transaction.getValue())))))))
            .render());
  }

  @Override
  public void getOne(Context ctx, String resourceId) {

    String user = transactions.get(resourceId);

    if (user != null) {
      ctx.html(
          html(
                  head(title("Transaction " + resourceId)),
                  body(
                      h1("Transaction " + resourceId),
                      p(user),
                      a("All transactions").withHref("/transactions")))
              .render());
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  @Override
  public void create(Context ctx) {

    String body = ctx.body();
    String id = String.valueOf(transactions.size() + 1);

    transactions.put(id, body);

    getAll(ctx);
  }

  @Override
  public void update(Context ctx, String resourceId) {

    if (transactions.containsKey(resourceId)) {
      transactions.put(resourceId, ctx.body());

      getAll(ctx);
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  @Override
  public void delete(Context ctx, String resourceId) {

    transactions.remove(resourceId);

    getAll(ctx);
  }
}
