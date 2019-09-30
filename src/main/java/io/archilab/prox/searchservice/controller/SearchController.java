package io.archilab.prox.searchservice.controller;

import java.util.List;

import org.springframework.data.domain.Page;
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



@RestController
@RequestMapping("search")
public class SearchController implements ResourceProcessor<RepositoryLinksResource> {

  @GetMapping(value = "/")
  public RepositoryLinksResource allLinks() {
    RepositoryLinksResource resource = new RepositoryLinksResource();
    
    Link searchBasic = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).searchBasic(null))
        .withRel("searchBasic");
    resource.add(searchBasic);

    Link searchAdvanced = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).searchAdvanced(null))
        .withRel("searchAdvanced");
    resource.add(searchAdvanced);
    
//    Link pages = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).pages(null,null))
//        .withRel("pages");
//    resource.add(pages);
    
    
    Link self = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SearchController.class).allLinks()).withSelfRel();
    resource.add(self);

    return resource;
  }
  
  @GetMapping(value = "/searchBasic")
  public ResponseEntity<List<String>> searchBasic(@RequestParam(value = "page", required = false) Integer offset) {
    ResponseEntity<List<String>> response = new ResponseEntity<List<String>>(HttpStatus.ACCEPTED);
    // response.getBody().add("hallo121");

    return response;
  }
  
  
//  @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
//  public ResponseEntity < PagedResources < String >> pages(Pageable pageable, PagedResourcesAssembler assembler) {
//   Page < String > products = Page.empty( pageable);
//   PagedResources < String > pr = assembler.toResource(products, ControllerLinkBuilder.linkTo(SearchController.class).slash("/products").withSelfRel());
//   HttpHeaders responseHeaders = new HttpHeaders();
//   responseHeaders.add("Link", createLinkHeader(pr));
//   return new ResponseEntity < > (assembler.toResource(products, ControllerLinkBuilder.linkTo(SearchController.class).slash("/products").withSelfRel()), responseHeaders, HttpStatus.OK);
//  }
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
