package edu.cwi.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class MyBatisConfig {

  private static SqlSessionFactory sqlSessionFactory;

  static {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/online_bank");
    hikariConfig.setUsername("root");
    hikariConfig.setPassword(System.getenv("MYSQL_PASSWORD"));
    hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
    DataSource dataSource = new HikariDataSource(hikariConfig);

    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("development", transactionFactory, dataSource);

    Configuration configuration = new Configuration(environment);
    configuration.addMapper(UserMapper.class);
    configuration.addMapper(AccountMapper.class);
    configuration.addMapper(TransactionMapper.class);
    configuration.setMapUnderscoreToCamelCase(true);

    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  public static SqlSessionFactory getSqlSessionFactory() {
    return sqlSessionFactory;
  }
}
