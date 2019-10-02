package io.archilab.prox.searchservice.services;

import io.archilab.prox.searchservice.project.Project;
import io.archilab.prox.searchservice.project.ProjectRepository;
import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.project.WeightedProject;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class CachedSearchResultService {

  Logger log = LoggerFactory.getLogger(ProjectRepository.class);

  private ProjectRepository projectRepository;
  private List<Project> cache;

  @Autowired
  public CachedSearchResultService(ProjectRepository projectRepository){
    this.projectRepository = projectRepository;

    this.load();
  }

  public void load(){

    var cache = new ArrayList<Project>();

    this.projectRepository.findAll().forEach(cache::add);

    this.cache = cache;

    log.info("SearchService: Projects loaded");
  }

  public Long getTotalElements()
  {
    return projectRepository.count();
  }

  public List<ProjectSearchData> findPaginated(Pageable pageable, String searchText) throws Exception {

    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    var start = pageNumber * pageSize;

    var projects = this.getProjects(searchText);

    List<ProjectSearchData> retList = new ArrayList<>();

    for (int i = start; i < start + pageSize && i < projects.size(); i++) {
      var project = projects.get(i);
      var projectData = new ProjectSearchData(project.getId());
      retList.add(projectData);
    }

    log.info("Returned Projects: " + retList.size());

    return retList;
  }

  private List<Project> getProjects(String filter)
  {
    if(filter == null || filter.length() < 2)
      return this.cache;

    filter = filter.toLowerCase();

    var result = new ArrayList<Project>(this.cache);

    log.info(filter);

    // Supervisor
    var supervisorFilter = this.getFilter(filter, "Betreuer");
    if(supervisorFilter.hasValues){
      result = new ArrayList<>(this.filterBySupervisor(result, supervisorFilter.values));
      filter = supervisorFilter.filter;
    }

    if(filter.length() < 2)
      return result;

    // Short Description
    var shortDescriptionFilter = this.getFilter(filter, "Kurzbeschreibung");
    if(shortDescriptionFilter.hasValues){
      result = new ArrayList<>(this.filterByShortDescription(result, shortDescriptionFilter.values));
      filter = shortDescriptionFilter.filter;
    }

    if(filter.length() < 2)
      return result;

    // Description
    var descriptionFilter = this.getFilter(filter, "Beschreibung");
    if(descriptionFilter.hasValues){
      result = new ArrayList<>(this.filterByDescription(result, descriptionFilter.values));
      filter = descriptionFilter.filter;
    }

    if(filter.length() < 2)
      return result;

    log.info(filter);

    List<String> words = new ArrayList<>();

    Pattern reg = Pattern.compile("(\\w+)");
    Matcher m = reg.matcher(filter);
    while (m.find()){
      words.add(m.group());
      log.info("Word: " + m.group());
    }

    List<WeightedProject> weighted = new ArrayList<>();
    result.forEach(p -> weighted.add(new WeightedProject(p)));

    this.updateSupervisorWeight(weighted, words, 20);
    this.updateTitleWeight(weighted, words, 10);

    Collections.sort(weighted);

    log.info("result: " + weighted);

    return weighted.stream().map(p -> p.getProject()).collect(Collectors.toList());
  }


  private FilterResult getFilter(String searchString, String key){
    List<String> result = new ArrayList<>();

    var pattern = key.toLowerCase() + "\\s*=\\s*['\"](\\S*)['\"]";

    Pattern pairRegex = Pattern.compile(pattern);
    Matcher matcher = pairRegex.matcher(searchString.toLowerCase());

    while (matcher.find()) {
      var match = matcher.group(0).toLowerCase();
      var value = matcher.group(1).toLowerCase();
      result.add(value);

      searchString = searchString.replace(match, "");
    }

    return new FilterResult(result, searchString);
  }

  private List<Project> filterBySupervisor(List<Project> projects, List<String> filters) {

    var result = new ArrayList<Project>();

    for (Project project : projects) {

      String projectSupervisor = project.getSupervisorName().getSupervisorName().toLowerCase();

      for (String filter : filters) {
        if (projectSupervisor.contains(filter)) {
          result.add(project);
          break;
        }
      }
    }

    return result;
  }

  private List<Project> filterByDescription(List<Project> projects, List<String> filters) {

    var result = new ArrayList<Project>();

    for (Project project : projects) {

      String description = project.getDescription().getDescription().toLowerCase();

      for (String filter : filters) {
        if (description.contains(filter)) {
          result.add(project);
          break;
        }
      }
    }

    return result;
  }

  private List<Project> filterByShortDescription(List<Project> projects, List<String> filters) {

    var result = new ArrayList<Project>();

    for (Project project : projects) {

      String shortDescription = project.getShortDescription().getShortDescription().toLowerCase();
      for (String filter : filters) {
        if (shortDescription.contains(filter)) {
          result.add(project);
          break;
        }
      }
    }

    return result;
  }

  private class FilterResult
  {
    Boolean hasValues;
    List<String> values;
    String filter;

    FilterResult(List<String> values, String filter){
      this.values = values;
      this.filter = filter;
      this.hasValues = values.size() > 0;
    }
  }


  private void updateSupervisorWeight(List<WeightedProject> projects, List<String> words, int weightValue) {
    for (WeightedProject weightedProject : projects) {
      Project project = weightedProject.getProject();
      int weight = weightedProject.getWeight();

      String supervisorName = project.getSupervisorName().getSupervisorName().toLowerCase();

      for (String word: words) {
        if(supervisorName.contains(word)){
          weight += weightValue;
        }
      }

      weightedProject.setWeight(weight);
    }
  }

  private void updateTitleWeight(List<WeightedProject> projects, List<String> words, int weightValue) {
    for (WeightedProject weightedProject : projects) {
      Project project = weightedProject.getProject();
      int weight = weightedProject.getWeight();

      String name = project.getName().getName().toLowerCase();

      for (String word: words) {
        if(name.contains(word)){
          weight += weightValue;
        }
      }

      weightedProject.setWeight(weight);
    }
  }
}


