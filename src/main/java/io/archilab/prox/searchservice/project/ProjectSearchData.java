package io.archilab.prox.searchservice.project;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;
import lombok.Data;


@Data
public class ProjectSearchData {

  private String id;

  public ProjectSearchData(UUID id) {
    this.id = id.toString();


  }
}
