package com.farmdesk.api.membership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
}
