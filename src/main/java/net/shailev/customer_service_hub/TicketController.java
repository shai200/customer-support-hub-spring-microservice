package net.shailev.customer_service_hub;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/tickets")
    public TicketResponse createTicket(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        return ticketService.createTicket(userDetails.getUsername(), request);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getOwnTickets(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ticketService.getOwnTickets(userDetails.getUsername());
    }

    @GetMapping("/agent/tickets")
    public List<TicketResponse> searchCustomerTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "search", required = false) String search
    ) {
        return ticketService.searchAgentCustomerTickets(userDetails.getUsername(), search);
    }
}
