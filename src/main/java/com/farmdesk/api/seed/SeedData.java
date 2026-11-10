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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("dev")
public class SeedData {

  private static final Logger log =
      LoggerFactory.getLogger(SeedData.class);
  private final UserRepository userRepository;
  private final FarmRepository farmRepository;
  private final MembershipRepository membershipRepository;
  private final SectorRepository sectorRepository;

  private final UUID patrickAuthId;
  private final UUID nelsonAuthId;
  private final UUID danielleAuthId;
  private final UUID okparaAuthId;

  public SeedData(
      UserRepository userRepository,
      FarmRepository farmRepository,
      MembershipRepository membershipRepository,
      SectorRepository sectorRepository,
      @Value("${app.seed.users.patrick.auth-id}") UUID patrickAuthId,
      @Value("${app.seed.users.nelson.auth-id}") UUID nelsonAuthId,
      @Value("${app.seed.users.danielle.auth-id}") UUID danielleAuthId,
      @Value("${app.seed.users.okpara.auth-id}") UUID okparaAuthId
  ) {
    this.userRepository = userRepository;
    this.farmRepository = farmRepository;
    this.membershipRepository = membershipRepository;
    this.sectorRepository = sectorRepository;

    this.patrickAuthId = patrickAuthId;
    this.nelsonAuthId = nelsonAuthId;
    this.danielleAuthId = danielleAuthId;
    this.okparaAuthId = okparaAuthId;
  }

  @Transactional
  public void seed() {
    User patrick = seedUser(patrickAuthId, "patrick@example.com");
    User nelson = seedUser(nelsonAuthId, "nelson@example.com");
    User danielle = seedUser(danielleAuthId, "danielle@example.com");
    User okpara = seedUser(okparaAuthId, "okpara@example.com");

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

  private User seedUser(UUID authUserId, String email) {
    Optional<User> existingUser =
        userRepository.findByAuthUserId(authUserId);

    if (existingUser.isPresent()) {
      User user = existingUser.get();

      log.debug("Skipping existing seed user: id={}, email={}",
          user.getId(), user.getEmail());

      return user;
    }

    User user = userRepository.save(new User(authUserId, email));
    log.debug("Created seed user: id={}, email={}",
        user.getId(), user.getEmail());

    return user;
  }

  private Farm seedFarm(User owner, String name, String city, String state) {
    List<Membership> memberships = membershipRepository.findByUserId(owner.getId());

    for (Membership membership : memberships) {
      if (membership.getRole() != Membership.Role.owner) continue;

      Optional<Farm> farm = farmRepository.findById(membership.getFarmId());

      if (farm.isPresent() && Objects.equals(farm.get().getName(), name)) {
        log.debug("Skipping existing seed farm: id={}, name={}",
            farm.get().getId(), name);
        return farm.get();
      }
    }

    Farm farm = new Farm(name);
    farm.setCity(city);
    farm.setState(state);

    Farm savedFarm = farmRepository.save(farm);

    log.debug("Created seed farm: id={}, name={}",
        savedFarm.getId(), savedFarm.getName());

    membershipRepository.save(new Membership(
        owner.getId(),
        savedFarm.getId(),
        Membership.Role.owner,
        Membership.SectorAccessScope.all
    ));

    log.debug("Created seed owner membership: user={}, farm={}",
        owner.getEmail(), savedFarm.getName());

    return savedFarm;
  }

  private void seedMembership(User user, Farm farm, Membership.Role role) {
    boolean exists = membershipRepository.existsByFarmIdAndUserId(
        farm.getId(),
        user.getId()
    );

    if (exists) {
      log.debug("Skipping existing seed membership: user={}, farm={}, role={}",
          user.getEmail(), farm.getName(), role);
      return;
    }

    membershipRepository.save(new Membership(
        user.getId(),
        farm.getId(),
        role,
        Membership.SectorAccessScope.all
    ));

    log.debug("Created seed membership: user={}, farm={}, role={}",
        user.getEmail(), farm.getName(), role);
  }

  private void seedSector(Farm farm, Sector.Type type) {
    boolean exists = sectorRepository.existsByFarmIdAndNameAndType(
        farm.getId(), type.toString(), type
    );

    if (exists) {
      log.debug("Skipping existing seed sector: farm={}, type={}",
          farm.getName(), type);
      return;
    }

    Sector savedSector = sectorRepository.save(new Sector(farm.getId(), type));

    log.debug("Created seed sector: id={}, farm={}, type={}",
        savedSector.getId(), farm.getName(), type);
  }


}
