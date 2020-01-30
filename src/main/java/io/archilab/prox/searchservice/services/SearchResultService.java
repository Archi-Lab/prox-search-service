package io.archilab.prox.searchservice.services;

import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.project.ProjectStatus;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SearchResultService {

  private final String status;
  private final String title;
  private final String supervisorName;
  private final String description;
  private final String shortDescription;
  private final String requirements;
  private final String tags;
  private final String words;
  private final int titleMultiplier;
  private final int supervisorNameMultiplier;
  private final int descriptionMultiplier;
  private final int shortDescriptionMultiplier;
  private final int requirementsMultiplier;
  private final int tagsMultiplier;
  private final int boostTags;
  Logger log = LoggerFactory.getLogger(SearchResultService.class);
  @PersistenceContext private EntityManager entityManager;
  // @Autowired
  private JdbcTemplate jdbcTemplate;

  public SearchResultService(JdbcTemplate jdbcTemplate, Environment env) {
    this.jdbcTemplate = jdbcTemplate;

    this.status = env.getProperty("searchNames.status", "Status");

    this.title = env.getProperty("searchNames.title", "Titel");

    this.supervisorName = env.getProperty("searchNames.supervisorName", "Betreuer");

    this.description = env.getProperty("searchNames.description", "Beschreibung");

    this.shortDescription = env.getProperty("searchNames.shortDescription", "Kurzbeschreibung");

    this.requirements = env.getProperty("searchNames.requirements", "Voraussetzung");

    this.tags = env.getProperty("searchNames.tag", "Tag");

    this.words = "Words";

    this.titleMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.title", "50"));
    this.supervisorNameMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.supervisorName", "50"));
    this.descriptionMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.shortDescription", "1"));
    this.shortDescriptionMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.description", "1"));
    this.requirementsMultiplier =
        Integer.valueOf(env.getProperty("searchMultiplier.requirements", "10"));
    this.tagsMultiplier = Integer.valueOf(env.getProperty("searchMultiplier.tag", "25"));

    this.boostTags = Integer.valueOf(env.getProperty("searchMultiplier.boostTags", "2"));
  }

  public Pair<List<ProjectSearchData>, Long> findPaginated(Pageable pageable, String searchText)
      throws Exception {

    searchText = searchText.toLowerCase();

    Map<String, ResultList> resultList = this.prepareFilterLists(searchText);

    if (resultList == null) {
      throw new Exception("bad search string");
    }

    List<SqlParameterValue> paramValues = new ArrayList<>();

    String returnParts = "";

    String mustHaveTagParts = "";
    if (!resultList.get(this.tags).values.isEmpty()) {
      String tags = "";
      for (int i = 0; i < resultList.get(this.tags).values.size(); i++) {
        String tagX = resultList.get(this.tags).values.get(i);
        String comma = "";
        if (i != 0) {
          comma = " or ";
        }
        tags += comma + " POSITION( ?  IN pt.tag_name) > 0 ";

        paramValues.add(new SqlParameterValue(Types.VARCHAR, tagX));
      }
      mustHaveTagParts =
          " (( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND ( "
              + tags
              + " ) ) >= "
              + resultList.get(this.tags).values.size()
              + " )";
    } else {
      mustHaveTagParts = " true ";
    }

    String andStatus = "";
    if (!resultList.get(this.status).values.isEmpty()) {

      ProjectStatus status_data =
          ProjectStatus.valueOf(resultList.get(this.status).values.get(0).toUpperCase());
      andStatus = "and p.status = " + status_data.getValue();
    }

    String forcedTitleParts =
        " and " + this.buildWhereClause(resultList.get(this.title), "name", paramValues);
    String forcedSupervisorParts =
        " and "
            + this.buildWhereClause(
                resultList.get(this.supervisorName), "supervisor_name", paramValues);
    String forcedRequirementsParts =
        " and "
            + this.buildWhereClause(resultList.get(this.requirements), "requirement", paramValues);
    String forcedShortDescriptionParts =
        " and "
            + this.buildWhereClause(
                resultList.get(this.shortDescription), "short_description", paramValues);
    String forcedDescriptionParts =
        " and "
            + this.buildWhereClause(resultList.get(this.description), "description", paramValues);

    String where_part =
        mustHaveTagParts
            + forcedTitleParts
            + forcedSupervisorParts
            + forcedRequirementsParts
            + forcedShortDescriptionParts
            + forcedDescriptionParts
            + andStatus;

    String counting_part = "";

    counting_part = " 0 "; // wichtig, weil die Funktion prepareSelectStringCount immer am Anfang
    // ein plus einbaut.

    // wenn status angegeben wird dann muss priority +1 gerechent werdne, da es ansonsten sein kann,
    // dass das projekt
    // 0 priority bekommt, wenn nur nach status gesucht wird.
    if (!resultList.get(this.status).values.isEmpty()) {
      counting_part += " + 1 ";
    }

    returnParts =
        this.prepareSelectStringCount(
            "name", resultList.get(this.title), resultList.get(this.words).values, paramValues);
    counting_part += returnParts;

    returnParts =
        this.prepareSelectStringCount(
            "requirement",
            resultList.get(this.requirements),
            resultList.get(this.words).values,
            paramValues);
    counting_part += returnParts;

    returnParts =
        this.prepareSelectStringCount(
            "supervisor_name",
            resultList.get(this.supervisorName),
            resultList.get(this.words).values,
            paramValues);
    counting_part += returnParts;

    returnParts =
        this.prepareSelectStringCount(
            "short_description",
            resultList.get(this.shortDescription),
            resultList.get(this.words).values,
            paramValues);
    counting_part += returnParts;

    returnParts =
        this.prepareSelectStringCount(
            "description",
            resultList.get(this.description),
            resultList.get(this.words).values,
            paramValues);
    counting_part += returnParts;

    // tags

    if (!resultList.get(this.tags).values.isEmpty()
        || !resultList.get(this.words).values.isEmpty()) {
      List<SqlParameterValue> paramValuesTemp1 = new ArrayList<>();

      String tags1 = "";
      String tags2 = "";

      List<String> tagsList = new ArrayList<>();
      for (String tag_word : resultList.get(this.words).values) {
        tagsList.add(tag_word);
      }
      for (String filter_word : resultList.get(this.tags).values) {
        if (!tagsList.contains(filter_word)) {
          tagsList.add(filter_word);
        }
      }

      for (int i = 0; i < tagsList.size(); i++) {
        String tagX = tagsList.get(i);
        if (i != 0) {
          tags1 += ",";
          tags2 += "|" + tagX;
        } else {
          tags2 += tagX;
        }
        tags1 += "?";

        paramValuesTemp1.add(new SqlParameterValue(Types.VARCHAR, tagX));
      }
      paramValues.addAll(paramValuesTemp1);
      paramValues.add(new SqlParameterValue(Types.VARCHAR, tags2));

      counting_part +=
          " + (select  sum( "
              + " CASE WHEN pt.tag_name in ("
              + tags1
              + ") THEN "
              + resultList.get(this.tags).weight * this.boostTags
              + "  WHEN ( pt.tag_name  ~ ? ) THEN "
              + resultList.get(this.tags).weight
              + " ELSE 0 END  "
              + "   ) from project_tags as pt where pt.project_id = p.id  ) ";
    }

    String paging_part = " LIMIT ?  OFFSET ? ";
    paramValues.add(new SqlParameterValue(Types.INTEGER, pageable.getPageSize()));
    paramValues.add(new SqlParameterValue(Types.INTEGER, pageable.getOffset()));

    String from_part =
        "select res_1.id, res_1.priority from (SELECT  p.id as id, ("
            + counting_part
            + ") as priority from project p where "
            + where_part
            + " ) as res_1  where res_1.priority > 0";
    String full_query =
        "Select result_query.id from ("
            + from_part
            + " ORDER BY priority desc ) as result_query "
            + paging_part
            + ";";

    // log.info(full_query);

    Object[] preparedValues = new Object[paramValues.size()];
    for (int i = 0; i < paramValues.size(); i++) {
      preparedValues[i] = paramValues.get(i);
    }

    List<UUID> result = this.jdbcTemplate.queryForList(full_query, preparedValues, UUID.class);

    String full_query_no_paging =
        "Select count(*) from (" + from_part + " ORDER BY priority desc ) as result_query ;";

    // -2 wegen paging
    int pagingRemove = 2;
    Object[] preparedValues2 = new Object[paramValues.size() - pagingRemove];
    for (int i = 0; i < paramValues.size() - pagingRemove; i++) {
      preparedValues2[i] = paramValues.get(i);
    }

    long count_data =
        this.jdbcTemplate.queryForObject(full_query_no_paging, preparedValues2, Long.class);

    List<ProjectSearchData> retList = new ArrayList<>();

    for (UUID uuid : result) {
      retList.add(new ProjectSearchData(uuid));
      // log.info("id " + uuid.toString());;
    }

    Pair<List<ProjectSearchData>, Long> pair = Pair.of(retList, count_data);

    return pair;
  }

  private String buildWhereClause(
      ResultList filter, String dbFieldName, List<SqlParameterValue> paramValues) {
    String queryPart = "";
    if (!filter.values.isEmpty()) {
      for (int i = 0; i < filter.values.size(); i++) {
        String part = filter.values.get(i);
        if (i != 0) {
          queryPart += " and ";
        }
        queryPart += " POSITION( ?  IN lower(lower(p." + dbFieldName + "))) > 0 ";

        paramValues.add(new SqlParameterValue(Types.VARCHAR, part));
      }
      queryPart = "(" + queryPart + ")";
    } else {
      queryPart = " TRUE ";
    }
    return queryPart;
  }

  private String prepareSelectStringCount(
      String dbFieldName,
      ResultList categoryResultList,
      List<String> words,
      List<SqlParameterValue> paramValues) {

    String contentParts = "";
    if (!categoryResultList.values.isEmpty() || !words.isEmpty()) {
      List<String> contentList = new ArrayList<>();
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
        contentParts +=
            plus
                + "  ((length(lower(p."
                + dbFieldName
                + ")) - length(replace(lower(p."
                + dbFieldName
                + "), "
                + " ? , '')) )::int  / length( ? )) ";

        paramValues.add(new SqlParameterValue(Types.VARCHAR, descTerm));
        paramValues.add(new SqlParameterValue(Types.VARCHAR, descTerm));
      }
    } else {
      contentParts = " 0 ";
    }

    return " +( ( (" + contentParts + ") *" + categoryResultList.weight + ")  )";
  }

  public Map<String, ResultList> prepareFilterLists(String searchText) {
    if (searchText == null || searchText.length() < 2) {
      return null;
    }

    Map<String, ResultList> return_lists = new HashMap<>();

    String key = "";
    int weight = 1;
    Pair<String, List<String>> pair;

    key = this.status;
    weight = 1;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.title;
    weight = this.titleMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.supervisorName;
    weight = this.supervisorNameMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.shortDescription;
    weight = this.shortDescriptionMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.description;
    weight = this.descriptionMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.requirements;
    weight = this.requirementsMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    key = this.tags;
    weight = this.tagsMultiplier;
    pair = this.doFiltering(searchText, key);
    searchText = pair.getFirst();
    return_lists.put(key, new ResultList(pair.getSecond(), key, weight));

    // free words
    List<String> words = new ArrayList<>();

    Pattern reg = Pattern.compile("(\\w+)");
    Matcher m = reg.matcher(searchText);
    while (m.find()) {
      String wordCandidate = m.group();
      if (wordCandidate.length() >= 2) {
        words.add(wordCandidate);
      }
    }

    return_lists.put("Words", new ResultList(words, "Words", 1));

    return return_lists;
  }

  public Pair<String, List<String>> doFiltering(String searchString, String key) {
    List<String> result = new ArrayList<>();

    var pattern = key.toLowerCase() + "\\s*=\\s*['\"](.*?)['\"]";

    Pattern pairRegex = Pattern.compile(pattern);
    Matcher matcher = pairRegex.matcher(searchString);

    while (matcher.find()) {
      var match = matcher.group(0);
      var value = matcher.group(1);
      if (value.length() >= 2) {
        result.add(value);
      }

      searchString = searchString.replace(match, "");
    }

    return Pair.of(searchString, result);
  }

  public class ResultList {

    public final List<String> values;
    public final String key;
    public final int weight;

    public ResultList(List<String> values, String key, int weight) {
      this.values = values;
      this.key = key;
      this.weight = weight;
    }
  }
}
