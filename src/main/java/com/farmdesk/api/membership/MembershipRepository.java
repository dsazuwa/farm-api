package com.farmdesk.api.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

  List<Membership> findByUserId(UUID userId);

  boolean existsByFarmIdAndUserId(UUID farmId, UUID userId);

}
