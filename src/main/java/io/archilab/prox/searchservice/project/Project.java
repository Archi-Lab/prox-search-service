package io.archilab.prox.searchservice.project;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.archilab.prox.searchservice.core.AbstractEntity;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
@Transactional
public class Project extends AbstractEntity {

  @Setter
  @JsonUnwrapped
  private ProjectName name;

  @Setter
  @JsonUnwrapped
  private ProjectDescription description;

  @Setter
  @JsonUnwrapped
  private ProjectShortDescription shortDescription;

  @Setter
  @JsonUnwrapped
  private ProjectRequirement requirement;

  @Setter
  private ProjectStatus status;

  @NotNull
  @Setter
  @JsonUnwrapped
  private SupervisorName supervisorName;

  // Eager loading is important for the CachedSearchService
  @Getter
  @ElementCollection(fetch = FetchType.EAGER)
  private List<TagName> tags = new ArrayList<>();

  @Basic
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private java.util.Date created;

  @Basic
  @Temporal(TemporalType.TIMESTAMP)
  private java.util.Date modified;


  public Project(UUID id, ProjectName name, ProjectShortDescription shortDescription,
      ProjectDescription description, ProjectStatus status, ProjectRequirement requirement,
      @NotNull SupervisorName supervisorName) {

    super(id);
    this.requirement = requirement;
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
    this.status = status;
    this.supervisorName = supervisorName;
  }

  public List<String> GetTagNames()
  {
    List<String> names = new ArrayList<>();

    if(this.tags == null)
      return names;

    for (TagName tagName : this.tags)
      names.add(tagName.getTagName());

    return names;
  }
}
