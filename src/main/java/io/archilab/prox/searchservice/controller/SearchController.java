package io.archilab.prox.searchservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import java.net.URI;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.services.CachedSearchResultService;
import io.archilab.prox.searchservice.services.SearchResultService;


@RestController
@RequestMapping("search")
public class SearchController implements ResourceProcessor<RepositoryLinksResource> {

  Logger log = LoggerFactory.getLogger(SearchController.class);


  @Autowired
  SearchResultService searchResultService;

  @Autowired
  CachedSearchResultService cachedSearchResultService;

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
    final String linkToController = ControllerLinkBuilder.linkTo(SearchController.class).toString();



    try {

      Link sasa = new Link(new UriTemplate(linkToController + "/projects",
          // register it as variable
          getBaseTemplateVariables()), "projects");

      resource.add(sasa);

    } catch (Exception e1) {
      e1.printStackTrace();
    }

    try {

      Link sasa = new Link(new UriTemplate(linkToController + "/sqlProjects",
          // register it as variable
          getBaseTemplateVariables()), "sqlProjects");

      resource.add(sasa);

    } catch (Exception e1) {
      e1.printStackTrace();
    }

    Link self = ControllerLinkBuilder
        .linkTo(ControllerLinkBuilder.methodOn(SearchController.class).allLinks()).withSelfRel();
    resource.add(self);

    return resource;
  }

  private void fillObjectNode(ObjectNode onode, ProjectSearchData projectSearchData) {
    onode.put("id", projectSearchData.getId());
  }

  @GetMapping(value = "/sqlprojects", produces = MediaType.APPLICATION_JSON_VALUE)
  public String searchProjects(@NotNull final Pageable pageable,
      @RequestParam("searchText") String searchText, HttpServletRequest httpServletRequest)
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
        searchResultService.findPaginated(pageable, searchText);
    List<ProjectSearchData> resultPage = resultData.getFirst();


    for (ProjectSearchData project : resultPage) {
      fillObjectNode(onode_projects_list.addObject(), project);
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
    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest).replaceQueryParam("page", "0")
        .build();
    uri = ucb.toUri();
    onode_links_first.put("href", uri.toString());
    onode_links.set("first", onode_links_first);

    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
        .replaceQueryParam("page", String.valueOf(lastPage)).build();
    uri = ucb.toUri();
    onode_links_last.put("href", uri.toString());
    onode_links.set("last", onode_links_last);


    if (0 != pageable.getPageNumber()) {
      ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
          .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1))).build();
      uri = ucb.toUri();
      onode_links_prev.put("href", uri.toString());
      onode_links.set("prev", onode_links_prev);
    }


    if (lastPage != pageable.getPageNumber()) {
      ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
          .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1))).build();
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
  public String searchCachedProjects(@NotNull final Pageable pageable,
      @RequestParam("searchText") String searchText, HttpServletRequest httpServletRequest)
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


    List<ProjectSearchData> resultPage =
        cachedSearchResultService.findPaginated(pageable, searchText);

    for (ProjectSearchData project : resultPage) {
      fillObjectNode(onode_projects_list.addObject(), project);
    }



    long totalElements = cachedSearchResultService.getTotalElements();
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
    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest).replaceQueryParam("page", "0")
        .build();
    uri = ucb.toUri();
    onode_links_first.put("href", uri.toString());
    onode_links.set("first", onode_links_first);

    ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
        .replaceQueryParam("page", String.valueOf(lastPage)).build();
    uri = ucb.toUri();
    onode_links_last.put("href", uri.toString());
    onode_links.set("last", onode_links_last);


    if (0 != pageable.getPageNumber()) {
      ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
          .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1))).build();
      uri = ucb.toUri();
      onode_links_prev.put("href", uri.toString());
      onode_links.set("prev", onode_links_prev);
    }


    if (lastPage != pageable.getPageNumber()) {
      ucb = ServletUriComponentsBuilder.fromRequest(httpServletRequest)
          .replaceQueryParam("page", String.valueOf((pageable.getPageNumber() + 1))).build();
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

    Link link = ControllerLinkBuilder
        .linkTo(ControllerLinkBuilder.methodOn(SearchController.class).allLinks())
        .withRel("search");
    resource.add(link);
    return resource;
  }

}
