package io.archilab.prox.searchservice.project;

import lombok.Getter;
import lombok.Setter;

public class WeightedProject implements Comparable<WeightedProject> {
  @Getter
  private Project project;

  @Setter
  @Getter
  private int weight;

  public WeightedProject(Project project) {
    this.project = project;
  }

  @Override
  public int compareTo(WeightedProject o) {
    if (o == null)
      return -1;
    if (this.getWeight() > o.getWeight())
      return -1;
    if (this.getWeight() < o.getWeight())
      return 1;

    return 0;
  }

  @Override
  public String toString() {
    return this.weight + " - " + this.project.getName().getName();
  }
}
