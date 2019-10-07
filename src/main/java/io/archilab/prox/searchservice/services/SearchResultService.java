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
import org.springframework.jdbc.core.JdbcTemplate;
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
  private CachedSearchResultService cachedSearchResultService;

  
  Logger log = LoggerFactory.getLogger(ProjectRepository.class);


  public List<ProjectSearchData> findPaginated(Pageable pageable, String searchText) throws Exception {
    
    
    if(projectRepository.count()==0)
    {
//      for(int i=0;i<800;i++)
//      {
//        Project testpp = new Project(UUID.randomUUID() , new ProjectName(String.valueOf(i)), new ProjectShortDescription("sfeeeeeeeeee"),
//            new ProjectDescription("eeeff"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("fsertgr5z5z5zrr5zr5zr5zr5"),
//            new SupervisorName("fffffffffffff") );
//        testpp.getTags().add(e)
//        projectRepository.save(testpp);
//      }
      Project testpp = null;
      testpp = new Project(UUID.randomUUID() , new ProjectName("Hallo Hallo hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(UUID.randomUUID() , new ProjectName("wer wer wer wer Hallo wir hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(UUID.randomUUID() , new ProjectName("Hallo wir wir wir wir wir wir hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      projectRepository.save(testpp);
      
      testpp = new Project(UUID.randomUUID() , new ProjectName("Hallo Hallo wer wie wo wer wie wo hasu haus warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tttt"));
      testpp.getTags().add(new TagName("tt22"));
      testpp.getTags().add(new TagName("tt33"));
      testpp.getTags().add(new TagName("tt44"));
      projectRepository.save(testpp);
      
      testpp = new Project(UUID.randomUUID() , new ProjectName(" Hallo hasu  warum wort"), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag2"));
      testpp.getTags().add(new TagName("tag3"));
      testpp.getTags().add(new TagName("tag4"));
      projectRepository.save(testpp);
      
      testpp = new Project(UUID.randomUUID() , new ProjectName("Hallo   haus warum "), new ProjectShortDescription(" wiese baum bohne see wasser wasser wasser sonne"),
          new ProjectDescription("ewqewq"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("qweqwe"),
          new SupervisorName("fffffffffffff") );
      testpp.getTags().add(new TagName("tag1"));
      testpp.getTags().add(new TagName("tag3"));
      projectRepository.save(testpp);
      
      log.info("new data");

    }
    
    
    
    String filter = searchText;
    
    if(filter == null || filter.length() < 2)
      throw new Exception("bad search string");

    filter = filter.toLowerCase();
    
 // Supervisor
    var titleFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    var supervisorFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    var requirementsFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    var shortDescriptionFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    var descriptionFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    var tagsFilter = cachedSearchResultService.getFilter(filter, "Betreuer");
    

    
    
    int tagMultiplier = 5;
    int descriptionMultiplier = 8;
    int shortDescriptionMultiplier=2;
    int requirementsMultiplier=4;
    int supervisorNameMultiplier=3;
    int titleMultiplier=2;
    
    String where_part=""
           
+    "(( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND (pt.tag_name in ('tag1','tag2','tag3')) ) > 0)"
+   " and ( lower(p.supervisor_name) like '%s1%'"
+   " or lower(p.supervisor_name) like '%s2%'"
+  "      )"
+  "  and p.status = 0"
+"    and ("
+"        lower(p.name) like '%t1%'"
+"    or  lower(p.name) like '%t2%' "
+"    )"
+"    and ("
+"        lower(p.short_description) like '%sd1%'"
+"    or  lower(p.short_description) like '%sd2%' "
+"    )"
+"    and ("
+"        lower(p.description) like '%d1%'"
+"    or  lower(p.description) like '%d2%' "
+"    )"
+"    and ("
+"        lower(p.requirement) like '%r1%'"
+"    or  lower(p.requirement) like '%r2%' "
+"    ) ";
    
    
    
    
    
    String counting_part="";
    
    // name - title
    if(titleFilter.hasValues)
    {
      String titleParts="";
      for(int i=0;i<200;i++)
      {
        String term="";
        String plus="";
        if(i!=0)
        {
          plus="+";
        }
        titleParts+="((length(p.name) - length(replace(p.name, '"+term+"', '')) )::int  / length('"+term+"'))";
        
      }
      counting_part+="+( "+titleParts+" *"+titleMultiplier+")";
          
    }
    
    // requirement
    if(requirementsFilter.hasValues)
    {
      String requirementParts="";
      for(int i=0;i<200;i++)
      {
        String term="";
        String plus="";
        if(i!=0)
        {
          plus="+";
        }
        requirementParts+="((length(p.requirement) - length(replace(p.requirement, '"+term+"', '')) )::int  / length('"+term+"'))";
        
      }
      counting_part+="+( "+requirementParts+" *"+requirementsMultiplier+")";
          
    }
    
    // supervisor_name
    if(supervisorFilter.hasValues)
    {
      String supervisorNameParts="";
      for(int i=0;i<200;i++)
      {
        String term="";
        String plus="";
        if(i!=0)
        {
          plus="+";
        }
        supervisorNameParts+="((length(p.supervisor_name) - length(replace(p.supervisor_name, '"+term+"', '')) )::int  / length('"+term+"'))";
        
      }
      counting_part+="+( "+supervisorNameParts+" *"+supervisorNameMultiplier+")";
          
    }
    
    // short_description
    if(shortDescriptionFilter.hasValues)
    {
      String shortDescriptionParts="";
      for(int i=0;i<200;i++)
      {
        String term="";
        String plus="";
        if(i!=0)
        {
          plus="+";
        }
        shortDescriptionParts+="((length(p.short_description) - length(replace(p.short_description, '"+term+"', '')) )::int  / length('"+term+"'))";
        
      }
      counting_part+="+( "+shortDescriptionParts+" *"+shortDescriptionMultiplier+")";
          
    }
    
    // description
    if(descriptionFilter.hasValues)
    {
      String descriptionParts="";
      for(int i=0;i<200;i++)
      {
        String descTerm="";
        String plus="";
        if(i!=0)
        {
          plus="+";
        }
        descriptionParts+="((length(p.description) - length(replace(p.description, '"+descTerm+"', '')) )::int  / length('"+descTerm+"'))";
        
      }
      counting_part+="+( "+descriptionParts+" *"+descriptionMultiplier+")";
          
    }
   
    
    // tags
    if(tagsFilter.hasValues)
    {
      String tags = "";
      for(int i=0;i<200;i++)
      {
        String tagX="tag1";
        String comma="";
        if(i!=0)
        {
          comma=",";
        }
        
        tags+=comma+"'"+tagX+"'";
        
      }
      counting_part+= "+( ( SELECT COUNT(*) FROM project_tags as pt WHERE pt.project_id = p.id AND (pt.tag_name in ("+tags+")) ) *"+tagMultiplier+")";
         
    }
     
    String paging_part=" LIMIT "  + pageable.getPageSize()
        +"  OFFSET "+ pageable.getOffset();
    
    String from_part="SELECT  p.id as id, ("+counting_part+") as priority from project p where "+where_part;
    String full_query="Select result_query.id from ("+from_part+" ORDER BY priority desc ) as result_query "+paging_part;
    
 
    List<UUID> result = jdbcTemplate.queryForObject(full_query, List.class);
    
    
    
    List<ProjectSearchData> retList = new ArrayList<>();
    
    
    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
    
    countQuery.select(criteriaBuilder
        .count(countQuery.from(Project.class)));
      Long count = entityManager.createQuery(countQuery)
        .getSingleResult();
      
      
      CriteriaQuery<Project> sub_query = criteriaBuilder.createQuery(Project.class);
      Root<Project> root_pro = sub_query.from(Project.class);
      sub_query.select(root_pro.get("id"));
  

      

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
          retList.add(new ProjectSearchData(pt.getId()));
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
  
  public Page<UUID> findAll(Pageable pageable, String searchText) {

    Page<Project> projects =  projectRepository.findAll(pageable);

    return projects.map(project -> project.getId());
  }


  
}
