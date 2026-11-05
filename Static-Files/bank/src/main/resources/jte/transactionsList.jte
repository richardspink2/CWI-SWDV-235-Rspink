<%-- Java imports --%>
@import java.util.Map

<%-- Parameters --%>
@param Map<String, String> transactions



@param Map<String, String> amounts
@param double total

<h1>Transactions</h1>
<table class="transactions-table">
    <thead>
        <tr>
            <th scope="col">Name</th>
            <th scope="col">Amount</th>
        </tr>
    </thead>
    <tbody>
    @for(var entry : transactions.entrySet())
        <tr>
            <td>
                <a href="/transactions/${entry.getKey()}">${entry.getValue()}</a>
            </td>
            <td>${String.format("$%,.2f", Double.parseDouble(amounts.get(entry.getKey())))}</td>
        </tr>
    @else
        <tr>
            <td colspan="2">You have no transactions to display.</td>
        </tr>
    @endfor
    </tbody>
    <tfoot>
        <tr>
            <th scope="row">Total</th>
            <td>${String.format("$%,.2f", total)}</td>
        </tr>
    </tfoot>
</table>
