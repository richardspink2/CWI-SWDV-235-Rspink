
@param String name

@param String balance
@param int accountNumber


<div>
    <h1>Account</h1>
    <div>
        <strong>Name:</strong> ${name}
    </div>
    @if(accountNumber > 0)
        <div>
            <strong>Account number:</strong> ${accountNumber}
        </div>
    @endif
    <div>
        <strong>Balance:</strong> ${balance}
    </div>
</div>
