package com.farmdesk.api.membership;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memberships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "memberships_farm_user_unique",
            columnNames = {"farm_id", "user_id"}
        ),
        @UniqueConstraint(
            name = "memberships_id_farm_unique",
            columnNames = {"id", "farm_id"}
        )
    })
public class Membership {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "farm_id", nullable = false)
  private UUID farmId;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "sector_access_scope", nullable = false, length = 20)
  private SectorAccessScope sectorAccessScope;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Membership() {
  }

  public Membership(UUID userId, UUID farmId, Role role, SectorAccessScope sectorAccessScope) {
    if (role == Role.owner && sectorAccessScope != SectorAccessScope.all) {
      throw new IllegalArgumentException(
          "Owners must have access to all sectors."
      );
    }

    this.userId = userId;
    this.farmId = farmId;
    this.role = role;
    this.sectorAccessScope = sectorAccessScope;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getFarmId() {
    return farmId;
  }

  public Role getRole() {
    return role;
  }

  public SectorAccessScope getSectorAccessScope() {
    return sectorAccessScope;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public enum Role {
    owner,
    manager,
    accountant,
    attendant,
    viewer
  }

  public enum SectorAccessScope {
    all,
    selected
  }
}
