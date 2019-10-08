package io.archilab.prox.searchservice;

import io.archilab.prox.searchservice.project.*;
import io.archilab.prox.searchservice.services.CachedSearchResultService;
import lombok.var;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.UUID;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CachedSearchResultServiceTest {

  @Autowired
  ProjectRepository projectRepository;

  @Autowired
  Environment environment;

  @Test
  public void googleStyleUserStory() {
    Project projectA = new Project();
    projectA.setId(UUID.randomUUID());
    projectA
        .setName(new ProjectName("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
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
    projectB
        .setShortDescription(new ProjectShortDescription("Maintenance von Software-Architekturen"));
    projectB.getTags().add(new TagName("Maintenance"));
    projectB.getTags().add(new TagName("Software-Architektur"));

    this.projectRepository.save(projectB);


    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    // "Architektur" => B
    var architekturProjects = searchService.getProjects("Architektur");
    Assert.assertEquals(1, architekturProjects.size());
    Assert.assertEquals(projectB.getId(), architekturProjects.get(0).getId());

    // "Predictive-Maintenance" => A, B
    var predMaintainanceProjects = searchService.getProjects("Predictive-Maintenance");
    Assert.assertEquals(2, predMaintainanceProjects.size());
    Assert.assertEquals(projectA.getId(), predMaintainanceProjects.get(0).getId());
    Assert.assertEquals(projectB.getId(), predMaintainanceProjects.get(1).getId());

    // "Maintenance" => A, B
    var maintainanceProjects = searchService.getProjects("Maintenance");
    Assert.assertEquals(2, maintainanceProjects.size());
    Assert.assertEquals(projectA.getId(), maintainanceProjects.get(0).getId());
    Assert.assertEquals(projectB.getId(), maintainanceProjects.get(1).getId());

    // Betreuer = Pyschny => A
    var pyschnyProjects = searchService.getProjects("Betreuer = Pyschny");
    Assert.assertEquals(1, pyschnyProjects.size());
    Assert.assertEquals(projectA.getId(), pyschnyProjects.get(0).getId());
  }


  @Test
  public void extendedSearchUserStory() {
    Project projectA = new Project();
    projectA.setId(UUID.randomUUID());
    projectA
        .setName(new ProjectName("Eine Predictive-Maintenance-Plattform für Schneidemaschinen"));
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
    projectB
        .setShortDescription(new ProjectShortDescription("Maintenance von Software-Architekturen"));
    projectB.getTags().add(new TagName("Maintenance"));
    projectB.getTags().add(new TagName("Software-Architektur"));

    this.projectRepository.save(projectB);

    var searchService = new CachedSearchResultService(this.projectRepository, this.environment);

    // Beschreibung = "Architektur" => B
    var architekturProjects = searchService.getProjects("Beschreibung = \"Architektur\"");
    Assert.assertEquals(1, architekturProjects.size());
    Assert.assertEquals(projectB.getId(), architekturProjects.get(0).getId());

    // Beschreibung = "Predictive-Maintenance" => A
    var predMaintainanceProjects =
        searchService.getProjects("Beschreibung=\"Predictive-Maintenance\"");
    Assert.assertEquals(1, predMaintainanceProjects.size());
    Assert.assertEquals(projectA.getId(), predMaintainanceProjects.get(0).getId());

    // Beschreibung = "Maintenance" => A, B
    var maintainanceProjects = searchService.getProjects("Beschreibung = \"Maintenance\"");
    Assert.assertEquals(2, maintainanceProjects.size());
    Assert.assertEquals(projectA.getId(), maintainanceProjects.get(0).getId());
    Assert.assertEquals(projectB.getId(), maintainanceProjects.get(1).getId());

    // Tag = "Predictive Maintenance" => A
    var tagPredMainProjects = searchService.getProjects("Tag = \"Predictive Maintenance\"");
    Assert.assertEquals(1, tagPredMainProjects.size());
    Assert.assertEquals(projectA.getId(), tagPredMainProjects.get(0).getId());

    // Tag = "Maintenance" => A, B
    var tagMainProjects = searchService.getProjects("Tag = \"Maintenance\"");
    Assert.assertEquals(2, tagMainProjects.size());
    Assert.assertEquals(projectA.getId(), tagMainProjects.get(0).getId());
    Assert.assertEquals(projectB.getId(), tagMainProjects.get(1).getId());

    // Betreuer = "Pyschny" => A
    var pyschnyProjects = searchService.getProjects("Betreuer = \"Pyschny\"");
    Assert.assertEquals(1, pyschnyProjects.size());
    Assert.assertEquals(projectA.getId(), pyschnyProjects.get(0).getId());
  }
}
