package io.archilab.prox.searchservice.services;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.RowSet;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
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
import io.archilab.prox.searchservice.services.CachedSearchResultService.FilterResult;
import lombok.var;

@Service
@Transactional

public class SearchResultService {

  @Autowired
  private ProjectRepository projectRepository;

  @PersistenceContext
  private EntityManager entityManager;


  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private Environment env;

  @Autowired
  private CachedSearchResultService cachedSearchResultService;


  Logger log = LoggerFactory.getLogger(ProjectRepository.class);


  public Pair<List<ProjectSearchData>, Long> findPaginated(Pageable pageable, String searchText)
      throws Exception {

    log.info(" " + projectRepository.count());
    if (projectRepository.count() == 0) {
      // for(int i=0;i<800;i++)
      // {
      // Project testpp = new Project(UUID.randomUUID() , new ProjectName(String.valueOf(i)), new
      // ProjectShortDescription("sfeeeeeeeeee"),
      // new ProjectDescription("eeeff"), ProjectStatus.VERFÜGBAR, new
      // ProjectRequirement("fsertgr5z5z5zrr5zr5zr5zr5"),
      // new SupervisorName("fffffffffffff") );
      // testpp.getTags().add(e)
      // projectRepository.save(testpp);
      // }
      Project testpp = null;
      testpp = new Project(UUID.randomUUID(), new ProjectName("Hallo hallo hasu haus warum wort"),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);

      testpp = new Project(UUID.randomUUID(),
          new ProjectName("wer wer wer wer Hallo wir hasu haus warum wort"),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);

      testpp = new Project(UUID.randomUUID(),
          new ProjectName("Hallo wir wir wir wir wir wir hasu haus warum wort"),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      projectRepository.save(testpp);

      testpp = new Project(UUID.randomUUID(),
          new ProjectName("Hallo Hallo wer wie wo wer wie wo hasu haus warum wort"),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      testpp.getTags().add(new TagName("tt33"));
      testpp.getTags().add(new TagName("tt44"));
      projectRepository.save(testpp);

      testpp = new Project(UUID.randomUUID(), new ProjectName(" Hallo hasu  warum wort"),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);

      testpp = new Project(UUID.randomUUID(), new ProjectName("Hallo   haus warum "),
          new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR,
          new ProjectRequirement("qweqwe"), new SupervisorName("fffffffffffff"));
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag3"));
      projectRepository.save(testpp);

      log.info("new data");

    }



    String filter = searchText;

    if (filter == null || filter.length() < 2)
      throw new Exception("bad search string");

    filter = filter.toLowerCase();

    // Supervisor
    var titleFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.title"));
    filter = titleFilter.filter;
    var supervisorFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.supervisorName"));
    filter = supervisorFilter.filter;
    var requirementsFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.requirements"));
    filter = requirementsFilter.filter;
    var shortDescriptionFilter = cachedSearchResultService.getFilter(filter,
        env.getProperty("searchNames.shortDescription"));
    filter = shortDescriptionFilter.filter;
    var descriptionFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.description"));
    filter = descriptionFilter.filter;
    var tagsFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.tag"));
    filter = tagsFilter.filter;

    var statusFilter =
        cachedSearchResultService.getFilter(filter, env.getProperty("searchNames.status"));
    filter = tagsFilter.filter;

    log.info("rest filter " + filter);

    List<String> words = new ArrayList<>();

    Pattern reg = Pattern.compile("(\\w+)");
    Matcher m = reg.matcher(filter);
    while (m.find()) {
      words.add(m.group());
      // log.info("Word: " + m.group());
    }


    int tagMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.tag"));
    int descriptionMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.description"));
    int shortDescriptionMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.shortDescription"));
    int requirementsMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.requirements"));
    int supervisorNameMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.supervisorName"));
    int titleMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.title"));

    String forcedTagParts = "";
    if (tagsFilter.hasValues) {
      String tags = "";
      for (int i = 0; i < tagsFilter.values.size(); i++) {
        String tagX = tagsFilter.values.get(i);
        String comma = "";
        if (i != 0) {
          comma = ",";
        }
        tags += comma + "'" + tagX + "'";

      }
      forcedTagParts =
          " (( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND (pt.tag_name in ("
              + tags + ")) ) > 0) ";
    } else {
      forcedTagParts = " true ";
    }

    String andStatus = "";
    if (statusFilter.hasValues) {
      ProjectStatus status_data = ProjectStatus.valueOf(statusFilter.values.get(0).toUpperCase());
      andStatus = "and p.status = " + status_data.getValue();
    }

    String forcedTitleParts = " and " + buildWhereClause(titleFilter, "name");
    String forcedSupervisorParts = " and " + buildWhereClause(supervisorFilter, "supervisor_name");
    String forcedRequirementsParts = " and " + buildWhereClause(requirementsFilter, "requirement");
    String forcedShortDescriptionParts =
        " and " + buildWhereClause(shortDescriptionFilter, "short_description");
    String forcedDescriptionParts = " and " + buildWhereClause(descriptionFilter, "description");

    String where_part =
        forcedTagParts + forcedTitleParts + forcedSupervisorParts + forcedRequirementsParts
            + forcedShortDescriptionParts + forcedDescriptionParts + andStatus;


    String counting_part = "";

    // name - title
    if (titleFilter.hasValues || !words.isEmpty()) {
      List<String> titleList = new ArrayList<String>();
      for (String tag_word : words) {
        titleList.add(tag_word);
      }
      for (String filter_word : titleFilter.values) {
        if (!titleList.contains(filter_word)) {
          titleList.add(filter_word);
        }
      }
      String titleParts = "";
      for (int i = 0; i < titleList.size(); i++) {
        String term = titleList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        titleParts += plus + " ((length(p.name) - length(replace(p.name, '" + term
            + "', '')) )::int  / length('" + term + "'))";

      }
      counting_part += "( " + titleParts + " *" + titleMultiplier + ")";

    }

    // requirement
    if (requirementsFilter.hasValues || !words.isEmpty()) {
      List<String> requirementsList = new ArrayList<String>();
      for (String tag_word : words) {
        requirementsList.add(tag_word);
      }
      for (String filter_word : requirementsFilter.values) {
        if (!requirementsList.contains(filter_word)) {
          requirementsList.add(filter_word);
        }
      }
      String requirementParts = "";
      for (int i = 0; i < requirementsList.size(); i++) {
        String term = requirementsList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        requirementParts += plus + " ((length(p.requirement) - length(replace(p.requirement, '"
            + term + "', '')) )::int  / length('" + term + "'))";

      }
      counting_part += "+( " + requirementParts + " *" + requirementsMultiplier + ")";

    }

    // supervisor_name
    if (supervisorFilter.hasValues || !words.isEmpty()) {
      List<String> supervisorList = new ArrayList<String>();
      for (String tag_word : words) {
        supervisorList.add(tag_word);
      }
      for (String filter_word : supervisorFilter.values) {
        if (!supervisorList.contains(filter_word)) {
          supervisorList.add(filter_word);
        }
      }
      String supervisorNameParts = "";
      for (int i = 0; i < supervisorList.size(); i++) {
        String term = supervisorList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        supervisorNameParts +=
            plus + " ((length(p.supervisor_name) - length(replace(p.supervisor_name, '" + term
                + "', '')) )::int  / length('" + term + "'))";

      }
      counting_part += "+( " + supervisorNameParts + " *" + supervisorNameMultiplier + ")";

    }

    // short_description
    if (shortDescriptionFilter.hasValues || !words.isEmpty()) {
      List<String> shortDescriptionList = new ArrayList<String>();
      for (String tag_word : words) {
        shortDescriptionList.add(tag_word);
      }
      for (String filter_word : shortDescriptionFilter.values) {
        if (!shortDescriptionList.contains(filter_word)) {
          shortDescriptionList.add(filter_word);
        }
      }
      String shortDescriptionParts = "";
      for (int i = 0; i < shortDescriptionList.size(); i++) {
        String term = shortDescriptionList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        shortDescriptionParts +=
            plus + " ((length(p.short_description) - length(replace(p.short_description, '" + term
                + "', '')) )::int  / length('" + term + "'))";

      }
      counting_part += "+( " + shortDescriptionParts + " *" + shortDescriptionMultiplier + ")";

    }

    // description
    if (descriptionFilter.hasValues || !words.isEmpty()) {
      List<String> descriptionList = new ArrayList<String>();
      for (String tag_word : words) {
        descriptionList.add(tag_word);
      }
      for (String filter_word : descriptionFilter.values) {
        if (!descriptionList.contains(filter_word)) {
          descriptionList.add(filter_word);
        }
      }
      String descriptionParts = "";
      for (int i = 0; i < descriptionList.size(); i++) {
        String descTerm = descriptionList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        descriptionParts += plus + " ((length(p.description) - length(replace(p.description, '"
            + descTerm + "', '')) )::int  / length('" + descTerm + "'))";

      }
      counting_part += "+( " + descriptionParts + " *" + descriptionMultiplier + ")";

    }


    // tags
    if (tagsFilter.hasValues || !words.isEmpty()) {
      List<String> tagsList = new ArrayList<String>();
      for (String tag_word : words) {
        tagsList.add(tag_word);
      }
      for (String filter_word : tagsFilter.values) {
        if (!tagsList.contains(filter_word)) {
          tagsList.add(filter_word);
        }
      }
      String tags = "";
      for (int i = 0; i < tagsList.size(); i++) {
        String tagX = tagsList.get(i);
        String comma = "";
        if (i != 0) {
          comma = ",";
        }

        tags += comma + "'" + tagX + "'";

      }
      counting_part +=
          "+( ( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND (pt.tag_name in ("
              + tags + ")) ) *" + tagMultiplier + ")";

    }

    String paging_part = " LIMIT " + pageable.getPageSize() + "  OFFSET " + pageable.getOffset();

    String from_part = "SELECT  p.id as id, (" + counting_part
        + ") as priority from project p where " + where_part;
    String full_query = "Select result_query.id from (" + from_part
        + " ORDER BY priority desc ) as result_query " + paging_part + ";";

    // log.info(full_query);

    List<UUID> result = jdbcTemplate.queryForList(full_query, UUID.class);

    String full_query_no_paging =
        "Select count(*) from (" + from_part + " ORDER BY priority desc ) as result_query ;";

    long count_data = jdbcTemplate.queryForObject(full_query_no_paging, Long.class);


    List<ProjectSearchData> retList = new ArrayList<>();

    for (UUID uuid : result) {
      retList.add(new ProjectSearchData(uuid));
      log.info("id   " + uuid.toString());;
    }


    Pair<List<ProjectSearchData>, Long> pair = Pair.of(retList, count_data);

    return pair;

  }

  private String buildWhereClause(FilterResult filter, String dbFieldName) {
    String queryPart = "";
    if (filter.hasValues) {
      for (int i = 0; i < filter.values.size(); i++) {
        String part = filter.values.get(i);
        if (i != 0) {
          queryPart += " or ";
        }
        queryPart += " lower(p." + dbFieldName + ") like '%" + part + "%' ";
      }
      queryPart = "(" + queryPart + ")";
    } else {
      queryPart = " TRUE ";
    }
    return queryPart;
  }


  public Long getTotalElements() {
    log.info("" + projectRepository.count());
    return projectRepository.count();
  }

  public Page<UUID> findAll(Pageable pageable, String searchText) {

    Page<Project> projects = projectRepository.findAll(pageable);

    return projects.map(project -> project.getId());
  }



}
