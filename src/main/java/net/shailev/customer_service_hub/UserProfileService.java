package net.shailev.customer_service_hub;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResponse createCustomer(String actorUsername, CreateCustomerRequest request) {
        UserProfile actor = findByUsernameOrThrow(actorUsername);
        if (userProfileRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        UserProfile assignedAgent = resolveAssignedAgent(actor, request.agentUsername());

        UserProfile customer = new UserProfile();
        customer.setUsername(request.username());
        customer.setRoleType(RoleType.CUSTOMER);
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());
        customer.setAgent(assignedAgent);

        UserProfile saved = userProfileRepository.save(customer);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getCustomersForActor(String actorUsername) {
        UserProfile actor = findByUsernameOrThrow(actorUsername);

        if (actor.getRoleType() == RoleType.ADMIN) {
            return userProfileRepository.findByRoleType(RoleType.CUSTOMER).stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (actor.getRoleType() != RoleType.AGENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only agents can query their customers");
        }

        return userProfileRepository.findByAgentUsernameAndRoleType(actor.getUsername(), RoleType.CUSTOMER).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getOwnProfile(String actorUsername) {
        return toResponse(findByUsernameOrThrow(actorUsername));
    }

    public UserProfileResponse updateOwnProfile(String actorUsername, UpdateProfileRequest request) {
        UserProfile actor = findByUsernameOrThrow(actorUsername);

        actor.setFullName(request.fullName());
        actor.setEmail(request.email());

        UserProfile saved = userProfileRepository.save(actor);
        return toResponse(saved);
    }

    private UserProfile resolveAssignedAgent(UserProfile actor, String requestedAgentUsername) {
        if (actor.getRoleType() == RoleType.ADMIN) {
            if (requestedAgentUsername == null || requestedAgentUsername.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "agentUsername is required for admin customer creation");
            }

            UserProfile agent = findByUsernameOrThrow(requestedAgentUsername);
            if (agent.getRoleType() != RoleType.AGENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user must have AGENT role");
            }
            return agent;
        }

        if (actor.getRoleType() == RoleType.AGENT) {
            return actor;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only agents can create customers");
    }

    private UserProfile findByUsernameOrThrow(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for username: " + username));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        String agentUsername = profile.getAgent() == null ? null : profile.getAgent().getUsername();

        return new UserProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getRoleType(),
                profile.getFullName(),
                profile.getEmail(),
                agentUsername
        );
    }
}
