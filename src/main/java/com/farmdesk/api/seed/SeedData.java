package com.farmdesk.api.seed;

import com.farmdesk.api.farm.Farm;
import com.farmdesk.api.farm.FarmRepository;
import com.farmdesk.api.membership.Membership;
import com.farmdesk.api.membership.MembershipRepository;
import com.farmdesk.api.sector.Sector;
import com.farmdesk.api.sector.SectorRepository;
import com.farmdesk.api.user.User;
import com.farmdesk.api.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("dev")
public class SeedData {

  private final UserRepository userRepository;
  private final FarmRepository farmRepository;
  private final MembershipRepository membershipRepository;
  private final SectorRepository sectorRepository;

  public SeedData(
      UserRepository userRepository,
      FarmRepository farmRepository,
      MembershipRepository membershipRepository,
      SectorRepository sectorRepository
  ) {
    this.userRepository = userRepository;
    this.farmRepository = farmRepository;
    this.membershipRepository = membershipRepository;
    this.sectorRepository = sectorRepository;
  }

  @Transactional
  public void seed() {
    User patrick = seedUser(
        "20000000-0000-4000-8000-000000000001",
        "patrick@example.com"
    );

    User okpara = seedUser(
        "20000000-0000-4000-8000-000000000002",
        "okpara@example.com"
    );

    User danielle = seedUser(
        "20000000-0000-4000-8000-000000000003",
        "danielle@example.com"
    );

    User nelson = seedUser(
        "20000000-0000-4000-8000-000000000004",
        "nelson@example.com"
    );

    Farm afumnaza = seedFarm(patrick, "Afumnaza Farm", "Asaba", "Delta");
    Farm okparaFarm = seedFarm(okpara, "Okpara Layers", "Ughelli", "Delta");
    Farm farmFresh = seedFarm(danielle, "Farm Fresh", "Lagos", "Lagos");

    seedMembership(okpara, afumnaza, Membership.Role.viewer);
    seedMembership(danielle, afumnaza, Membership.Role.manager);
    seedMembership(nelson, afumnaza, Membership.Role.manager);
    seedMembership(danielle, okparaFarm, Membership.Role.viewer);

    seedSector(afumnaza, Sector.Type.layers);
    seedSector(afumnaza, Sector.Type.broilers);
    seedSector(afumnaza, Sector.Type.fishery);
    seedSector(afumnaza, Sector.Type.piggery);
    seedSector(afumnaza, Sector.Type.crops);

    seedSector(okparaFarm, Sector.Type.layers);

    seedSector(farmFresh, Sector.Type.broilers);
    seedSector(farmFresh, Sector.Type.fishery);
  }

  private User seedUser(String authId, String email) {
    UUID authUserId = UUID.fromString(authId);

    return userRepository.findByAuthUserId(authUserId)
        .orElseGet(() -> userRepository.save(new User(authUserId, email)));
  }

  private Farm seedFarm(User owner, String name, String city, String state) {
    List<Membership> memberships = membershipRepository.findByUserId(owner.getId());

    for (Membership membership : memberships) {
      if (membership.getRole() != Membership.Role.owner) continue;

      Optional<Farm> farm = farmRepository.findById(membership.getFarmId());

      if (farm.isPresent() && Objects.equals(farm.get().getName(), name))
        return farm.get();
    }

    Farm farm = new Farm(name);
    farm.setCity(city);
    farm.setState(state);

    Farm savedFarm = farmRepository.save(farm);

    membershipRepository.save(new Membership(
        owner.getId(),
        savedFarm.getId(),
        Membership.Role.owner,
        Membership.SectorAccessScope.all
    ));

    return savedFarm;
  }

  private void seedMembership(User user, Farm farm, Membership.Role role) {
    boolean exists = membershipRepository.existsByFarmIdAndUserId(
        farm.getId(),
        user.getId()
    );

    if (!exists)
      membershipRepository.save(new Membership(
          user.getId(),
          farm.getId(),
          role,
          Membership.SectorAccessScope.all
      ));
  }

  private void seedSector(Farm farm, Sector.Type type) {
    boolean exists = sectorRepository.existsByFarmIdAndNameAndType(
        farm.getId(), type.toString(), type
    );

    if (!exists) sectorRepository.save(new Sector(farm.getId(), type));
  }
}
