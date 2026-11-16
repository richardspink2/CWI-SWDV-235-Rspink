package edu.cwi;

import static j2html.TagCreator.*;

import edu.cwi.data.Account;
import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.Transaction;
import edu.cwi.data.TransactionMapper;
import edu.cwi.data.User;
import edu.cwi.data.UserMapper;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ibatis.session.SqlSession;

public class TransactionsController implements CrudHandler {

  private final Map<String, String> transactions = new ConcurrentHashMap<>();
  private final Map<String, String> amounts = new ConcurrentHashMap<>();

  @Override
  public void getAll(Context ctx) {
    int userId = getCustomerId(ctx);
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      renderAccount(ctx, session, user, accountMapper.getByUserId(userId));
    }
  }

  public void showAccounts(Context ctx) {
    int userId = getCustomerId(ctx);
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      ctx.render(
          "jte/accountsPage.jte",
          Map.of(
              "name", user.getFirstName() + " " + user.getLastName(),
              "accounts", accountMapper.getAllByUserId(userId)));
    }
  }

  public void showAccount(Context ctx) {
    int userId = getCustomerId(ctx);
    int accountId;
    try {
      accountId = Integer.parseInt(ctx.pathParam("id"));
    } catch (NumberFormatException ex) {
      ctx.status(404).result("Account not found");
      return;
    }

    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      Account bankAccount = accountMapper.getById(accountId);
      if (bankAccount == null || bankAccount.getUserId() != userId) {
        ctx.status(404).result("Account not found");
        return;
      }
      renderAccount(ctx, session, user, bankAccount);
    }
  }

  private int getCustomerId(Context ctx) {
    Object userId = ctx.cookieStore().get("user-id");
    if (!(userId instanceof Integer)
        || !AppRole.CUSTOMER.name().equals(ctx.cookieStore().get("user-role"))) {
      throw new UnauthorizedResponse();
    }
    return (Integer) userId;
  }

  private User getCustomer(SqlSession session, int userId) {
    UserMapper userMapper = session.getMapper(UserMapper.class);
    User user = userMapper.getById(userId);
    if (user == null || !AppRole.CUSTOMER.name().equals(user.getRole())) {
      throw new UnauthorizedResponse();
    }
    return user;
  }

  private void renderAccount(Context ctx, SqlSession session, User user, Account bankAccount) {
    Map<String, String> accountTransactions = new LinkedHashMap<>();
    Map<String, String> accountAmounts = new LinkedHashMap<>();
    double balance = 0;
    int accountNumber = 0;

    if (bankAccount != null) {
      accountNumber = bankAccount.getId();
      balance = bankAccount.getBalance().doubleValue();
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      for (Transaction transaction : transactionMapper.getByAccountId(accountNumber)) {
        String id = String.valueOf(transaction.getId());
        String description = transaction.getDescription();
        if (description == null || description.isBlank()) {
          description = transaction.getTransactionType().name();
        }
        accountTransactions.put(id, description);
        accountAmounts.put(id, transaction.getAmount().toPlainString());
      }
    }

    UserAccount account = new UserAccount(
        String.valueOf(user.getId()),
        user.getFirstName() + " " + user.getLastName(),
        AppRole.CUSTOMER,
        balance,
        accountTransactions);
    ctx.render(
        "jte/accountPage.jte",
        Map.of(
            "title", "Account transactions",
            "account", account,
            "accountNumber", accountNumber,
            "amounts", accountAmounts,
            "total", account.balance()));
  }

  private void renderTransactionsPage(Context ctx) {
    double total = 0;

    for (String amount : amounts.values()) {
      if (!amount.isEmpty()) {
        total += Double.parseDouble(amount);
      }
    }

    ctx.render(
        "jte/transactionsPage.jte",
        Map.of(
            "transactions", transactions,
            "amounts", amounts,
            "total", total));
  }

  public void showTransactions(Context ctx) {
    renderTransactionsPage(ctx);
  }

  @Override
  public void getOne(Context ctx, String resourceId) {
    if ("account".equals(ctx.queryParam("source"))) {
      showAccountTransaction(ctx, resourceId);
      return;
    }

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
                          a("Accounts").withHref("/accounts"),
                          a("Log Out").withHref("/logout")),
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
                          a("All transactions").withHref("/transactions/manage"))))
              .render());
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  private void showAccountTransaction(Context ctx, String resourceId) {
    int userId = getCustomerId(ctx);
    int transactionId;
    try {
      transactionId = Integer.parseInt(resourceId);
    } catch (NumberFormatException ex) {
      ctx.status(404).result("Transaction not found");
      return;
    }

    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      Transaction transaction = transactionMapper.getById(transactionId);
      if (transaction == null) {
        ctx.status(404).result("Transaction not found");
        return;
      }

      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      Account account = accountMapper.getById(transaction.getAccountId());
      if (account == null || account.getUserId() != userId) {
        ctx.status(404).result("Transaction not found");
        return;
      }

      String description = transaction.getDescription();
      if (description == null || description.isBlank()) {
        description = transaction.getTransactionType().name();
      }
      ctx.html(
          html(
                  head(
                      title("Transaction " + resourceId),
                      link().withRel("stylesheet").withHref("/styles.css")),
                  body(
                      nav(
                          attrs(".site-nav"),
                          a("Home").withHref("/"),
                          a("Accounts").withHref("/accounts"),
                          a("Log Out").withHref("/logout")),
                      main(
                          attrs("#main.content"),
                          h1("Transaction " + resourceId),
                          p(description),
                          p(String.format("Amount: $%,.2f", transaction.getAmount())),
                          a("Back to account").withHref("/account/" + account.getId()))))
              .render());
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

    renderTransactionsPage(ctx);
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

      renderTransactionsPage(ctx);
    } else {
      ctx.status(404).result("Transaction not found");
    }
  }

  @Override
  public void delete(Context ctx, String resourceId) {

    transactions.remove(resourceId);
    amounts.remove(resourceId);

    renderTransactionsPage(ctx);
  }
}
