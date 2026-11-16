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
import java.math.BigDecimal;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.mindrot.jbcrypt.BCrypt;

public class UsersController implements CrudHandler {

  @Override
  public void getAll(Context ctx) {
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      UserMapper mapper = session.getMapper(UserMapper.class);
      ctx.json(mapper.getAllUsers());
    }
  }

  @Override
  public void getOne(Context ctx, String resourceId) {
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      UserMapper mapper = session.getMapper(UserMapper.class);
      User user = mapper.getById(Integer.parseInt(resourceId));
      if (user == null) {
        ctx.status(404).json(Map.of("error", "User not found."));
        return;
      }
      ctx.json(user);
    }
  }

  @Override
  public void create(Context ctx) {
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      UserMapper userMapper = session.getMapper(UserMapper.class);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      TransactionMapper transactionMapper = session.getMapper(TransactionMapper.class);

      User user = ctx.bodyAsClass(User.class);
      BigDecimal deposit = BigDecimal.valueOf(5.00);

      String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
      user.setPassword(hashedPassword);

      userMapper.insert(user);

      Account account = new Account();
      account.setUserId(user.getId());
      account.setBalance(deposit);
      accountMapper.insert(account);

      Transaction transaction = new Transaction();
      transaction.setAccountId(account.getId());
      transaction.setTransactionType(TransactionType.DEPOSIT);
      transaction.setAmount(deposit);
      transaction.setDescription("Opening deposit.");
      transactionMapper.insert(transaction);

      session.commit();
      user.setPassword("");
      ctx.json(user);
    }
  }

  @Override
  public void update(Context ctx, String resourceId) {
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      User user = ctx.bodyAsClass(User.class);
      user.setId(Integer.parseInt(resourceId));
      user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
      UserMapper mapper = session.getMapper(UserMapper.class);
      if (mapper.update(user) == 0) {
        ctx.status(404).json(Map.of("error", "User not found."));
        return;
      }
      session.commit();
      user.setPassword("");
      ctx.json(user);
    }
  }

  @Override
  public void delete(Context ctx, String resourceId) {
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      int id = Integer.parseInt(resourceId);
      AccountMapper accountMapper = session.getMapper(AccountMapper.class);
      boolean deleted = false;
      if (accountMapper.getByUserId(id) == null) {
        UserMapper mapper = session.getMapper(UserMapper.class);
        deleted = mapper.delete(id) > 0;
        session.commit();
      }
      ctx.json(Map.of("id", resourceId, "deleted", deleted));
    }
  }

  public User authenticateUser(Context ctx, String username, String password) {
    if (username == null || password == null) {
      return null;
    }
    try (SqlSession session = MyBatisConfig.getSqlSessionFactory().openSession()) {
      UserMapper mapper = session.getMapper(UserMapper.class);
      User user = mapper.getByUsername(username);

      try {
        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
          return user;
        }
      } catch (IllegalArgumentException ex) {
        // The SQL seed uses a placeholder that is not a valid bcrypt hash.
        return null;
      }

      return null;
    }
  }
}
