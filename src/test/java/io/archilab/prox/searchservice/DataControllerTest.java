package io.archilab.prox.searchservice;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import io.archilab.prox.searchservice.controller.SearchController;
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
import io.archilab.prox.searchservice.services.CachedSearchResultService;
import io.archilab.prox.searchservice.services.SearchResultService;

@RunWith(SpringRunner.class)
@SpringBootTest

public class DataControllerTest {

  Logger log = LoggerFactory.getLogger(DataControllerTest.class);


  @Autowired
  private SearchResultService searchResultService;
  
  @Autowired
  private CachedSearchResultService service2;
  
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  @Autowired
  private ProjectRepository projectRepository;


  @Test
  public void fullSearchTest() {
    
    Pageable pageable = PageRequest.of(0, 10);
    
    String searchText = "haus";
    

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

    

    Pair<List<ProjectSearchData>, Long> resultData = null;
    try {
      resultData = searchResultService.findPaginated(pageable, searchText);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    List<ProjectSearchData> resultPage = resultData.getFirst();
    
    long count = resultData.getSecond();
    for (ProjectSearchData ele : resultPage) {

      log.info(ele.getId());
    }
  
    log.info("count "+count);
    

  }
}
