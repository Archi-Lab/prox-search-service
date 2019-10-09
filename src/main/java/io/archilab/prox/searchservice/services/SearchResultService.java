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
import org.springframework.jdbc.core.SqlParameterValue;
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
import io.archilab.prox.searchservice.services.CachedSearchResultService.ResultList;
import lombok.var;
import java.sql.Types;

@Service
@Transactional

public class SearchResultService {

  @PersistenceContext
  private EntityManager entityManager;


//  @Autowired
  private JdbcTemplate jdbcTemplate;

//  @Autowired
  private Environment env;

//  @Autowired
  private CachedSearchResultService cachedSearchResultService;
  
  private final String status;
  private final String title;
  private final String supervisorName;
  private final String description;
  private final String shortDescription;
  private final String requirements;
  private final String tags;
  private final String words;

  Logger log = LoggerFactory.getLogger(ProjectRepository.class);
  
  public SearchResultService(JdbcTemplate jdbcTemplate,Environment env, CachedSearchResultService cachedSearchResultService)
  {
    this.jdbcTemplate=jdbcTemplate;
    this.env=env;
    this.cachedSearchResultService=cachedSearchResultService;
    
    status = env.getProperty("searchNames.status", "Status");
    
    title = env.getProperty("searchNames.title", "Titel");
    
    supervisorName = env.getProperty("searchNames.supervisorName", "Betreuer");
    
    description = env.getProperty("searchNames.description", "Beschreibung");
    
    shortDescription = env.getProperty("searchNames.shortDescription", "Kurzbeschreibung");

    requirements = env.getProperty("searchNames.requirements", "Voraussetzung");
    
    tags = env.getProperty("searchNames.tag", "Tag");
    
    words = "Words";
    
  }

  public Pair<List<ProjectSearchData>, Long> findPaginated(Pageable pageable, String searchText)
      throws Exception {
    
    
    Map<String, ResultList> resultList = cachedSearchResultService.prepareFilterLists(searchText);
    
    if(resultList == null)
    {
      throw new Exception("bad search string");
    }

    List<SqlParameterValue> paramValues = new ArrayList<>();
    
    String returnParts="";
    
//    SqlParameterValue s = new SqlParameterValue(java.sql.Types.VARCHAR, "d");

    String forcedTagParts = "";
    if (!resultList.get(this.tags).values.isEmpty()) {
      String tags = "";
      for (int i = 0; i < resultList.get(this.tags).values.size(); i++) {
        String tagX = resultList.get(this.tags).values.get(i);
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
    if (!resultList.get(this.status).values.isEmpty()) {
      
      ProjectStatus status_data = ProjectStatus.valueOf(resultList.get(this.status).values.get(0).toUpperCase());
      andStatus = "and p.status = " + status_data.getValue();
    }

    String forcedTitleParts = " and " + buildWhereClause(resultList.get(this.title), "name");
    String forcedSupervisorParts = " and " + buildWhereClause(resultList.get(this.supervisorName), "supervisor_name");
    String forcedRequirementsParts = " and " + buildWhereClause(resultList.get(this.requirements), "requirement");
    String forcedShortDescriptionParts =
        " and " + buildWhereClause(resultList.get(this.shortDescription), "short_description");
    String forcedDescriptionParts = " and " + buildWhereClause(resultList.get(this.description), "description");

    String where_part =
        forcedTagParts + forcedTitleParts + forcedSupervisorParts + forcedRequirementsParts
            + forcedShortDescriptionParts + forcedDescriptionParts + andStatus;


    String counting_part = "";
    
    counting_part= " 0 ";  // wichtig, wiel die Funktion  prepareSelectStringCount immer am Anfang ein plus einbaut.

    returnParts = prepareSelectStringCount("name",resultList.get(this.title),resultList.get(this.words).values,paramValues);
    counting_part+=returnParts;
    
    returnParts = prepareSelectStringCount("requirement",resultList.get(this.requirements),resultList.get(this.words).values,paramValues);
    counting_part+=returnParts;
    
    returnParts = prepareSelectStringCount("supervisor_name",resultList.get(this.supervisorName),resultList.get(this.words).values,paramValues);
    counting_part+=returnParts;
    
    returnParts = prepareSelectStringCount("short_description",resultList.get(this.shortDescription),resultList.get(this.words).values,paramValues);
    counting_part+=returnParts;

    
    returnParts = prepareSelectStringCount("description",resultList.get(this.description),resultList.get(this.words).values,paramValues);
    counting_part+=returnParts;



    // tags
    
    if (!resultList.get(this.tags).values.isEmpty() || !resultList.get(this.words).values.isEmpty()) {
      List<String> tagsList = new ArrayList<String>();
      for (String tag_word : resultList.get(this.words).values) {
        tagsList.add(tag_word);
      }
      for (String filter_word : resultList.get(this.tags).values) {
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
        paramValues.add(new SqlParameterValue(Types.VARCHAR,  tagX));

        tags += comma + " ? ";  // "'" + tagX + "'";

      }
      counting_part +=
          "+( ( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND (pt.tag_name in ("
              + tags + ")) ) *" + resultList.get(this.tags).weight + ")";

    }

    paramValues.add(new SqlParameterValue(Types.INTEGER,  pageable.getPageSize()));
    paramValues.add(new SqlParameterValue(Types.INTEGER,  pageable.getOffset()));
    String paging_part = " LIMIT ?  OFFSET ? ";

    String from_part = "SELECT  p.id as id, (" + counting_part
        + ") as priority from project p where " + where_part;
    String full_query = "Select result_query.id from (" + from_part
        + " ORDER BY priority desc ) as result_query " + paging_part + ";";

    

    
//    log.info(full_query);
     
    Object[] preparedValues = new Object[paramValues.size()];
    for (int i = 0; i < paramValues.size(); i++) {
      preparedValues[i] = paramValues.get(i);
    }

    List<UUID> result = jdbcTemplate.queryForList(full_query,preparedValues ,UUID.class );
    

    String full_query_no_paging =
        "Select count(*) from (" + from_part + " ORDER BY priority desc ) as result_query ;";
    
    // -2 wegen paging
    int pagingRemove = 2;
    Object[] preparedValues2 = new Object[paramValues.size()-pagingRemove];
    for (int i = 0; i < paramValues.size()-pagingRemove; i++) {
      preparedValues2[i] = paramValues.get(i);
    }

    long count_data = jdbcTemplate.queryForObject(full_query_no_paging,preparedValues2, Long.class);


    List<ProjectSearchData> retList = new ArrayList<>();

    for (UUID uuid : result) {
      retList.add(new ProjectSearchData(uuid));
//      log.info("id   " + uuid.toString());;
    }


    Pair<List<ProjectSearchData>, Long> pair = Pair.of(retList, count_data);

    return pair;

  }

  private String buildWhereClause(ResultList filter, String dbFieldName) {
    String queryPart = "";
    if (!filter.values.isEmpty()) {
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

  private String prepareSelectStringCount(String dbFieldName, ResultList categoryResultList,List<String> words ,  List <SqlParameterValue> paramValues)
  {
    String contentParts = "";
    if (!categoryResultList.values.isEmpty() || !words.isEmpty()) {
      List<String> contentList = new ArrayList<String>();
      for (String tag_word : words) {
        contentList.add(tag_word);
      }
      for (String filter_word : categoryResultList.values) {
        if (!contentList.contains(filter_word)) {
          contentList.add(filter_word);
        }
      }
      
      for (int i = 0; i < contentList.size(); i++) {
        String descTerm = contentList.get(i);
        String plus = "";
        if (i != 0) {
          plus = "+";
        }
        contentParts +=plus + " ((length(p."+dbFieldName+") - length(replace(p."+dbFieldName+", "
            + " ? , '')) )::int  / length( ? )) ";
        
        paramValues.add(new SqlParameterValue(Types.VARCHAR,  descTerm));
        paramValues.add(new SqlParameterValue(Types.VARCHAR,  descTerm));

      }
    }
    else
    {
      contentParts =" 0 ";
    }
    
    return " + ( (" + contentParts + ") *" + categoryResultList.weight + ") ";
  }


}
