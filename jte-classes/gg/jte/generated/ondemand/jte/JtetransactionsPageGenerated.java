package gg.jte.generated.ondemand.jte;
import java.util.Map;
@SuppressWarnings("unchecked")
public final class JtetransactionsPageGenerated {
	public static final String JTE_NAME = "jte/transactionsPage.jte";
	public static final int[] JTE_LINE_INFO = {1,1,4,4,4,4,58,58,59,59,62,62,62,62,62,62,62,64,64,64,66,66,71,71,71,78,78,78,4,5,6,6,6,6};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Map<String, String> transactions, Map<String, String> amounts, double total) {
		jteOutput.writeContent("\n<!doctype html>\n<html lang=\"en\">\n  <head>\n    <meta charset=\"utf-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n    <title>Bank Transactions</title>\n    <link rel=\"stylesheet\" href=\"/styles.css\">\n  </head>\n  <body>\n    <nav class=\"site-nav\">\n      <a href=\"/\">Home</a>\n      <a href=\"/account.html\">Account</a>\n      <a href=\"/transactions\">Transactions</a>\n    </nav>\n\n    <main id=\"main\" class=\"content\">\n      <a href=\"/account.html\" class=\"back-to-account\">Back to account</a>\n      <h2>Transactions</h2>\n\n      <form method=\"post\" action=\"/transactions\">\n        <label for=\"Description\">Create Transaction</label>\n        <input\n          type=\"text\"\n          id=\"Description\"\n          name=\"description\"\n          placeholder=\"Description\"\n          required>\n\n        <label for=\"Amount\">Amount</label>\n        <input\n          type=\"number\"\n          step=\"0.01\"\n          id=\"Amount\"\n          name=\"amount\"\n          placeholder=\"Amount\"\n          required>\n\n        <button type=\"submit\" class=\"transactions-button\">Create Transaction</button>\n      </form>\n\n      <p>Choose a transaction to view, update, or delete.</p>\n\n      <table class=\"transactions-table\">\n        <thead>\n          <tr>\n            <th scope=\"col\">Name</th>\n            <th scope=\"col\">Amount</th>\n          </tr>\n        </thead>\n        <tbody>\n          ");
		jteOutput.writeContent("\n          ");
		for (var entry : transactions.entrySet()) {
			jteOutput.writeContent("\n            <tr>\n              <td>\n                <a href=\"/transactions/");
			jteOutput.setContext("a", "href");
			jteOutput.writeUserContent(entry.getKey());
			jteOutput.setContext("a", null);
			jteOutput.writeContent("\">");
			jteOutput.setContext("a", null);
			jteOutput.writeUserContent(entry.getValue());
			jteOutput.writeContent("</a>\n              </td>\n              <td>");
			jteOutput.setContext("td", null);
			jteOutput.writeUserContent(String.format("$%,.2f", Double.parseDouble(amounts.get(entry.getKey()))));
			jteOutput.writeContent("</td>\n            </tr>\n          ");
		}
		jteOutput.writeContent("\n        </tbody>\n        <tfoot>\n          <tr>\n            <th scope=\"row\">Total</th>\n            <td>");
		jteOutput.setContext("td", null);
		jteOutput.writeUserContent(String.format("$%,.2f", total));
		jteOutput.writeContent("</td>\n          </tr>\n        </tfoot>\n      </table>\n    </main>\n  </body>\n</html>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Map<String, String> transactions = (Map<String, String>)params.get("transactions");
		Map<String, String> amounts = (Map<String, String>)params.get("amounts");
		double total = (double)params.get("total");
		render(jteOutput, jteHtmlInterceptor, transactions, amounts, total);
	}
}
