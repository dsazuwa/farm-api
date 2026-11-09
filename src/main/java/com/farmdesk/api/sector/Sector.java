package com.farmdesk.api.sector;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "sectors",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "sectors_id_farm_unique",
            columnNames = {"id", "farm_id"}
        )
    })
public class Sector {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false)
  private UUID id;

  @Column(name = "farm_id", nullable = false)
  private UUID farmId;

  @Column(nullable = false, length = 255)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Type type;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Sector() {
  }

  public Sector(UUID farmId, String name, Type type) {
    this.farmId = Objects.requireNonNull(farmId, "Farm ID is required");
    this.type = Objects.requireNonNull(type, "Sector type is required");
    setName(name);
  }

  public Sector(UUID farmId, Type type) {
    this.farmId = Objects.requireNonNull(farmId, "Farm ID is required");
    this.type = Objects.requireNonNull(type, "Sector type is required");
    setName(type.toString());
  }

  public UUID getId() {
    return id;
  }

  public UUID getFarmId() {
    return farmId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Sector name cannot be blank");
    }

    String trimmedName = name.trim();

    if (trimmedName.length() > 255) {
      throw new IllegalArgumentException(
          "Sector name cannot exceed 255 characters"
      );
    }

    this.name = trimmedName;
  }

  public Type getType() {
    return type;
  }

  public Instant getArchivedAt() {
    return archivedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void archive() {
    this.archivedAt = Instant.now();
  }

  public void restore() {
    this.archivedAt = null;
  }

  public enum Type {
    layers,
    broilers,
    piggery,
    fishery,
    crops,
    general
  }
}