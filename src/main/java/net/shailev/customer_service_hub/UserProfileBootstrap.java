package net.shailev.customer_service_hub;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserProfileBootstrap implements CommandLineRunner {

    private final UserProfileRepository userProfileRepository;

    public UserProfileBootstrap(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createIfMissing("admin", RoleType.ADMIN, null, "System Admin", "admin@customerhub.local");
        createIfMissing("developer", RoleType.AGENT, null, "Developer Agent", "developer@customerhub.local");
        createIfMissing("agent", RoleType.AGENT, null, "Support Agent", "agent@customerhub.local");
        createIfMissing("customer", RoleType.CUSTOMER, "developer", "Default Customer", "customer@customerhub.local");
    }

    private void createIfMissing(String username, RoleType roleType, String agentUsername, String fullName, String email) {
        if (userProfileRepository.existsByUsername(username)) {
            return;
        }

        UserProfile profile = new UserProfile();
        profile.setUsername(username);
        profile.setRoleType(roleType);
        profile.setFullName(fullName);
        profile.setEmail(email);

        if (agentUsername != null && !agentUsername.isBlank()) {
            UserProfile agent = userProfileRepository.findByUsername(agentUsername)
                    .orElseThrow(() -> new IllegalStateException("Agent profile not found: " + agentUsername));
            profile.setAgent(agent);
        }

        userProfileRepository.save(profile);
    }
}
