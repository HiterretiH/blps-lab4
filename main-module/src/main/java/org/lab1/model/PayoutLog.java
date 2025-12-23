package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Date;

@Entity
public class PayoutLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private Developer developer;

  @ManyToOne private MonetizedApplication monetizedApplication;

  private double payoutValue;
  private Date timestamp;

  public final int getId() {
    return id;
  }

  public final void setId(final int id) {
    this.id = id;
  }

  public final Developer getDeveloper() {
    return developer;
  }

  public final void setDeveloper(final Developer developer) {
    this.developer = developer;
  }

  public final MonetizedApplication getMonetizedApplication() {
    return monetizedApplication;
  }

  public final void setMonetizedApplication(final MonetizedApplication monetizedApplication) {
    this.monetizedApplication = monetizedApplication;
  }

  public final double getPayoutValue() {
    return payoutValue;
  }

  public final void setPayoutValue(final double payoutValue) {
    this.payoutValue = payoutValue;
  }

  public final Date getTimestamp() {
    return timestamp;
  }

  public final void setTimestamp(final Date timestamp) {
    this.timestamp = timestamp;
  }
}
