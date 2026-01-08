package org.lab1.json;

import lombok.Data;
import java.util.List;

@Data
public final class InAppPurchasesJson {
  private List<String> titles;
  private List<String> descriptions;
  private List<Double> prices;
}
