package org.lab1.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class AddAppSheetsRequest {
  private String spreadsheetId;
  private int appId;
}
