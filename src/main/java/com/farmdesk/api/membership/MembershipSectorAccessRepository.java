package com.farmdesk.api.membership;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipSectorAccessRepository
    extends JpaRepository<MembershipSectorAccess, MembershipSectorAccessId> {
}
