package com.farmdesk.api.membership;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "membership_sector_access")
public class MembershipSectorAccess {

  @EmbeddedId
  private MembershipSectorAccessId id;

  @Column(name = "farm_id", nullable = false)
  private UUID farmId;

  protected MembershipSectorAccess() {
  }

  public MembershipSectorAccess(
      UUID membershipId,
      UUID sectorId,
      UUID farmId
  ) {
    this.id = new MembershipSectorAccessId(membershipId, sectorId);
    this.farmId = Objects.requireNonNull(
        farmId, "Farm ID is required"
    );
  }

  public MembershipSectorAccessId getId() {
    return id;
  }

  public UUID getMembershipId() {
    return id.getMembershipId();
  }

  public UUID getSectorId() {
    return id.getSectorId();
  }

  public UUID getFarmId() {
    return farmId;
  }
}
