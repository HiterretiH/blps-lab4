package org.lab1.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String errorCode;
  private String message;
  private String path;
  private Object details;

  public static ErrorResponseBuilder builder() {
    return new ErrorResponseBuilder().timestamp(LocalDateTime.now());
  }
}
