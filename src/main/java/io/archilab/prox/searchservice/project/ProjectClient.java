package io.archilab.prox.searchservice.project;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.stereotype.Component;

@Component
public class ProjectClient {

  private final org.slf4j.Logger logger = LoggerFactory.getLogger(ProjectClient.class);

  private final EurekaClient eurekaClient;

  public ProjectClient(EurekaClient eurekaClient) {
    this.eurekaClient = eurekaClient;
  }

  private String projectServiceUrl() {
    InstanceInfo instance = this.eurekaClient.getNextServerFromEureka("project-service", false);
    String url = instance.getHomePageUrl() + "projects/search";
    return url;
  }

  private String tagServiceUrl() {
    InstanceInfo instance = this.eurekaClient.getNextServerFromEureka("tag-service", false);
    String url = instance.getHomePageUrl() + "tagCollections";
    return url;
  }

  private Traverson getTraversonInstance(String url) {
    URI uri;
    try {
      uri = new URI(url);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Eureka provided an invalid service URL", e);
    }

    return new Traverson(uri, MediaTypes.HAL_JSON);
  }

  public List<Project> getProjects(Date startTime) {
    Traverson traverson = this.getTraversonInstance(this.projectServiceUrl());
    String tagServiceURL = this.tagServiceUrl();

    if (traverson == null) {
      return null;
    }

    List<Project> projects = new ArrayList<>();

    try {

      Map<String, Object> params = new HashMap<>();
      params.put("modified", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));

      final PagedModel<EntityModel<Project>> pagedProjectResources =
          traverson
              .follow("findAllByModifiedAfter")
              .withTemplateParameters(params)
              .toObject(new TypeReferences.PagedModelType<>() {});

      for (EntityModel<Project> projectResource : pagedProjectResources.getContent()) {

        Project project = projectResource.getContent();

        var uri = projectResource.getLink(IanaLinkRelations.SELF).get().getHref();

        Pattern pairRegex =
            Pattern.compile(
                "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
        Matcher matcher = pairRegex.matcher(uri.toString());
        while (matcher.find()) {
          String id_uuid = matcher.group(0);
          project.setId(UUID.fromString(id_uuid));
        }

        // Tags
        if (traverson != null) {
          Traverson tagTraverson = this.getTraversonInstance(tagServiceURL + "/" + project.getId());
          final CollectionModel<EntityModel<TagName>> tagResources =
              tagTraverson.follow("tags").toObject(new TypeReferences.CollectionModelType<>() {});

          for (EntityModel<TagName> moduleResource : tagResources.getContent()) {
            TagName tag = moduleResource.getContent();

            project.getTags().add(tag);
          }
        }

        projects.add(project);
      }

      return projects;

    } catch (Exception e) {
      e.printStackTrace();
      this.logger.error("Error retrieving projects");

      return null;
    }
  }
}
