package org.lab1.json;

public final class DeveloperJson {
  private int id;
  private String name;
  private String description;
  private Integer userId;

  public int getId() {
    return id;
  }

  public void setId(final int idParam) {
    this.id = idParam;
  }

  public String getName() {
    return name;
  }

  public void setName(final String nameParam) {
    this.name = nameParam;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String descriptionParam) {
    this.description = descriptionParam;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(final Integer userIdParam) {
    this.userId = userIdParam;
  }
}
