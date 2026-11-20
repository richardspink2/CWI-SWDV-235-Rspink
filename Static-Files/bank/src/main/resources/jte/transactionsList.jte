<%-- Java imports --%>
@import java.util.Map
@import java.math.BigDecimal

<%-- Parameters --%>
@param Map<String, String> transactions



@param Map<String, String> amounts
@param BigDecimal total
@param int accountNumber

<h1>Transactions</h1>
<table class="transactions-table">
    <thead>
        <tr>
            <th scope="col">Name</th>
            <th scope="col">Amount</th>
            <th scope="col">Actions</th>
        </tr>
    </thead>
    <tbody>
    @for(var entry : transactions.entrySet())
        <tr>
            <td>
                <a href="/account/${accountNumber}?edit=${entry.getKey()}#transaction-form">${entry.getValue()}</a>
            </td>
            <td>${String.format("$%,.2f", new BigDecimal(amounts.get(entry.getKey())))}</td>
            <td>
                <div class="transaction-actions">
                    <a href="/account/${accountNumber}?edit=${entry.getKey()}#transaction-form">Edit</a>
                    <form method="post" action="/transactions/${entry.getKey()}/delete">
                        <button type="submit">Delete</button>
                    </form>
                </div>
            </td>
        </tr>
    @else
        <tr>
            <td colspan="3">You have no transactions to display.</td>
        </tr>
    @endfor
    </tbody>
    <tfoot>
        <tr>
            <th scope="row">Total</th>
            <td>${String.format("$%,.2f", total)}</td>
            <td></td>
        </tr>
    </tfoot>
</table>
