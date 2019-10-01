package io.archilab.prox.searchservice.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepositoryCustom {
  
  public List<ProjectSearchData> findAllSearchedProjects(Pageable pageable, String searchText) throws Exception;

//  public Page<String> findAllSearchedProjects2(Pageable pageable, String searchText) throws Exception;
  
//  public Page<ProjectUriProjection> findAllSearchedProjects3(Pageable pageable, String searchText) throws Exception;

}
