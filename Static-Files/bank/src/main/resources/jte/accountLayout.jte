@import gg.jte.Content

@param String title
@param Content account
@param Content detail

<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <link rel="stylesheet" href="/styles.css">
  </head>
<body>
    <nav class="site-nav">
        <a href="/">Home</a>
        <a href="/account.html">Account</a>
    </nav>

    <main class="content">
        <section>
            ${account}
        </section>

        <section>
            ${detail}
        </section>

        <a href="/transactions/manage" class="transactions-button">
            Manage Transactions
        </a>
    </main>
</body>
</html>
