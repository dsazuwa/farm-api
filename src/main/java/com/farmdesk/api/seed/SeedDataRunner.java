package com.farmdesk.api.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class SeedDataRunner implements CommandLineRunner {

  private final SeedData seedService;

  public SeedDataRunner(SeedData seedService) {
    this.seedService = seedService;
  }

  @Override
  public void run(String... args) {
    seedService.seed();
  }
}
