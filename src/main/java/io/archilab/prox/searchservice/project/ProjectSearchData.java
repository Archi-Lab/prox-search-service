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

  public ProjectSearchData(URI uri) {

      Pattern pairRegex = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");
      Matcher matcher = pairRegex.matcher(uri.toString());
      while (matcher.find()) {
          this.id = matcher.group(0);
      }
  }
}
