package io.archilab.prox.searchservice.project;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import io.archilab.prox.searchservice.core.AbstractEntity;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project{

  @Setter
  @JsonUnwrapped
  private ProjectName name;

  @Id
  @Setter
  @JsonUnwrapped
  private URI uri;

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
  
  @Getter
  @ElementCollection
  private List<TagName> tags = new ArrayList<>();

  @Basic
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false)
  private java.util.Date created;

  @Basic
  @Temporal(TemporalType.TIMESTAMP)
  private java.util.Date modified;


  public Project(URI uri, ProjectName name, ProjectShortDescription shortDescription,
      ProjectDescription description, ProjectStatus status, ProjectRequirement requirement,
         @NotNull SupervisorName supervisorName )  {

    this.uri = uri;
    this.requirement = requirement;
    this.name = name;
    this.shortDescription = shortDescription;
    this.description = description;
    this.status = status;
    this.supervisorName = supervisorName;
  }


}
