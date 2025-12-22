package org.lab1.json;

import java.util.List;

public class InAppPurchasesJson {
  List<String> titles;
  List<String> descriptions;
  List<Double> prices;

  public List<String> getTitles() {
    return titles;
  }

  public void setTitles(List<String> titles) {
    this.titles = titles;
  }

  public List<String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(List<String> descriptions) {
    this.descriptions = descriptions;
  }

  public List<Double> getPrices() {
    return prices;
  }

  public void setPrices(List<Double> prices) {
    this.prices = prices;
  }
}
