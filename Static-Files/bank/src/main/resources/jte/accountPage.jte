@import java.util.Map
@import java.math.BigDecimal
@import edu.cwi.UserAccount
@import edu.cwi.data.Transaction

@param String title
@param UserAccount account
@param int accountNumber
@param Map<String, String> amounts
@param BigDecimal total
@param Transaction editing = null

@template.jte.accountLayout(
    title = title,
    account = @`@template.jte.userAccount(account.name(), account.formatBalance(), accountNumber)`,
    detail = @`
        @template.jte.transactionsList(account.transactions(), amounts, total, accountNumber)
        @template.jte.transactionForm(accountNumber, editing)
        <a href="/accounts" class="back-to-account">Back to accounts</a>
    `
)
