package io.archilab.prox.searchservice.project;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface ProjectRepository extends PagingAndSortingRepository<Project, UUID> {

}
