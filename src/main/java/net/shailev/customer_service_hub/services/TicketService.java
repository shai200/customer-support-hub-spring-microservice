package net.shailev.customer_service_hub.services;

import net.shailev.customer_service_hub.jpa.Ticket;
import net.shailev.customer_service_hub.jpa.UserProfile;
import net.shailev.customer_service_hub.repositories.TicketRepository;
import net.shailev.customer_service_hub.repositories.UserProfileRepository;
import net.shailev.customer_service_hub.responses.TicketResponse;
import net.shailev.customer_service_hub.types.RoleType;
import net.shailev.customer_service_hub.validators.CreateTicketRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserProfileRepository userProfileRepository;

    public TicketService(TicketRepository ticketRepository, UserProfileRepository userProfileRepository) {
        this.ticketRepository = ticketRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public TicketResponse createTicket(String actorUsername, CreateTicketRequest request) {
        UserProfile actor = findUserByUsername(actorUsername);

        UserProfile customer = resolveCustomerForCreate(actor, request.customerUsername());

        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setCustomer(customer);

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getOwnTickets(String actorUsername) {
        UserProfile actor = findUserByUsername(actorUsername);

        if (actor.getRoleType() == RoleType.ADMIN) {
            return ticketRepository.findAllByOrderByIdDesc().stream()
                    .map(this::toResponse)
                    .toList();
        }

        if (actor.getRoleType() != RoleType.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can query their own tickets");
        }

        return ticketRepository.findByCustomerUsernameOrderByIdDesc(actor.getUsername()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> searchAgentCustomerTickets(String actorUsername, String search) {
        UserProfile actor = findUserByUsername(actorUsername);

        List<Ticket> tickets;
        if (actor.getRoleType() == RoleType.ADMIN) {
            tickets = ticketRepository.findAllByOrderByIdDesc();
        } else if (actor.getRoleType() == RoleType.AGENT) {
            tickets = ticketRepository.findByCustomerAgentUsernameOrderByIdDesc(actor.getUsername());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only agents can query tickets from their customers");
        }

        String normalized = normalize(search);
        if (normalized == null) {
            return tickets.stream().map(this::toResponse).toList();
        }

        return tickets.stream()
                .filter(ticket -> containsNormalized(ticket.getTitle(), normalized)
                        || containsNormalized(ticket.getDescription(), normalized)
                        || containsNormalized(ticket.getCustomer().getUsername(), normalized))
                .map(this::toResponse)
                .toList();
    }

    private UserProfile resolveCustomerForCreate(UserProfile actor, String requestedCustomerUsername) {
        if (actor.getRoleType() == RoleType.CUSTOMER) {
            return actor;
        }

        if (actor.getRoleType() == RoleType.ADMIN) {
            if (requestedCustomerUsername == null || requestedCustomerUsername.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerUsername is required for admin ticket creation");
            }

            UserProfile customer = findUserByUsername(requestedCustomerUsername);
            if (customer.getRoleType() != RoleType.CUSTOMER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerUsername must belong to a CUSTOMER");
            }
            return customer;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only customers can create tickets");
    }

    private UserProfile findUserByUsername(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for username: " + username));
    }

    private TicketResponse toResponse(Ticket ticket) {
        UserProfile customer = ticket.getCustomer();
        UserProfile agent = customer.getAgent();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                customer.getUsername(),
                agent == null ? null : agent.getUsername(),
                ticket.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private boolean containsNormalized(String text, String normalizedQuery) {
        if (text == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}
