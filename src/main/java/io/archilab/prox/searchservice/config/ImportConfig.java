package io.archilab.prox.searchservice.config;

import io.archilab.prox.searchservice.project.ProjectImportService;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class ImportConfig implements SchedulingConfigurer {

  @Autowired private Environment env;

  @Autowired private ProjectImportService projectImportService;

  private boolean initialStart = true;

  @Bean
  public Executor taskExecutor() {
    return Executors.newScheduledThreadPool(100);
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.setScheduler(taskExecutor());

    taskRegistrar.addTriggerTask(
        () -> projectImportService.importProjects(),
        triggerContext -> {
          Calendar nextExecutionTime = new GregorianCalendar();

          if (initialStart) {
            initialStart = false;
            nextExecutionTime.add(
                Calendar.SECOND, Integer.valueOf(env.getProperty("import.delay.initial.seconds")));
            return nextExecutionTime.getTime();
          }

          nextExecutionTime.add(
              Calendar.SECOND, Integer.valueOf(env.getProperty("import.delay.seconds")));

          return nextExecutionTime.getTime();
        });
  }
}
