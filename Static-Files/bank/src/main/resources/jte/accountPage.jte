@import java.util.Map
@import edu.cwi.UserAccount

@param String title
@param UserAccount account
@param Map<String, String> amounts
@param double total

@template.jte.accountLayout(
    title = title,
    account = @`@template.jte.userAccount(account.name(), account.formatBalance())`,
    detail = @`@template.jte.transactionsList(account.transactions(), amounts, total)`
)
