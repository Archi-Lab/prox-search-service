package io.archilab.prox.searchservice.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {
  
  public List<Project> findAllSearchedProjects(Pageable pageable, String searchText) throws Exception;

  public Page<String> findAllSearchedProjects2(Pageable pageable, String searchText) throws Exception;

}
