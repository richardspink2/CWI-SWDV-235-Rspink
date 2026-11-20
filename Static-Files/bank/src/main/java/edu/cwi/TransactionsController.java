package edu.cwi;

import edu.cwi.data.Account;
import edu.cwi.data.AccountMapper;
import edu.cwi.data.MyBatisConfig;
import edu.cwi.data.Transaction;
import edu.cwi.data.TransactionMapper;
import edu.cwi.data.TransactionType;
import edu.cwi.data.User;
import edu.cwi.data.UserMapper;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;

public class TransactionsController implements CrudHandler {

  private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999.99");

  @Override
  public void getAll(Context ctx) {
    getCustomerId(ctx);
    ctx.redirect("/accounts");
  }

  public void showAccounts(Context ctx) {
    int userId = getCustomerId(ctx);
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      List<Account> accounts = accountMapper.getAllByUserId(userId);
      for (Account account : accounts) {
        List<Transaction> transactions = transactionMapper.getByAccountId(account.getId());
        if (!transactions.isEmpty()) {
          account.setBalance(transactionTotal(transactions));
        }
      }
      ctx.render(
          "jte/accountsPage.jte",
          Map.of(
              "name", user.getFirstName() + " " + user.getLastName(),
              "accounts", accounts));
    }
  }

  public void showAccount(Context ctx) {
    int userId = getCustomerId(ctx);
    int accountId;
    try {
      accountId = Integer.parseInt(ctx.pathParam("id"));
    } catch (NumberFormatException ex) {
      throw new NotFoundResponse("Account not found");
    }

    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      Account account = accountMapper.getById(accountId);
      if (account == null || account.getUserId() != userId) {
        throw new NotFoundResponse("Account not found");
      }

      Transaction editing = null;
      String editId = ctx.queryParam("edit");
      if (editId != null) {
        TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
        editing = transactionMapper.getById(getTransactionId(editId));
        if (editing == null || editing.getAccountId() != accountId) {
          throw new NotFoundResponse("Transaction not found");
        }
      }
      renderAccount(ctx, session, user, account, editing);
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

  private void renderAccount(
      Context ctx, SqlSession session, User user, Account bankAccount, Transaction editing) {
    TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
    List<Transaction> transactions = transactionMapper.getByAccountId(bankAccount.getId());
    BigDecimal balance = bankAccount.getBalance();
    if (!transactions.isEmpty()) {
      balance = transactionTotal(transactions);
    }

    Map<String, String> descriptions = new LinkedHashMap<>();
    Map<String, String> amounts = new LinkedHashMap<>();
    for (Transaction transaction : transactions) {
      String id = String.valueOf(transaction.getId());
      String description = transaction.getDescription();
      if (description == null || description.isBlank()) {
        description = transaction.getTransactionType().name();
      }
      descriptions.put(id, description);
      amounts.put(id, transaction.getSignedAmount().toPlainString());
    }

    UserAccount account = new UserAccount(
        String.valueOf(user.getId()),
        user.getFirstName() + " " + user.getLastName(),
        AppRole.CUSTOMER,
        balance.doubleValue(),
        descriptions);
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("title", "Account transactions");
    model.put("account", account);
    model.put("accountNumber", bankAccount.getId());
    model.put("amounts", amounts);
    model.put("total", balance);
    if (editing != null) {
      model.put("editing", editing);
    }
    ctx.render("jte/accountPage.jte", model);
  }

  public void showTransactions(Context ctx) {
    getAll(ctx);
  }

  @Override
  public void getOne(Context ctx, String resourceId) {
    int userId = getCustomerId(ctx);
    long transactionId = getTransactionId(resourceId);
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      getCustomer(session, userId);
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      Transaction transaction = transactionMapper.getById(transactionId);
      if (transaction == null) {
        throw new NotFoundResponse("Transaction not found");
      }
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      Account account = accountMapper.getById(transaction.getAccountId());
      if (account == null || account.getUserId() != userId) {
        throw new NotFoundResponse("Transaction not found");
      }
      ctx.redirect(
          "/account/" + account.getId() + "?edit=" + transactionId + "#transaction-form");
    }
  }

  public void createAccountTransaction(Context ctx) {
    create(ctx);
  }

  @Override
  public void create(Context ctx) {
    int userId = getCustomerId(ctx);
    Transaction transaction = readTransaction(ctx);
    if (transaction == null) {
      return;
    }
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      getCustomer(session, userId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      Account account = accountMapper.getByIdForUpdate(transaction.getAccountId());
      if (account == null || account.getUserId() != userId) {
        throw new NotFoundResponse("Account not found");
      }
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      transactionMapper.insert(transaction);
      if (!saveBalance(ctx, session, account.getId())) {
        return;
      }
      session.commit();
      ctx.redirect("/account/" + account.getId());
    }
  }

  @Override
  public void update(Context ctx, String resourceId) {
    int userId = getCustomerId(ctx);
    long transactionId = getTransactionId(resourceId);
    Transaction transaction = readTransaction(ctx);
    if (transaction == null) {
      return;
    }
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      Transaction original = getOwnedTransactionForUpdate(session, transactionId, userId);
      if (transaction.getAccountId() != original.getAccountId()) {
        ctx.status(400).result("Update this transaction from its account page.");
        return;
      }
      transaction.setId(transactionId);
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      transactionMapper.update(transaction);
      if (!saveBalance(ctx, session, original.getAccountId())) {
        return;
      }
      session.commit();
      ctx.redirect("/account/" + original.getAccountId());
    }
  }

  @Override
  public void delete(Context ctx, String resourceId) {
    int userId = getCustomerId(ctx);
    long transactionId = getTransactionId(resourceId);
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      Transaction transaction = getOwnedTransactionForUpdate(session, transactionId, userId);
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
      transactionMapper.delete(transactionId);
      if (!saveBalance(ctx, session, transaction.getAccountId())) {
        return;
      }
      session.commit();
      ctx.redirect("/account/" + transaction.getAccountId());
    }
  }

  private long getTransactionId(String resourceId) {
    try {
      return Long.parseLong(resourceId);
    } catch (NumberFormatException ex) {
      throw new NotFoundResponse("Transaction not found");
    }
  }

  private Transaction getOwnedTransactionForUpdate(
      SqlSession session, long transactionId, int userId) {
    getCustomer(session, userId);
    TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
    Transaction original = transactionMapper.getById(transactionId);
    if (original == null) {
      throw new NotFoundResponse("Transaction not found");
    }

    AccountMapper accountMapper = session.getMapper(AccountMapper.class);
    Account account = accountMapper.getByIdForUpdate(original.getAccountId());
    if (account == null || account.getUserId() != userId) {
      throw new NotFoundResponse("Transaction not found");
    }
    Transaction current = transactionMapper.getByIdForUpdate(transactionId);
    if (current == null) {
      throw new NotFoundResponse("Transaction not found");
    }
    return current;
  }

  private Transaction readTransaction(Context ctx) {
    String amountText = ctx.formParam("amount");
    String typeText = ctx.formParam("transactionType");
    if (amountText == null || typeText == null) {
      ctx.status(400).result("Enter a transaction type and amount.");
      return null;
    }
    Transaction transaction = new Transaction();
    try {
      transaction.setAccountId(Integer.parseInt(ctx.formParam("accountId")));
      transaction.setAmount(new BigDecimal(amountText));
      transaction.setTransactionType(TransactionType.valueOf(typeText));
    } catch (IllegalArgumentException ex) {
      ctx.status(400).result("Enter a valid account, transaction type, and amount.");
      return null;
    }

    BigDecimal amount = transaction.getAmount();
    if (amount.compareTo(BigDecimal.ZERO) <= 0
        || amount.scale() > 2
        || amount.compareTo(MAX_AMOUNT) > 0) {
      ctx.status(400)
          .result("Enter a positive amount up to 9999999999999.99 with at most two decimal places.");
      return null;
    }
    String description = ctx.formParam("description");
    if (description != null && description.length() > 255) {
      ctx.status(400).result("Use a description of 255 characters or fewer.");
      return null;
    }
    transaction.setDescription(description);
    return transaction;
  }

  private BigDecimal transactionTotal(List<Transaction> transactions) {
    BigDecimal total = BigDecimal.ZERO;
    for (Transaction transaction : transactions) {
      total = total.add(transaction.getSignedAmount());
    }
    return total;
  }

  private boolean saveBalance(Context ctx, SqlSession session, int accountId) {
    TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);
    BigDecimal balance = transactionTotal(transactionMapper.getByAccountIdForUpdate(accountId));
    if (balance.abs().compareTo(MAX_AMOUNT) > 0) {
      ctx.status(400).result("The resulting balance is outside the account's supported range.");
      return false;
    }
    Account account = new Account();
    account.setId(accountId);
    account.setBalance(balance);
    AccountMapper accountMapper = session.getMapper(AccountMapper.class);
    accountMapper.updateBalance(account);
    return true;
  }
}
