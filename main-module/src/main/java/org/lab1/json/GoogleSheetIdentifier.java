package org.lab1.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GoogleSheetIdentifier {
  private String googleEmail;
  private String spreadsheetTitle;
}
