package io.archilab.prox.searchservice.project;

import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ProjectImportService {

    private final Logger logger = LoggerFactory.getLogger(ProjectClient.class);

    private final ProjectClient projectClient;

    public ProjectImportService(ProjectClient projectClient) {
        this.projectClient = projectClient;
    }

    public void importProjects() {
        this.logger.info("Start importing projects");

        var projects = this.projectClient.getProjects();
    }
}



