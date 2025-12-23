package org.lab1.json;

import java.util.List;

public final class InAppPurchasesJson {
  private List<String> titles;
  private List<String> descriptions;
  private List<Double> prices;

  public List<String> getTitles() {
    return titles;
  }

  public void setTitles(final List<String> titlesParam) {
    this.titles = titlesParam;
  }

  public List<String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(final List<String> descriptionsParam) {
    this.descriptions = descriptionsParam;
  }

  public List<Double> getPrices() {
    return prices;
  }

  public void setPrices(final List<Double> pricesParam) {
    this.prices = pricesParam;
  }
}
