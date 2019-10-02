package io.archilab.prox.searchservice.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@Service
@Transactional

public class SearchResultService {
  
  @Autowired
  private ProjectRepository projectRepository;
  
  @PersistenceContext
  private EntityManager entityManager;

  
  Logger log = LoggerFactory.getLogger(ProjectRepository.class);


  public List<ProjectSearchData> findPaginated(Pageable pageable, String searchText) throws Exception {
    
    
    if(projectRepository.count()==0)
    {
//      for(int i=0;i<800;i++)
//      {
//        Project testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444"+i) , new ProjectName(String.valueOf(i)), new ProjectShortDescription("sfeeeeeeeeee"),
//            new ProjectDescription("eeeff"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("fsertgr5z5z5zrr5zr5zr5zr5"),
//            new SupervisorName("fffffffffffff") );
//        testpp.getTags().add(e)
//        projectRepository.save(testpp);
//      }
      Project testpp = null;
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName("Hallo Hallo hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName("wer wer wer wer Hallo wir hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName("Hallo wir wir wir wir wir wir hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      projectRepository.save(testpp);
      
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName("Hallo Hallo wer wie wo wer wie wo hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      testpp.getTags().add(new TagName("tt33"));
      testpp.getTags().add(new TagName("tt44"));
      projectRepository.save(testpp);
      
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName(" Hallo hasu  warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444") , new ProjectName("Hallo   haus warum "), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag3"));
      projectRepository.save(testpp);
      
      log.info("new data");

    }
    
    List<ProjectSearchData> retList = new ArrayList<>();
    
    
    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    
    countQuery.select(criteriaBuilder
        .count(countQuery.from(Project.class)));
      Long count = entityManager.createQuery(countQuery)
        .getSingleResult();

      CriteriaQuery<Project> criteriaQuery = criteriaBuilder
        .createQuery(Project.class);
      Root<Project> from = criteriaQuery.from(Project.class);
      CriteriaQuery<Project> select = criteriaQuery.select(from);

      TypedQuery<Project> typedQuery = entityManager.createQuery(select);
      
      typedQuery.setFirstResult(pageNumber);
      typedQuery.setMaxResults(pageSize);
      
      if(pageNumber < count.intValue())
      {
        List<Project> fooList = typedQuery.getResultList();
 
        for(int i=0;i<fooList.size();i++)
        {
          Project pt = fooList.get(i);
          URI rest = pt.getUri();
          retList.add(new ProjectSearchData(rest));
        }
        
      }
      else
      {
        throw new Exception("page size too big");
      }
      
  
      return retList;

  }


  public Long getTotalElements() 
  {
    log.info(""+projectRepository.count());
    return projectRepository.count();
  }
  
  public Page<URI> findAll(Pageable pageable, String searchText) {

    Page<Project> projects =  projectRepository.findAll(pageable);

    return projects.map(project -> project.getUri());
  }


  
}
