@import edu.cwi.data.Transaction
@import edu.cwi.data.TransactionType

@param int accountNumber
@param Transaction editing = null

<h2 id="transaction-form">${editing == null ? "Create Transaction" : "Update Transaction"}</h2>
<form method="post" action="${editing == null ? "/transaction" : "/transactions/" + editing.getId() + "/update"}">
    <input type="hidden" name="accountId" value="${accountNumber}">

    <label for="transaction-type">Transaction type</label>
    <select id="transaction-type" name="transactionType" required>
        @for(var transactionType : TransactionType.values())
            <option value="${transactionType.name()}"
                    selected="${editing != null && editing.getTransactionType() == transactionType}">${transactionType.name()}</option>
        @endfor
    </select>

    <label for="transaction-amount">Amount</label>
    <input type="number" id="transaction-amount" name="amount"
           value="${editing == null ? "" : editing.getAmount().toPlainString()}"
           min="0.01" max="9999999999999.99" step="0.01" required>

    <label for="transaction-description">Description</label>
    <input type="text" id="transaction-description" name="description"
           value="${editing == null ? "" : editing.getDescription()}" maxlength="255">

    <button type="submit" class="transactions-button">${editing == null ? "Create Transaction" : "Update Transaction"}</button>
    @if(editing != null)
        <a href="/account/${accountNumber}#transaction-form">Cancel edit</a>
    @endif
</form>
