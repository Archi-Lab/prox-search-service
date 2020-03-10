package io.archilab.prox.searchservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.archilab.prox.searchservice.project.Project;
import io.archilab.prox.searchservice.project.ProjectDescription;
import io.archilab.prox.searchservice.project.ProjectName;
import io.archilab.prox.searchservice.project.ProjectRepository;
import io.archilab.prox.searchservice.project.ProjectRequirement;
import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.project.ProjectShortDescription;
import io.archilab.prox.searchservice.project.ProjectStatus;
import io.archilab.prox.searchservice.project.SupervisorName;
import io.archilab.prox.searchservice.project.TagName;
import io.archilab.prox.searchservice.services.SearchResultService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;

@SpringBootTest
public class SearchResultServiceTest {

  @Autowired ProjectRepository projectRepository;

  @Autowired Environment environment;

  @Autowired SearchResultService searchResultService;

  PageRequest pageable = PageRequest.of(0, 20);

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

    this.projectRepository.deleteAll();

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

    {
      // "Architektur" => B
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Architektur";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {

      // "Predictive-Maintenance" => A, B

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Predictive-Maintenance";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
      assertEquals(projectB.getId().toString(), resultData.get(1).getId());
    }

    {

      // "Maintenance" => B, A

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Maintenance";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);

      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }

    {
      // Betreuer = Pyschny => A

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Betreuer = Pyschny";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
    }
  }

  @Test
  public void extendedSearchUserStory() {

    this.projectRepository.deleteAll();

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

    {
      // Beschreibung = "Architektur" => B
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Beschreibung = \"Architektur\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      // Beschreibung = "Predictive-Maintenance" => A
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Beschreibung=\"Predictive-Maintenance\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
    }

    {
      // Beschreibung = "Maintenance" => A, B
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Beschreibung = \"Maintenance\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
      assertEquals(projectB.getId().toString(), resultData.get(1).getId());
    }

    {
      // Tag = "Predictive Maintenance" => A
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Tag = \"Predictive Maintenance\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
    }

    {
      // Tag = "Maintenance" => B, A
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Tag = \"Maintenance\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }

    {
      // Betreuer = "Pyschny" => A
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Betreuer = \"Pyschny\"";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
    }
  }

  @Test
  public void title() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.title", "Titel");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='Project-B'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Project BB";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void supervisor() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.supervisor", "Betreuer");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='Super B'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Super BB";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void status() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.status", "Status");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='" + ProjectStatus.VERFÜGBAR.name() + "'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {

        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
    }

    {
      String key = this.environment.getProperty("searchNames.status", "Status");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='" + ProjectStatus.ABGESCHLOSSEN.name() + "'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }
  }

  @Test
  public void requirement() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.requirement", "Voraussetzung");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='Requirement B'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Requirement BB";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void description() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.description", "Beschreibung");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='Desc B'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "Desc BB";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void shortDescription() {

    this.projectRepository.deleteAll();

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

    {
      String key = this.environment.getProperty("searchNames.shortDescription", "Kurzbeschreibung");

      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = key + "='ShortDesc BB'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "ShortDesc BB";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
      assertEquals(projectA.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void tags() {

    this.projectRepository.deleteAll();

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

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String key = this.environment.getProperty("searchNames.tag", "Tag");
      String searchText = key + "='tag b'";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(1, resultCount);
      assertEquals(projectB.getId().toString(), resultData.get(0).getId());
    }

    {
      Pair<List<ProjectSearchData>, Long> result = null;
      String searchText = "tag b";
      try {
        result = this.searchResultService.findPaginated(this.pageable, searchText);
      } catch (Exception e) {
        e.printStackTrace();
        fail();
      }
      List<ProjectSearchData> resultData = result.getFirst();
      long resultCount = result.getSecond();

      assertEquals(2, resultCount); // beide ergebnisse haben gleichen score
      assertEquals(projectA.getId().toString(), resultData.get(0).getId());
      assertEquals(projectB.getId().toString(), resultData.get(1).getId());
    }
  }

  @Test
  public void weights() {

    this.projectRepository.deleteAll();

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

    Pair<List<ProjectSearchData>, Long> result = null;
    String searchText = "cC";
    try {
      result = this.searchResultService.findPaginated(this.pageable, searchText);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    List<ProjectSearchData> resultData = result.getFirst();
    long resultCount = result.getSecond();

    assertEquals(2, resultCount);

    // Project B: cc in Supervisor and tag
    assertEquals(projectB.getId().toString(), resultData.get(0).getId());

    // Project A: cc in Title and short description
    assertEquals(projectA.getId().toString(), resultData.get(1).getId());
  }
}
