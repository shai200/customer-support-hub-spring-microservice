package net.shailev.customer_service_hub.repositories;

import net.shailev.customer_service_hub.jpa.UserProfile;
import net.shailev.customer_service_hub.types.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);

    List<UserProfile> findByAgentUsernameAndRoleType(String agentUsername, RoleType roleType);

    List<UserProfile> findByRoleType(RoleType roleType);

    boolean existsByUsername(String username);
}
