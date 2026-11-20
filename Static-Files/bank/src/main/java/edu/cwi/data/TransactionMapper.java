package edu.cwi.data;

import java.util.List;
import org.apache.ibatis.annotations.*;

public interface TransactionMapper {

  @Select(
      "SELECT transaction_id as id, account_id, transaction_type, amount, description, created_at FROM transactions WHERE account_id = #{accountId} ORDER BY transaction_id")
  List<Transaction> getByAccountId(int accountId);

  @Select(
      "SELECT transaction_id as id, account_id, transaction_type, amount, description, created_at FROM transactions WHERE transaction_id = #{transactionId}")
  Transaction getById(long transactionId);

  @Select(
      "SELECT transaction_id as id, account_id, transaction_type, amount, description, created_at FROM transactions WHERE transaction_id = #{transactionId} FOR UPDATE")
  Transaction getByIdForUpdate(long transactionId);

  @Select(
      "SELECT transaction_id as id, account_id, transaction_type, amount, description, created_at FROM transactions WHERE account_id = #{accountId} ORDER BY transaction_id FOR UPDATE")
  List<Transaction> getByAccountIdForUpdate(int accountId);

  @Insert(
      "INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES (#{accountId}, #{transactionType}, #{amount}, #{description})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(Transaction transaction);

  @Update(
      "UPDATE transactions SET transaction_type = #{transactionType}, amount = #{amount}, description = #{description} WHERE transaction_id = #{id}")
  int update(Transaction transaction);

  @Delete("DELETE FROM transactions WHERE transaction_id = #{transactionId}")
  int delete(long transactionId);
}
