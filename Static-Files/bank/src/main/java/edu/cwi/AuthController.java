package edu.cwi;

import io.javalin.http.Context;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class AuthController {

  record Pair(String a, String b) {}

  private static final Map<Pair, UserAccount> userMap =
      Map.of(
          new Pair("ajones", "weak"),
          new UserAccount(
              "Alice1",
              "Alice Jones",
              AppRole.CUSTOMER,
              750.00,
              Map.of("1", "Deposited $750.00")),
          new Pair("bjones", "weak"),
          new UserAccount(
              "Bob1",
              "Bob Jones",
              AppRole.CUSTOMER,
              500.00,
              Map.of("1", "Deposited $500.00")));

  public static Optional<UserAccount> getUser(String id) {
    Collection<UserAccount> accounts = userMap.values();

    for (UserAccount account : accounts) {
      if (account.id().equals(id)) {
        return Optional.of(account);
      }
    }
    return Optional.empty();
  }

  public static void authenticate(Context ctx) {
    String username = ctx.formParam("username");
    String password = ctx.formParam("password");

    Pair credentials = new Pair(username, password);
    UserAccount user = userMap.get(credentials);

    if (user != null) {
      ctx.cookieStore().set("user-id", user.id());
      ctx.cookieStore().set("user-role", user.role());
      ctx.redirect("/account");
    } else {
      ctx.cookieStore().clear();
      ctx.redirect("/unauthorized.html");
    }
  }

  public static void logout(Context ctx) {
    ctx.cookieStore().clear();
    ctx.redirect("/");
  }
}
