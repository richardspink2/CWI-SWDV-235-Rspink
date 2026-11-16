@import edu.cwi.data.Account
@import java.util.List

@param String name
@param List<Account> accounts

@template.jte.accountLayout(
    title = "Accounts",
    account = @`
        <h1>Accounts</h1>
        <p><strong>Name:</strong> ${name}</p>
    `,
    detail = @`
        <table class="transactions-table">
            <thead>
                <tr>
                    <th scope="col">Account number</th>
                    <th scope="col">Balance</th>
                </tr>
            </thead>
            <tbody>
                @for(var bankAccount : accounts)
                    <tr>
                        <td><a href="/account/${bankAccount.getId()}">${bankAccount.getId()}</a></td>
                        <td>${String.format("$%,.2f", bankAccount.getBalance())}</td>
                    </tr>
                @else
                    <tr>
                        <td colspan="2">You have no accounts to display.</td>
                    </tr>
                @endfor
            </tbody>
        </table>
    `
)
