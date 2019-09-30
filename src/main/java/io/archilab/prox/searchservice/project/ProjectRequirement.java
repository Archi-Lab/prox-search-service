

package io.archilab.prox.searchservice.project;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Data
@Setter(AccessLevel.NONE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRequirement {
  private static final int MAX_LENGTH = 10485760;

  @Column(length = MAX_LENGTH)
  private String requirement;

  public ProjectRequirement(String requirement) {
    if (!ProjectRequirement.isValid(requirement)) {
      throw new IllegalArgumentException(
          String.format("Requirement %s exceeded maximum number of %d allowed characters",
              requirement, ProjectRequirement.MAX_LENGTH));
    }

    this.requirement = requirement;
  }

  public static boolean isValid(String name) {
    return name != null && name.length() <= ProjectRequirement.MAX_LENGTH;
  }
}
