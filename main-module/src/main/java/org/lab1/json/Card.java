package org.lab1.json;

public final class Card {
  private String cardNumber;
  private String cardHolderName;
  private String expiryDate;
  private String cvv;

  public String getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(final String cardNumberParam) {
    this.cardNumber = cardNumberParam;
  }

  public String getCardHolderName() {
    return cardHolderName;
  }

  public void setCardHolderName(final String cardHolderNameParam) {
    this.cardHolderName = cardHolderNameParam;
  }

  public String getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(final String expiryDateParam) {
    this.expiryDate = expiryDateParam;
  }

  public String getCvv() {
    return cvv;
  }

  public void setCvv(final String cvvParam) {
    this.cvv = cvvParam;
  }
}
