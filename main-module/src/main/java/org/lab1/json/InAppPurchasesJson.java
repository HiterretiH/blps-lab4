package org.lab1.json;

import java.util.List;
import lombok.Data;

@Data
public final class InAppPurchasesJson {
  private List<String> titles;
  private List<String> descriptions;
  private List<Double> prices;
}
