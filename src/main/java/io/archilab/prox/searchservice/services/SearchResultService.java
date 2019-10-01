package io.archilab.prox.searchservice.services;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
  
  Logger log = LoggerFactory.getLogger(ProjectRepository.class);


  public List<ProjectSearchData> findPaginated(Pageable pageable, String searchText) throws Exception {
    
    
    if(projectRepository.count()==0)
    {
      for(int i=0;i<800;i++)
      {
        Project testpp = new Project(UUID.randomUUID(),new URI("3r4t5") , new ProjectName(String.valueOf(i)), new ProjectShortDescription("sfeeeeeeeeee"),
            new ProjectDescription("eeeff"), ProjectStatus.VERFÜGBAR, new ProjectRequirement("fsertgr5z5z5zrr5zr5zr5zr5"),
            new SupervisorName("fffffffffffff") );
        projectRepository.save(testpp);
      }
      log.info("new data");

    }
  
    return projectRepository.findAllSearchedProjects(pageable,searchText);

  }

}
