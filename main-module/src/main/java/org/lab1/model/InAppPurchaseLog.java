package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Date;

@Entity
public class InAppPurchaseLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private InAppPurchase inAppPurchase;

  private int quantity;
  private Date timestamp;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final InAppPurchase getInAppPurchase() {
    return inAppPurchase;
  }

  public final void setInAppPurchase(final InAppPurchase inAppPurchase) {
    this.inAppPurchase = inAppPurchase;
  }

  public final int getQuantity() {
    return quantity;
  }

  public final void setQuantity(final int quantity) {
    this.quantity = quantity;
  }

  public final Date getTimestamp() {
    return timestamp;
  }

  public final void setTimestamp(final Date timestamp) {
    this.timestamp = timestamp;
  }
}
