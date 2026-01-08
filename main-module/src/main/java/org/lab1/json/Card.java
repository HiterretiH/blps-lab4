package org.lab1.json;

import lombok.Data;

@Data
public final class Card {
  private String cardNumber;
  private String cardHolderName;
  private String expiryDate;
  private String cvv;
}
