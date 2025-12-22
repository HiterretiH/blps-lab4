package org.lab1.model;

import jakarta.persistence.*;
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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Developer getDeveloper() {
    return developer;
  }

  public void setDeveloper(Developer developer) {
    this.developer = developer;
  }

  public MonetizedApplication getMonetizedApplication() {
    return monetizedApplication;
  }

  public void setMonetizedApplication(MonetizedApplication monetizedApplication) {
    this.monetizedApplication = monetizedApplication;
  }

  public double getPayoutValue() {
    return payoutValue;
  }

  public void setPayoutValue(double payoutValue) {
    this.payoutValue = payoutValue;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }
}
