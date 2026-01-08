package org.lab1.json;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class GoogleSheetRequestWithData {
  private String googleEmail;
  private String sheetTitle;
  private List<String> headers;
  private List<List<Object>> data;
}
