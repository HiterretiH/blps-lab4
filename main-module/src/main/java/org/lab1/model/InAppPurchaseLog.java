package org.lab1.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class InAppPurchaseLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private InAppPurchase inAppPurchase;

  private int quantity;
  private Date timestamp;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public InAppPurchase getInAppPurchase() {
    return inAppPurchase;
  }

  public void setInAppPurchase(InAppPurchase inAppPurchase) {
    this.inAppPurchase = inAppPurchase;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
