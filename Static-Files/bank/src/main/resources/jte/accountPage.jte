@import java.util.Map
@import edu.cwi.UserAccount

@param String title
@param UserAccount account
@param int accountNumber
@param Map<String, String> amounts
@param double total

@template.jte.accountLayout(
    title = title,
    account = @`@template.jte.userAccount(account.name(), account.formatBalance(), accountNumber)`,
    detail = @`
        @template.jte.transactionsList(account.transactions(), amounts, total)
        <a href="/accounts" class="back-to-account">Back to accounts</a>
    `
)
