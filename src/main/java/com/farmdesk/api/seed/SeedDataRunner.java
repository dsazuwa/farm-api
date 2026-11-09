package com.farmdesk.api.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataRunner implements CommandLineRunner {

  private final SeedData seedService;

  private static final Logger log =
      LoggerFactory.getLogger(SeedDataRunner.class);

  public SeedDataRunner(SeedData seedService) {
    this.seedService = seedService;
  }

  @Override
  public void run(String... args) {
    log.info("Starting development data seeding");
    seedService.seed();
    log.info("Development data seeding completed");
  }
}
