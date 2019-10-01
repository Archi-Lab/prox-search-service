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
      for(int i=0;i<800;i++)
      {
        Project testpp = new Project(new URI("http://t4tgegtete4t/bhrjkge54ughe/bnnnnn444444"+i) , new ProjectName(String.valueOf(i)), new ProjectShortDescription("sfeeeeeeeeee"),
            new ProjectDescription("eeeff"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("fsertgr5z5z5zrr5zr5zr5zr5"),
            new SupervisorName("fffffffffffff") );
        projectRepository.save(testpp);
      }
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
