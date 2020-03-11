package io.archilab.prox.searchservice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.archilab.prox.searchservice.project.Project;
import io.archilab.prox.searchservice.project.ProjectDescription;
import io.archilab.prox.searchservice.project.ProjectName;
import io.archilab.prox.searchservice.project.ProjectRepository;
import io.archilab.prox.searchservice.project.ProjectRequirement;
import io.archilab.prox.searchservice.project.ProjectShortDescription;
import io.archilab.prox.searchservice.project.ProjectStatus;
import io.archilab.prox.searchservice.project.SupervisorName;
import io.archilab.prox.searchservice.project.TagName;
import io.archilab.prox.searchservice.services.CachedSearchResultService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;

@DataJpaTest
public class CachedSearchResultServiceTest {

  @Autowired ProjectRepository projectRepository;

  @Autowired Environment environment;

  private Project createProject(
      String title,
      String supervisor,
      ProjectStatus status,
      String requirement,
      String description,
      String shortDescription,
      String[] tags) {
    Project project = new Project();
    project.setId(UUID.randomUUID());
    project.setName(new ProjectName(title));
    project.setStatus(status);
    project.setSupervisorName(new SupervisorName(supervisor));
    project.setRequirement(new ProjectRequirement(requirement));
    project.setDescription(new ProjectDescription(description));
    project.setShortDescription(new ProjectShortDescription(shortDescription));

    for (String tag : tags) {
      project.getTags().add(new TagName(tag));
    }

    return this.projectRepository.save(project);
  }

  @Test
  public void googleStyleUserStory() {
    Project projectA = new Project();
    projectA.setId(UUID.randomUUID());
    projectA.setName(
        new ProjectName("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.setStatus(ProjectStatus.VERFÜGBAR);
    projectA.setSupervisorName(new SupervisorName("Prof. Dr. Pyschny"));
    projectA.setRequirement(new ProjectRequirement("Software Engineering 1"));
    projectA.setDescription(
        new ProjectDescription("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.setShortDescription(
        new ProjectShortDescription("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.getTags().add(new TagName("Predictive Maintenance"));
    projectA.getTags().add(new TagName("Schneidemaschine"));

    this.projectRepository.save(projectA);

    Project projectB = new Project();
    projectB.setId(UUID.randomUUID());
    projectB.setName(new ProjectName("Maintenance von Software-Architekturen"));
    projectB.setStatus(ProjectStatus.VERFÜGBAR);
    projectB.setSupervisorName(new SupervisorName("Prof. Dr. Bente"));
    projectB.setRequirement(new ProjectRequirement("Datenbanken 1+2"));
    projectB.setDescription(new ProjectDescription("Maintenance von Software-Architekturen"));
    projectB.setShortDescription(
        new ProjectShortDescription("Maintenance von Software-Architekturen"));
    projectB.getTags().add(new TagName("Maintenance"));
    projectB.getTags().add(new TagName("Software-Architektur"));

    this.projectRepository.save(projectB);

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    // "Architektur" => B
    var architekturProjects = searchService.getProjects("Architektur");
    assertEquals(1, architekturProjects.size());
    assertEquals(projectB.getId(), architekturProjects.get(0).getId());

    // "Predictive-Maintenance" => A, B
    var predMaintainanceProjects = searchService.getProjects("Predictive-Maintenance");
    assertEquals(2, predMaintainanceProjects.size());
    assertEquals(projectB.getId(), predMaintainanceProjects.get(0).getId());
    assertEquals(projectA.getId(), predMaintainanceProjects.get(1).getId());

    // "Maintenance" => B, A
    var maintainanceProjects = searchService.getProjects("Maintenance");
    assertEquals(2, maintainanceProjects.size());
    assertEquals(projectB.getId(), maintainanceProjects.get(0).getId());
    assertEquals(projectA.getId(), maintainanceProjects.get(1).getId());

    // Betreuer = Pyschny => A
    var pyschnyProjects = searchService.getProjects("Betreuer = Pyschny");
    assertEquals(1, pyschnyProjects.size());
    assertEquals(projectA.getId(), pyschnyProjects.get(0).getId());
  }

  @Test
  public void extendedSearchUserStory() {
    Project projectA = new Project();
    projectA.setId(UUID.randomUUID());
    projectA.setName(
        new ProjectName("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.setStatus(ProjectStatus.VERFÜGBAR);
    projectA.setSupervisorName(new SupervisorName("Prof. Dr. Pyschny"));
    projectA.setRequirement(new ProjectRequirement("Software Engineering 1"));
    projectA.setDescription(
        new ProjectDescription("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.setShortDescription(
        new ProjectShortDescription("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
    projectA.getTags().add(new TagName("Predictive Maintenance"));
    projectA.getTags().add(new TagName("Schneidemaschine"));

    this.projectRepository.save(projectA);

    Project projectB = new Project();
    projectB.setId(UUID.randomUUID());
    projectB.setName(new ProjectName("Maintenance von Software-Architekturen"));
    projectB.setStatus(ProjectStatus.VERFÜGBAR);
    projectB.setSupervisorName(new SupervisorName("Prof. Dr. Bente"));
    projectB.setRequirement(new ProjectRequirement("Datenbanken 1+2"));
    projectB.setDescription(new ProjectDescription("Maintenance von Software-Architekturen"));
    projectB.setShortDescription(
        new ProjectShortDescription("Maintenance von Software-Architekturen"));
    projectB.getTags().add(new TagName("Maintenance"));
    projectB.getTags().add(new TagName("Software-Architektur"));

    this.projectRepository.save(projectB);

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    // Beschreibung = "Architektur" => B
    var architekturProjects = searchService.getProjects("Beschreibung = \"Architektur\"");
    assertEquals(1, architekturProjects.size());
    assertEquals(projectB.getId(), architekturProjects.get(0).getId());

    // Beschreibung = "Predictive-Maintenance" => A
    var predMaintainanceProjects =
        searchService.getProjects("Beschreibung=\"Predictive-Maintenance\"");
    assertEquals(1, predMaintainanceProjects.size());
    assertEquals(projectA.getId(), predMaintainanceProjects.get(0).getId());

    // Beschreibung = "Maintenance" => A, B
    var maintainanceProjects = searchService.getProjects("Beschreibung = \"Maintenance\"");
    assertEquals(2, maintainanceProjects.size());
    assertEquals(projectA.getId(), maintainanceProjects.get(0).getId());
    assertEquals(projectB.getId(), maintainanceProjects.get(1).getId());

    // Tag = "Predictive Maintenance" => A
    var tagPredMainProjects = searchService.getProjects("Tag = \"Predictive Maintenance\"");
    assertEquals(1, tagPredMainProjects.size());
    assertEquals(projectA.getId(), tagPredMainProjects.get(0).getId());

    // Tag = "Maintenance" => A, B
    var tagMainProjects = searchService.getProjects("Tag = \"Maintenance\"");
    assertEquals(2, tagMainProjects.size());
    assertEquals(projectA.getId(), tagMainProjects.get(0).getId());
    assertEquals(projectB.getId(), tagMainProjects.get(1).getId());

    // Betreuer = "Pyschny" => A
    var pyschnyProjects = searchService.getProjects("Betreuer = \"Pyschny\"");
    assertEquals(1, pyschnyProjects.size());
    assertEquals(projectA.getId(), pyschnyProjects.get(0).getId());
  }

  @Test
  public void title() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super",
            ProjectStatus.VERFÜGBAR,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super",
            ProjectStatus.VERFÜGBAR,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.title", "Titel");

    var filterResult = searchService.getProjects(key + "='Project-B'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("Project BB");
    assertEquals(2, weightResult.size());
    assertEquals(projectB.getId(), weightResult.get(0).getId());
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }

  @Test
  public void supervisor() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.VERFÜGBAR,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.supervisor", "Betreuer");

    var filterResult = searchService.getProjects(key + "='Super B'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("Super BB");
    assertEquals(2, weightResult.size());
    assertEquals(projectB.getId(), weightResult.get(0).getId());
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }

  @Test
  public void status() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.ABGESCHLOSSEN,
            "Requirement",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.status", "Status");

    var filterResult = searchService.getProjects(key + "='" + ProjectStatus.VERFÜGBAR.name() + "'");
    assertEquals(1, filterResult.size());
    assertEquals(projectA.getId(), filterResult.get(0).getId());

    var finishedResult =
        searchService.getProjects(key + "='" + ProjectStatus.ABGESCHLOSSEN.name() + "'");
    assertEquals(1, finishedResult.size());
    assertEquals(projectB.getId(), finishedResult.get(0).getId());
  }

  @Test
  public void requirement() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement AA",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.VERFÜGBAR,
            "Requirement BB",
            "Desc",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.requirement", "Voraussetzung");

    var filterResult = searchService.getProjects(key + "='Requirement B'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("Requirement BB");
    assertEquals(2, weightResult.size());
    assertEquals(projectB.getId(), weightResult.get(0).getId());
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }

  @Test
  public void description() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement AA",
            "Desc AA",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.VERFÜGBAR,
            "Requirement BB",
            "Desc BB",
            "ShortDesc",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.description", "Beschreibung");

    var filterResult = searchService.getProjects(key + "='Desc B'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("Desc BB");
    assertEquals(2, weightResult.size());
    assertEquals(projectB.getId(), weightResult.get(0).getId());
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }

  @Test
  public void shortDescription() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement AA",
            "Desc AA",
            "ShortDesc AA",
            new String[] {"tag1", "tag2"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.VERFÜGBAR,
            "Requirement BB",
            "Desc BB",
            "ShortDesc BB",
            new String[] {"tag1", "tag2"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.shortDescription", "Kurzbeschreibung");

    var filterResult = searchService.getProjects(key + "='ShortDesc BB'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("ShortDesc BB");
    assertEquals(2, weightResult.size());
    assertEquals(projectB.getId(), weightResult.get(0).getId());
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }

  @Test
  public void tags() {
    Project projectA =
        this.createProject(
            "Project-A AA",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement AA",
            "Desc AA",
            "ShortDesc AA",
            new String[] {"tag1", "tag2", "tag a"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB",
            ProjectStatus.VERFÜGBAR,
            "Requirement BB",
            "Desc BB",
            "ShortDesc BB",
            new String[] {"tag1", "tag2", "tag b"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    String key = this.environment.getProperty("searchNames.tag", "Tag");

    var filterResult = searchService.getProjects(key + "='tag b'");
    assertEquals(1, filterResult.size());
    assertEquals(projectB.getId(), filterResult.get(0).getId());

    var weightResult = searchService.getProjects("tag b");
    assertEquals(2, weightResult.size()); // gleichwertig result
    assertEquals(projectA.getId(), weightResult.get(0).getId());
    assertEquals(projectB.getId(), weightResult.get(1).getId());
  }

  @Test
  public void weights() {
    Project projectA =
        this.createProject(
            "Project-A AA CC",
            "Super AA",
            ProjectStatus.VERFÜGBAR,
            "Requirement AA",
            "Desc AA",
            "ShortDesc AA CC",
            new String[] {"tag1", "tag2", "tag a"});

    Project projectB =
        this.createProject(
            "Project-B BB",
            "Super BB CC",
            ProjectStatus.VERFÜGBAR,
            "Requirement BB",
            "Desc BB",
            "ShortDesc BB",
            new String[] {"tag1", "tag2", "tag b", "cc"});

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    var weightResult = searchService.getProjects("cC");
    assertEquals(2, weightResult.size());

    // Project B: cc in Supervisor and tag
    assertEquals(projectB.getId(), weightResult.get(0).getId());

    // Project A: cc in Title and short description
    assertEquals(projectA.getId(), weightResult.get(1).getId());
  }
}
