package io.archilab.prox.searchservice.project;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = true)
public interface ProjectRepository extends ProjectRepositoryCustom, PagingAndSortingRepository<Project, UUID> {

}
