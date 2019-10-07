package io.archilab.prox.searchservice.services;

import io.archilab.prox.searchservice.project.*;
import lombok.var;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.text.html.HTML;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class CachedSearchResultService {


  Logger log = LoggerFactory.getLogger(ProjectRepository.class);

  private ProjectRepository projectRepository;
  private List<Project> cache;
  private Environment env;

  @Autowired
  public CachedSearchResultService(ProjectRepository projectRepository, Environment environment) {
    this.projectRepository = projectRepository;
    this.env = environment;

    this.load();
  }

  public void load() {

    var cache = new ArrayList<Project>();

    this.projectRepository.findAll().forEach(cache::add);

    this.cache = cache;

    log.info("SearchService: Projects loaded");
  }

  public Long getTotalElements() {
    return projectRepository.count();
  }

  public List<ProjectSearchData> findPaginated(Pageable pageable, String searchText)
          throws Exception {

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

  public List<Project> getProjects(String searchText) {
    if (searchText == null || searchText.length() < 2)
      return this.cache;

    searchText = searchText.toLowerCase();

    var result = new ArrayList<Project>(this.cache);

    log.info(searchText);

    List<Filter> filters = new ArrayList<>();
    filters.add(new Filter(env.getProperty("searchNames.status", "Status"), Integer.valueOf(env.getProperty("searchMultiplier.status", "1000")), (project -> project.getStatus().name())));
    filters.add(new Filter(env.getProperty("searchNames.title", "Titel"), Integer.valueOf(env.getProperty("searchMultiplier.title", "50")),  (project -> project.getName().getName())));
    filters.add(new Filter(env.getProperty("searchNames.supervisorName", "Betreuer"), Integer.valueOf(env.getProperty("searchMultiplier.supervisorName", "50")), (project -> project.getSupervisorName().getSupervisorName())));
    filters.add(new Filter(env.getProperty("searchNames.description", "Beschreibung"), Integer.valueOf(env.getProperty("searchMultiplier.description", "1")), (project -> project.getDescription().getDescription())));
    filters.add(new Filter(env.getProperty("searchNames.shortDescription", "Kurzbeschreibung"), Integer.valueOf(env.getProperty("searchMultiplier.shortDescription", "1")), (project -> project.getShortDescription().getShortDescription())));
    filters.add(new Filter(env.getProperty("searchNames.requirements", "Voraussetzung"), Integer.valueOf(env.getProperty("searchMultiplier.requirements", "10")), (project -> project.getRequirement().getRequirement())));
    filters.add(new Filter(Integer.valueOf(env.getProperty("searchMultiplier.tag", "10")), env.getProperty("searchNames.tag", "Tag"), (project -> project.getTags().stream().map(t -> t.getTagName()).collect(Collectors.toList()))));

    for (Filter filter : filters) {
      log.info(filter.filterKey + " - " + searchText);

      var filterValues = this.getFilter(searchText, filter.filterKey);
      if (filterValues.hasValues) {
        result = new ArrayList<>(this.filterBy(result, filterValues.values, filter.getFilterValue));
        searchText = filterValues.filter;
      }
    }


    log.info(searchText);

    List<String> words = new ArrayList<>();

    Pattern reg = Pattern.compile("(\\w+)");
    Matcher m = reg.matcher(searchText);
    while (m.find()) {
      words.add(m.group());
      log.info("Word: " + m.group());
    }

    // Weight
    List<WeightedProject> weighted = new ArrayList<>();
    result.forEach(p -> weighted.add(new WeightedProject(p)));

    for (Filter filter : filters) {
      this.updateWeight(weighted, words, filter.weight, filter.getFilterValue);
    }

    Collections.sort(weighted);

    log.info("result: " + weighted);

    if(weighted.stream().anyMatch(p -> p.getWeight() > 0)){
      return weighted.stream().filter(p -> p.getWeight() > 0).map(p -> p.getProject()).collect(Collectors.toList());
    }

    return weighted.stream().map(p -> p.getProject()).collect(Collectors.toList());
  }

  // Filter
  public FilterResult getFilter(String searchString, String key) {
    List<String> result = new ArrayList<>();

    var pattern = key.toLowerCase() + "\\s*=\\s*['\"](.*)['\"]";

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

  private List<Project> filterBy(List<Project> projects, List<String> filters, Function<Project, List<String>> getFilterValue) {

    var result = new ArrayList<Project>();

    for (Project project : projects) {

      boolean canAdd = this.filterProject(project, filters, getFilterValue);

      if(canAdd)
        result.add(project);
    }

    return result;
  }

  private boolean filterProject(Project project, List<String> filters, Function<Project, List<String>> getFilterValue){

    List<String> textValues = getFilterValue.apply(project);

    for (String text : textValues){

      text = text.toLowerCase();

      for (String filter : filters) {
        if (text.contains(filter)) {
          return true;
        }
      }
    }

    return false;
  }

  public class FilterResult {
    Boolean hasValues;
    List<String> values;
    String filter;

    FilterResult(List<String> values, String filter) {
      this.values = values;
      this.filter = filter;
      this.hasValues = values.size() > 0;
    }
  }

  private class Filter {
    String filterKey;
    Function<Project, List<String>> getFilterValue;
    int weight;

    Filter(String filterKey, int weight, Function<Project, String> getFilterValue) {
      this.filterKey = filterKey;
      this.weight = weight;

      this.getFilterValue = (project -> {
        List<String> list = new ArrayList<>();
        list.add(getFilterValue.apply(project));
        return list;
      });
    }

    // Key and weight swapped so that the constructors are different ( -> syntax error ...)
    Filter(int weight, String filterKey, Function<Project, List<String>> getFilterValue) {
      this.filterKey = filterKey;
      this.getFilterValue = getFilterValue;
      this.weight = weight;
    }
  }


  // Weight
  private void updateWeight(List<WeightedProject> projects, List<String> words, int weightValue, Function<Project, List<String>> getFilterValue) {
    for (WeightedProject weightedProject : projects) {
      Project project = weightedProject.getProject();
      int weight = weightedProject.getWeight();


      List<String> textList = getFilterValue.apply(project);

      for (String text : textList){

        text = text.toLowerCase();

        for (String word : words) {
          if(text == word){
            weight += weightValue * 3;
          }
          else if (text.contains(word)) {
            weight += weightValue;
          }
        }
      }

      weightedProject.setWeight(weight);
    }
  }
}


