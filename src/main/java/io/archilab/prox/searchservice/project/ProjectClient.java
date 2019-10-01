package io.archilab.prox.searchservice.project;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.*;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.mvc.TypeReferences;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class ProjectClient {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(ProjectClient.class);

    private final EurekaClient eurekaClient;

    public ProjectClient(EurekaClient eurekaClient) {
        this.eurekaClient = eurekaClient;
    }

    private String serviceUrl() {
        InstanceInfo instance = this.eurekaClient.getNextServerFromEureka("project-service", false);
        String url = instance.getHomePageUrl() + "projects/search";
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
        Traverson traverson = this.getTraversonInstance(this.serviceUrl());


        if (traverson == null) {
            return null;
        }


        List<Project> projects = new ArrayList<>();

        try {

            Map<String, Object> params = new HashMap<>();
            params.put("modified", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime));

            final PagedResources<Resource<Project>> pagedProjectResources =
                    traverson.follow("findAllByModifiedAfter").withTemplateParameters(params)
                            .toObject(new TypeReferences.PagedResourcesType<Resource<Project>>() {
                            });

            for (Resource<Project> projectResource : pagedProjectResources.getContent()) {

                Project project = projectResource.getContent();

                var uri = projectResource.getId().getHref();

                project.setUri(new URI(uri));

                // Tags
                Link tagCollection = projectResource.getLink("tagCollection");

                Traverson tagCollectionTraverson = this.getTraversonInstance(tagCollection.getHref());

                if (traverson != null) {

                    final Resources<Resource<TagName>> tagResources = tagCollectionTraverson.follow("self")
                            .toObject(new TypeReferences.ResourcesType<Resource<TagName>>() {
                            });

                    for (Resource<TagName> moduleResource : tagResources.getContent()) {

                        TagName tag = moduleResource.getContent();

                        project.getTags().add(tag);
                    }
                }

                projects.add(project);
            }

            return projects;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error retrieving projects");

            return null;
        }
    }
}
