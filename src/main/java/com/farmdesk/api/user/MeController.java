package com.farmdesk.api.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
public class MeController {

  private final UserRepository userRepository;

  public MeController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/api/me")
  public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
    UUID authUserId = UUID.fromString(jwt.getSubject());

    User user = userRepository.findByAuthUserId(authUserId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "User profile not found"
        ));

    return new MeResponse(user.getId(), authUserId, user.getEmail());
  }

  public record MeResponse(UUID id, UUID authUserId, String email) {
  }
}