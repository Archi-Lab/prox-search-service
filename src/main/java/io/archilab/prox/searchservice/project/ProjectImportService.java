package io.archilab.prox.searchservice.project;

import io.archilab.prox.searchservice.services.CachedSearchResultService;
import io.archilab.prox.searchservice.services.SearchResultService;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Transactional
@Service
public class ProjectImportService {

  private final Logger logger = LoggerFactory.getLogger(ProjectClient.class);
  private final ProjectRepository projectRepository;
  private final ProjectClient projectClient;
  private final CachedSearchResultService cachedSearchResultService;

  private Date _lastUpdate = new Date(1);

  public ProjectImportService(ProjectClient projectClient, ProjectRepository projectRepository,
      CachedSearchResultService cachedSearchResultService) {
    this.projectClient = projectClient;
    this.projectRepository = projectRepository;
    this.cachedSearchResultService = cachedSearchResultService;
  }

  public void importProjects() {
    this.logger.info("Start importing projects");

    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

    var doy = calendar.get(Calendar.DAY_OF_YEAR);
    calendar.setTime(this._lastUpdate);
    var lastDoy = calendar.get(Calendar.DAY_OF_YEAR);

    // Import all once a day, to remove deleted projects
    var loadAll = doy != lastDoy;

    var time = loadAll ? new Date(0) : this._lastUpdate;
    var startTime = new Date();

    var projects = this.projectClient.getProjects(time);

    if (projects == null)
      return;

    if (loadAll)
      this.projectRepository.deleteAll();
    else
      this.projectRepository.deleteAll(projects);

    this.projectRepository.saveAll(projects);

    this.logger.info(projects.size() + " projects imported");

    this._lastUpdate = startTime;

    if (projects.size() > 0)
      this.cachedSearchResultService.load();
  }
}


