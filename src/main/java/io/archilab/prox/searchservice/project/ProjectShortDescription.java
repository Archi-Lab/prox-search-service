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
public class ProjectShortDescription {

  private static final int MAX_LENGTH = 10485760;

  @Column(length = MAX_LENGTH)
  private String shortDescription;

  public ProjectShortDescription(String shortDescription) {
    if (!ProjectShortDescription.isValid(shortDescription)) {
      throw new IllegalArgumentException(
          String.format("ShortDescription %s exceeded maximum number of %d allowed characters",
              shortDescription, ProjectShortDescription.MAX_LENGTH));
    }
    this.shortDescription = shortDescription;
  }

  public static boolean isValid(String name) {
    return name != null && name.length() <= ProjectShortDescription.MAX_LENGTH;
  }
}
