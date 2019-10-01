package io.archilab.prox.searchservice.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.archilab.prox.searchservice.project.Project;
import io.archilab.prox.searchservice.project.ProjectSearchData;
import io.archilab.prox.searchservice.services.SearchResultService;



@RestController
@RequestMapping("search")
public class SearchController implements ResourceProcessor<RepositoryLinksResource> {

  @Autowired
  SearchResultService searchResultService;
  
  @GetMapping(value = "/")
  public RepositoryLinksResource allLinks() {
    RepositoryLinksResource resource = new RepositoryLinksResource();
    
    Link searchBasic;
    try {
      searchBasic = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).searchBasic(Pageable.unpaged(),null,null))
          .withRel("searchBasic");
      resource.add(searchBasic);
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    

    Link searchAdvanced = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).searchAdvanced(null))
        .withRel("searchAdvanced");
    resource.add(searchAdvanced);
    
//    Link pages;
//    try {
//      pages = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).pages(null,null))
//          .withRel("pages");
//      resource.add(pages);
//    } catch (Exception e) {
//      
//      e.printStackTrace();
//    }
//    
    
    
    Link self = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).allLinks()).withSelfRel();
    resource.add(self);

    return resource;
  }
  
  private void fillObjectNode(ObjectNode onode, ProjectSearchData projectSearchData)
  {
    onode.put("uri", projectSearchData.getUri() );
  }
  
  @GetMapping(value = "/searchBasic" , produces=MediaType.APPLICATION_JSON_VALUE)
  public String searchBasic(@NotNull final Pageable pageable,  @RequestParam("searchText") String searchText, HttpServletRequest httpServletRequest) throws Exception {

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
    
    
    List<ProjectSearchData> resultPage = searchResultService.findPaginated(pageable,searchText);
    
    for (ProjectSearchData project : resultPage) {
      fillObjectNode(onode_projects_list.addObject(),project);
    }
    
    
    
    long totalElements = searchResultService.getTotalElements();  
    long lastPage = ((totalElements - 1l) / (long)pageable.getPageSize()) + 1l;
    onode_page.put("size",pageable.getPageSize());
    onode_page.put("totalElements",totalElements);
    onode_page.put("totalPages", lastPage);
    onode_page.put("number",pageable.getPageNumber());



    
    StringBuilder requestURL = new StringBuilder(httpServletRequest.getRequestURL().toString());
    String queryString = httpServletRequest.getQueryString();

    if (queryString != null) 
    {
      requestURL.append('?').append(queryString).toString();
    } 
    UriComponents ucb=null;
    URI uri = null;
    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", "0")
            .build();
    uri = ucb.toUri();     
    onode_links_first.put("href",uri.toString());
    onode_links.set("first",onode_links_first);

    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .replaceQueryParam("page", String.valueOf(lastPage))
            .build();
    uri = ucb.toUri();  
    onode_links_last.put("href",uri.toString());
    onode_links.set("last",onode_links_last);
    
    
    if(0!=pageable.getPageNumber())
    {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page", String.valueOf((pageable.getPageNumber()+1)))
              .build();
      uri = ucb.toUri();  
      onode_links_prev.put("href",uri.toString());
      onode_links.set("prev",onode_links_prev);
    }

    
    if(lastPage!=pageable.getPageNumber())
    {
      ucb =
          ServletUriComponentsBuilder.fromRequest(httpServletRequest)
              .replaceQueryParam("page",  String.valueOf((pageable.getPageNumber()+1)))
              .build();
      uri = ucb.toUri();  
      onode_links_next.put("href",uri.toString());
      onode_links.set("next",onode_links_next);
    }

    
    ucb =
        ServletUriComponentsBuilder.fromRequest(httpServletRequest)
            .build();
    uri = ucb.toUri();   
    onode_links_self.put("href",uri.toString());
    onode_links.set("self",onode_links_self);
   

    onode_projects.set("projects", onode_projects_list);
 
    onode_root.set("_embedded", onode_projects);
    onode_root.set("_links", onode_links);
    onode_root.set("page", onode_page);

    return onode_root.toString();

  }
  
  
  
//  @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity < PagedResources < String >> pages2(Pageable pageable, PagedResourcesAssembler assembler) {
//   Page < String > products = Page.empty( pageable);
//   PagedResources < String > pr = assembler.toResource(products, ControllerLinkBuilder.linkTo(SearchController.class).slash("/products").withSelfRel());
//   HttpHeaders responseHeaders = new HttpHeaders();
//   responseHeaders.add("Link", createLinkHeader(pr));
//   return new ResponseEntity < > (assembler.toResource(products, ControllerLinkBuilder.linkTo(SearchController.class).slash("/products").withSelfRel()), responseHeaders, HttpStatus.OK);
//  }
//  
  // ,params = { "pageable", "searchText" }  @RequestParam("page") int page, @RequestParam("size") int size, 
  
//  @GetMapping(value = "/products" , produces = MediaType.APPLICATION_JSON_VALUE)
//  public Page<Project> pages( @NotNull final Pageable pageable,
//      @RequestParam("searchText") String searchText) throws Exception {
//
//    List<Project> resultPage = searchResultService.findPaginated(pageable,searchText);
////        if (pageable.getPageNumber() > resultPage.getTotalPages()) {
////            throw new Exception("Page does not exist");
////        }
//    
//    int start = (int) pageable.getOffset();
//
//    int end = (int) ((start + pageable.getPageSize()) > resultPage.size() ? resultPage.size()
//      : (start + pageable.getPageSize()));
//
// 
//    Page<Project> page 
//      = new PageImpl<Project>(resultPage.subList(start, end), pageable, resultPage.size());
//
//        return page;
//    }
//  
//  private String createLinkHeader(PagedResources < String > pr) {
//    final StringBuilder linkHeader = new StringBuilder();
////    linkHeader.append(buildLinkHeader(pr.getLinks("first").get(0).getHref(), "first"));
////    linkHeader.append(", ");
////    linkHeader.append(buildLinkHeader(pr.getLinks("next").get(0).getHref(), "next"));
//    return linkHeader.toString();
//   }
//
//   public static String buildLinkHeader(final String uri, final String rel) {
//    return "<" + uri + ">; rel=\"" + rel + "\"";
//   }

  
  @GetMapping(value = "/searchAdvanced")
  public ResponseEntity<List<String>> searchAdvanced(@RequestParam(value = "page", required = false) Integer offset) {
    ResponseEntity<List<String>> response = new ResponseEntity<List<String>>(HttpStatus.ACCEPTED);
    // response.getBody().add("hallo121");

    return response;
  }

  @Override
  public RepositoryLinksResource process(RepositoryLinksResource resource) {

    Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).allLinks())
        .withRel("search");
    resource.add(link);
    return resource;
  }

}
