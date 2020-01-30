package io.archilab.prox.searchservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.archilab.prox.searchservice.project.ProjectRepository;
import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.services.CachedSearchResultService;
import io.archilab.prox.searchservice.services.SearchResultService;
import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

@RestController
@RequestMapping("search")
public class SearchController implements RepresentationModelProcessor<RepositoryLinksResource> {

  Logger log = LoggerFactory.getLogger(SearchController.class);

  @Autowired SearchResultService searchResultService;

  @Autowired CachedSearchResultService cachedSearchResultService;

  @Autowired ProjectRepository projectRepository;

  private static TemplateVariables getBaseTemplateVariables() {
    return new TemplateVariables(
        new TemplateVariable("page", TemplateVariable.VariableType.REQUEST_PARAM),
        new TemplateVariable("sort", TemplateVariable.VariableType.REQUEST_PARAM),
        new TemplateVariable("size", TemplateVariable.VariableType.REQUEST_PARAM),
        new TemplateVariable("searchText", TemplateVariable.VariableType.REQUEST_PARAM));
  }

  @GetMapping
  public RepositoryLinksResource allLinks() {
    RepositoryLinksResource resource = new RepositoryLinksResource();

    // UriTemplate esd = new UriTemplate();
    final String linkToController = WebMvcLinkBuilder.linkTo(SearchController.class).toString();

    try {

      Link sasa =
          new Link(
              new UriTemplate(
                  linkToController + "/projects",
                  // register it as variable
                  SearchController.getBaseTemplateVariables()),
              "projects");

      resource.add(sasa);

    } catch (Exception e1) {
      e1.printStackTrace();
    }

    try {

      Link sasa =
          new Link(
              new UriTemplate(
                  linkToController + "/sqlprojects",
                  // register it as variable
                  SearchController.getBaseTemplateVariables()),
              "sqlprojects");

      resource.add(sasa);

    } catch (Exception e1) {
      e1.printStackTrace();
    }

    Link self =
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SearchController.class).allLinks())
            .withSelfRel();
    resource.add(self);

    return resource;
  }

  private void fillObjectNode(ObjectNode onode, ProjectSearchData projectSearchData) {
    onode.put("id", projectSearchData.getId());
  }

  @GetMapping(value = "/sqlprojects", produces = MediaType.APPLICATION_JSON_VALUE)
  public String searchProjects(
      @NotNull final Pageable pageable,
      @RequestParam("searchText") String searchText,
      HttpServletRequest httpServletRequest)
      throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();

    ObjectNode onode_root = objectMapper.createObjectNode();
    ObjectNode onode_projects = objectMapper.createObjectNode();
    ArrayNode onode_projects_list = objectMapper.createArrayNode();
    ObjectNode onode_links = objectMapper.createObjectNode();

    ObjectNode onode_links_first = objectMapper.createObjectNode();
    ObjectNode onode_links_last = objectMapper.createObjectNode();
    ObjectNode onode_links_prev = objectMapper.createObjectNode();
    ObjectNode onode_links_next = objectMapper.createObjectNode();
    ObjectNode onode_links_self = objectMapper.createObjectNode();

    ObjectNode onode_page = objectMapper.createObjectNode();

    Pair<List<ProjectSearchData>, Long> resultData =
        this.searchResultService.findPaginated(pageable, searchText);
    List<ProjectSearchData> resultPage = resultData.getFirst();

    for (ProjectSearchData project : resultPage) {
      this.fillObjectNode(onode_projects_list.addObject(), project);
    }

    long totalElements = resultData.getSecond(); // searchResultService.getTotalElements();
    long lastPage = ((totalElements - 1l) / (long) pageable.getPageSize()) + 1l;
    onode_page.put("size", pageable.getPageSize());
    onode_page.put("totalElements", totalElements);
    onode_page.put("totalPages", lastPage);
    onode_page.put("number", pageable.getPageNumber());

    StringBuilder requestURL = new StringBuilder(httpServletRequest.getRequestURL().toString());
    String queryString = httpServletRequest.getQueryString();

    if (queryString != null) {
      requestURL.append('?').append(queryString).toString();
    }
    UriComponents ucb = null;
    URI uri = null;
    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", "0")
            .build();
    uri = ucb.toUri();
    onode_links_first.put("href", uri.toString());
    onode_links.set("first", onode_links_first);

    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", String.valueOf(lastPage))
            .build();
    uri = ucb.toUri();
    onode_links_last.put("href", uri.toString());
    onode_links.set("last", onode_links_last);

    if (0 != pageable.getPageNumber()) {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() - 1)))
              .build();
      uri = ucb.toUri();
      onode_links_prev.put("href", uri.toString());
      onode_links.set("prev", onode_links_prev);
    }

    if (lastPage != pageable.getPageNumber()) {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1)))
              .build();
      uri = ucb.toUri();
      onode_links_next.put("href", uri.toString());
      onode_links.set("next", onode_links_next);
    }

    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest).build();
    uri = ucb.toUri();
    onode_links_self.put("href", uri.toString());
    onode_links.set("self", onode_links_self);

    onode_projects.set("projects", onode_projects_list);

    onode_root.set("_embedded", onode_projects);
    onode_root.set("_links", onode_links);
    onode_root.set("page", onode_page);

    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(onode_root);
  }

  @GetMapping(value = "/projects", produces = MediaType.APPLICATION_JSON_VALUE)
  public String searchCachedProjects(
      @NotNull final Pageable pageable,
      @RequestParam("searchText") String searchText,
      HttpServletRequest httpServletRequest)
      throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();

    ObjectNode onode_root = objectMapper.createObjectNode();
    ObjectNode onode_projects = objectMapper.createObjectNode();
    ArrayNode onode_projects_list = objectMapper.createArrayNode();
    ObjectNode onode_links = objectMapper.createObjectNode();

    ObjectNode onode_links_first = objectMapper.createObjectNode();
    ObjectNode onode_links_last = objectMapper.createObjectNode();
    ObjectNode onode_links_prev = objectMapper.createObjectNode();
    ObjectNode onode_links_next = objectMapper.createObjectNode();
    ObjectNode onode_links_self = objectMapper.createObjectNode();

    ObjectNode onode_page = objectMapper.createObjectNode();

    int pageNumber = pageable.getPageNumber();
    int pageSize = pageable.getPageSize();
    var start = pageNumber * pageSize;

    var projects = this.cachedSearchResultService.getProjects(searchText);

    for (int i = start; i < start + pageSize && i < projects.size(); i++) {
      var project = projects.get(i);
      var projectData = new ProjectSearchData(project.getId());

      this.fillObjectNode(onode_projects_list.addObject(), projectData);
    }

    long totalElements = projects.size();
    long size = (long) pageable.getPageSize();
    long totalPages = totalElements / size;

    if (totalElements % size != 0) {
      totalPages++;
    }

    long lastPage = Math.max(0, totalPages - 1);

    this.log.info("total: " + totalElements);
    this.log.info("size: " + size);
    this.log.info("totalPages: " + totalPages);
    this.log.info("lastPage: " + lastPage);

    onode_page.put("size", size);
    onode_page.put("totalElements", totalElements);
    onode_page.put("totalPages", totalPages);
    onode_page.put("number", pageable.getPageNumber());

    StringBuilder requestURL = new StringBuilder(httpServletRequest.getRequestURL().toString());
    String queryString = httpServletRequest.getQueryString();

    if (queryString != null) {
      requestURL.append('?').append(queryString).toString();
    }
    UriComponents ucb = null;
    URI uri = null;
    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", "0")
            .build();
    uri = ucb.toUri();
    onode_links_first.put("href", uri.toString());
    onode_links.set("first", onode_links_first);

    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", String.valueOf(lastPage))
            .build();
    uri = ucb.toUri();
    onode_links_last.put("href", uri.toString());
    onode_links.set("last", onode_links_last);

    if (0 != pageable.getPageNumber()) {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() - 1)))
              .build();
      uri = ucb.toUri();

      onode_links_prev.put("href", uri.toString());

      onode_links.set("prev", onode_links_prev);
    }

    if (lastPage != pageable.getPageNumber()) {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1)))
              .build();
      uri = ucb.toUri();
      onode_links_next.put("href", uri.toString());
      onode_links.set("next", onode_links_next);
    }

    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest).build();
    uri = ucb.toUri();
    onode_links_self.put("href", uri.toString());
    onode_links.set("self", onode_links_self);

    onode_projects.set("projects", onode_projects_list);

    onode_root.set("_embedded", onode_projects);
    onode_root.set("_links", onode_links);
    onode_root.set("page", onode_page);

    return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(onode_root);
  }

  @Override
  public RepositoryLinksResource process(RepositoryLinksResource resource) {

    Link link =
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(SearchController.class).allLinks())
            .withRel("search");
    resource.add(link);
    return resource;
  }
}
