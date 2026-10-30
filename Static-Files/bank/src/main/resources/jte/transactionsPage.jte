<%-- Java imports --%>
@import java.util.Map

<%-- Parameters --%>
@param Map<String, String> transactions
@param Map<String, String> amounts
@param double total

<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Bank Transactions</title>
    <link rel="stylesheet" href="/styles.css">
  </head>
  <body>
    <nav class="site-nav">
      <a href="/">Home</a>
      <a href="/account.html">Account</a>
      <a href="/transactions">Transactions</a>
    </nav>

    <main id="main" class="content">
      <a href="/account.html" class="back-to-account">Back to account</a>
      <h2>Transactions</h2>

      <form method="post" action="/transactions">
        <label for="Description">Create Transaction</label>
        <input
          type="text"
          id="Description"
          name="description"
          placeholder="Description"
          required>

        <label for="Amount">Amount</label>
        <input
          type="number"
          step="0.01"
          id="Amount"
          name="amount"
          placeholder="Amount"
          required>

        <button type="submit" class="transactions-button">Create Transaction</button>
      </form>

      <p>Choose a transaction to view, update, or delete.</p>

      <table class="transactions-table">
        <thead>
          <tr>
            <th scope="col">Name</th>
            <th scope="col">Amount</th>
          </tr>
        </thead>
        <tbody>
          <%-- Loop through the transactions map --%>
          @for(var entry : transactions.entrySet())
            <tr>
              <td>
                <a href="/transactions/${entry.getKey()}">${entry.getValue()}</a>
              </td>
              <td>${String.format("$%,.2f", Double.parseDouble(amounts.get(entry.getKey())))}</td>
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
    </main>
  </body>
</html>
