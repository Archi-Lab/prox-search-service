package io.archilab.prox.searchservice.project;

public enum ProjectStatus {
  VERFÜGBAR(0), LAUFEND(1), ABGESCHLOSSEN(2);


  private int value;

  private ProjectStatus(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
