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
public class ProjectDescription {

  private static final int MAX_LENGTH = 10485760;

  @Column(length = MAX_LENGTH)
  private String description;

  public ProjectDescription(String description) {
    if (!ProjectDescription.isValid(description)) {
      throw new IllegalArgumentException(
          String.format("Name %s exceeded maximum number of %d allowed characters", description,
              ProjectDescription.MAX_LENGTH));
    }
    this.description = description;
  }

  public static boolean isValid(String name) {
    return name != null && name.length() <= ProjectDescription.MAX_LENGTH;
  }
}
