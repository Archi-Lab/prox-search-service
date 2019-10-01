package io.archilab.prox.searchservice.project;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import lombok.Data;


@Data
public class ProjectSearchData {

  private String uri;

  public ProjectSearchData(URI uri) {
      this.uri = uri.toString();

  }
}
