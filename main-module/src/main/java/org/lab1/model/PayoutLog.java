package org.lab1.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class PayoutLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  private Developer developer;

  @ManyToOne
  private MonetizedApplication monetizedApplication;

  private double payoutValue;
  private Date timestamp;
}
