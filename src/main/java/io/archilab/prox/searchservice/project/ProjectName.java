package io.archilab.prox.searchservice.project;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectName {

  private static final int MAX_LENGTH = 10485760;

  @Column(length = MAX_LENGTH)
  private String name;

  public ProjectName(String name) {
    if (!ProjectName.isValid(name)) {
      throw new IllegalArgumentException(
          String.format("Name %s exceeded maximum number of %d allowed characters", name,
              ProjectName.MAX_LENGTH));
    }
    this.name = name;
  }

  public static boolean isValid(String name) {
    return name != null && name.length() <= ProjectName.MAX_LENGTH;
  }
}
