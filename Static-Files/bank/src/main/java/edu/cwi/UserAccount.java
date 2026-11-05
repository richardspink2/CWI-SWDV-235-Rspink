package edu.cwi;

import java.text.DecimalFormat;
import java.util.Map;

public record UserAccount(String name, Double balance, Map<String, String> transactions) {

  public String formatBalance() {
    DecimalFormat df = new DecimalFormat("$#,###.00");
    return df.format(balance);
  }
}
