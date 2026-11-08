package com.farmdesk.api.membership;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MembershipSectorAccessId implements Serializable {

  @Column(name = "membership_id", nullable = false)
  private UUID membershipId;

  @Column(name = "sector_id", nullable = false)
  private UUID sectorId;

  protected MembershipSectorAccessId() {
  }

  public MembershipSectorAccessId(UUID membershipId, UUID sectorId) {
    this.membershipId = Objects.requireNonNull(
        membershipId, "Membership ID is required"
    );

    this.sectorId = Objects.requireNonNull(
        sectorId, "Sector ID is required"
    );
  }

  public UUID getSectorId() {
    return sectorId;
  }

  public UUID getMembershipId() {
    return membershipId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;

    if (!(other instanceof MembershipSectorAccessId that)) return false;

    return Objects.equals(membershipId, that.membershipId)
        && Objects.equals(sectorId, that.sectorId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(membershipId, sectorId);
  }
}
