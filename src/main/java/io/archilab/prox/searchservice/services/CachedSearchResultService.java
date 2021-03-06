package io.archilab.prox.searchservice.services;

import io.archilab.prox.searchservice.project.Project;
import io.archilab.prox.searchservice.project.ProjectRepository;
import io.archilab.prox.searchservice.project.WeightedProject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CachedSearchResultService {

  private final int perfectMatchBoost = 5;
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

    this.cache = new ArrayList<>();

    this.projectRepository.findAll().forEach(this.cache::add);

    this.log.info("SearchService: Projects loaded");
  }

  public List<Project> getProjects(String searchText) {
    if (searchText == null || searchText.length() < 2) {
      return this.cache;
    }

    searchText = searchText.toLowerCase();

    var result = new ArrayList<>(this.cache);

    this.log.info(searchText);

    List<Filter> filters = new ArrayList<>();
    filters.add(
        new Filter(
            this.env.getProperty("searchNames.status", "Status"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.status", "1000")),
            (project -> project.getStatus() == null ? "" : project.getStatus().toString())));

    filters.add(
        new Filter(
            this.env.getProperty("searchNames.title", "Titel"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.title", "50")),
            (project -> project.getName() == null ? "" : project.getName().getName())));

    filters.add(
        new Filter(
            this.env.getProperty("searchNames.supervisorName", "Betreuer"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.supervisorName", "50")),
            (project ->
                project.getSupervisorName() == null
                    ? ""
                    : project.getSupervisorName().getSupervisorName())));

    filters.add(
        new Filter(
            this.env.getProperty("searchNames.shortDescription", "Kurzbeschreibung"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.shortDescription", "1")),
            (project ->
                project.getShortDescription() == null
                    ? ""
                    : project.getShortDescription().getShortDescription())));

    filters.add(
        new Filter(
            this.env.getProperty("searchNames.description", "Beschreibung"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.description", "1")),
            (project ->
                project.getDescription() == null
                    ? ""
                    : project.getDescription().getDescription())));

    filters.add(
        new Filter(
            this.env.getProperty("searchNames.requirements", "Voraussetzung"),
            Integer.valueOf(this.env.getProperty("searchMultiplier.requirements", "10")),
            (project ->
                project.getRequirement() == null
                    ? ""
                    : project.getRequirement().getRequirement())));

    filters.add(
        new Filter(
            Integer.valueOf(this.env.getProperty("searchMultiplier.tag", "25")),
            this.env.getProperty("searchNames.tag", "Tag"),
            (project -> project.GetTagNames())));

    for (Filter filter : filters) {
      var filterValues = this.getFilter(searchText, filter.filterKey);
      if (filterValues.hasValues) {
        result = new ArrayList<>(this.filterBy(result, filterValues.values, filter.getFilterValue));
        searchText = filterValues.filter;
      }
    }

    this.log.info(searchText);

    List<String> words = new ArrayList<>();

    Pattern reg = Pattern.compile("(\\w+)");
    Matcher m = reg.matcher(searchText);
    while (m.find()) {
      String getWord = m.group();
      if (getWord.length() >= 2) {
        words.add(m.group());
      }

      this.log.info("Word: " + m.group());
    }

    if (words.size() == 0) {
      return result;
    }

    // Weight
    List<WeightedProject> weighted = new ArrayList<>();
    result.forEach(p -> weighted.add(new WeightedProject(p)));

    for (Filter filter : filters) {
      this.updateWeight(weighted, words, filter.weight, filter.getFilterValue);
    }

    Collections.sort(weighted);

    this.log.info("result: " + weighted);

    return weighted.stream()
        .filter(p -> p.getWeight() > 0)
        .map(p -> p.getProject())
        .collect(Collectors.toList());
  }

  // Filter
  public FilterResult getFilter(String searchString, String key) {
    List<String> result = new ArrayList<>();

    var pattern = key.toLowerCase() + "\\s*=\\s*['\"](.*?)['\"]";

    Pattern pairRegex = Pattern.compile(pattern);
    Matcher matcher = pairRegex.matcher(searchString);

    while (matcher.find()) {
      var match = matcher.group(0);
      String value = matcher.group(1);

      if (value.length() >= 2) {
        result.add(value);
      }

      searchString = searchString.replace(match, "");
    }

    return new FilterResult(result, searchString);
  }

  private List<Project> filterBy(
      List<Project> projects,
      List<String> filters,
      Function<Project, List<String>> getFilterValue) {

    var result = new ArrayList<Project>();

    for (Project project : projects) {

      boolean canAdd = this.filterProject(project, filters, getFilterValue);

      if (canAdd) {
        result.add(project);
      }
    }

    return result;
  }

  private boolean filterProject(
      Project project, List<String> filters, Function<Project, List<String>> getFilterValue) {

    List<String> textValues = getFilterValue.apply(project);

    for (String filter : filters) {

      boolean containsFilter = false;

      for (String text : textValues) {

        if (text == null) {
          continue;
        }

        text = text.toLowerCase();

        if (text.contains(filter)) {
          containsFilter = true;
        }
      }

      if (containsFilter == false) {
        return false;
      }
    }

    return true;
  }

  // Weight
  private void updateWeight(
      List<WeightedProject> projects,
      List<String> words,
      int weightValue,
      Function<Project, List<String>> getFilterValue) {
    for (WeightedProject weightedProject : projects) {
      Project project = weightedProject.getProject();
      int weight = weightedProject.getWeight();

      List<String> textList = getFilterValue.apply(project);

      for (String text : textList) {

        if (text == null) {
          continue;
        }

        text = text.toLowerCase();

        for (String word : words) {
          if (text.equals(word)) {
            weight += weightValue * this.perfectMatchBoost;
          } else if (text.contains(word)) {
            weight += weightValue;
          }
        }
      }

      weightedProject.setWeight(weight);
    }
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

      this.getFilterValue =
          (project -> {
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
}
