package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity
@Data
public class MonetizedApplication {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne private Developer developer;

  @OneToOne private Application application;

  private double currentBalance;
  private double revenue;
  private double downloadRevenue;
  private double adsRevenue;
  private double purchasesRevenue;
}
