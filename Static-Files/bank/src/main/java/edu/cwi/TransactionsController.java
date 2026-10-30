package edu.cwi;

import static j2html.TagCreator.*;

import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionsController implements CrudHandler {

  private final Map<String, String> transactions = new ConcurrentHashMap<>();
  private final Map<String, String> amounts = new ConcurrentHashMap<>();

  @Override
  public void getAll(Context ctx) {
    double total = 0;

    for (String amount : amounts.values()) {
      if (!amount.isEmpty()) {
        total += Double.parseDouble(amount);
      }
    }
    ctx.render(
        "jte/transactionsPage.jte",
        Map.of("transactions", transactions, "amounts", amounts, "total", total));
  }

  @Override
  public void getOne(Context ctx, String resourceId) {

    String user = transactions.get(resourceId);

    if (user != null) {
      ctx.html(
          html(
                  head(
                      title("Transaction " + resourceId),
                      link().withRel("stylesheet").withHref("/styles.css")),
                  body(
                      nav(
                          attrs(".site-nav"),
                          a("Home").withHref("/"),
                          a("Account").withHref("/account.html"),
                          a("Transactions").withHref("/transactions")),
                      main(
                          attrs("#main.content"),
                          h1("Transaction " + resourceId),
                          p(user),
                          p("Amount: $ " + amounts.get(resourceId)),
                          h2("Update transaction"),
                          form()
                              .withMethod("post")
                              .withAction("/transactions/" + resourceId + "/update")
                              .with(
                                  label("Description").withFor("update-description"),
                                  input()
                                      .withType("text")
                                      .withId("update-description")
                                      .withName("description")
                                      .withValue(user)
                                      .isRequired(),
                                  label("Amount").withFor("update-amount"),
                                  input()
                                      .withType("number")
                                      .withId("update-amount")
                                      .withName("amount")
                                      .withValue(amounts.get(resourceId))
                                      .isRequired(),
                                  button("Update transaction")
                                      .withType("submit")
                                      .withClass("transactions-button")),
                          form()
                              .withMethod("post")
                              .withAction("/transactions/" + resourceId + "/delete")
                              .with(
                                  button("Delete transaction")
                                      .withType("submit")
                                      .withClass("transactions-button")),
                          a("All transactions").withHref("/transactions"))))
              .render());
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  @Override
  public void create(Context ctx) {

    String body = ctx.formParam("description");
    if (body == null) {
      body = ctx.body();
    }
    String amount = ctx.formParam("amount");
    if (amount == null) {
      amount = "";
    }
    String id = String.valueOf(transactions.size() + 1);

    transactions.put(id, body);
    amounts.put(id, amount);

    getAll(ctx);
  }

  @Override
  public void update(Context ctx, String resourceId) {

    if (transactions.containsKey(resourceId)) {
      String body = ctx.formParam("description");
      if (body == null) {
        body = ctx.body();
      }
      transactions.put(resourceId, body);
      String amount = ctx.formParam("amount");
      if (amount != null) {
        amounts.put(resourceId, amount);
      }

      getAll(ctx);
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  @Override
  public void delete(Context ctx, String resourceId) {

    transactions.remove(resourceId);
    amounts.remove(resourceId);

    getAll(ctx);
  }
}
