package edu.cwi.data;

import java.util.List;
import org.apache.ibatis.annotations.*;

public interface UserMapper {

  @Select(
      "SELECT user_id as id, username, '' as password, email, first_name, last_name, role FROM users")
  List<User> getAllUsers();

  @Select(
      "SELECT user_id as id, username, '' as password, email, first_name, last_name, role FROM users WHERE user_id = #{id}")
  User getById(int id);

  @Select(
      "SELECT user_id as id, username, password, email, first_name, last_name, role FROM users WHERE username = #{username}")
  User getByUsername(String username);

  @Insert(
      "INSERT INTO users (username, password, email, first_name, last_name, role) VALUES (#{username}, #{password}, #{email}, #{firstName}, #{lastName}, #{role})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  int insert(User user);

  @Update(
      "UPDATE users SET username = #{username}, password = #{password}, email = #{email}, first_name = #{firstName}, last_name = #{lastName}, role = #{role} WHERE user_id = #{id}")
  int update(User user);

  @Delete("DELETE FROM users WHERE user_id = #{id}")
  int delete(int id);
}
