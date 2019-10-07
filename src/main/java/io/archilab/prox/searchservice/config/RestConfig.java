package io.archilab.prox.searchservice.config;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class RestConfig implements RepositoryRestConfigurer {

  @Autowired
  private EntityManager entityManager;
  
 
  @Override
  public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
    config.exposeIdsFor(this.entityManager.getMetamodel().getEntities().stream()
        .map(Type::getJavaType).toArray(Class[]::new));
  }
  
  
}
