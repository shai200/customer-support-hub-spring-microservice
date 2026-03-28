package net.shailev.customer_service_hub;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
            Authentication authentication,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        return ticketService.createTicket(authentication.getName(), request);
    }

    @GetMapping("/tickets")
    public List<TicketResponse> getOwnTickets(
            Authentication authentication
    ) {
        return ticketService.getOwnTickets(authentication.getName());
    }

    @GetMapping("/agent/tickets")
    public List<TicketResponse> searchCustomerTickets(
            Authentication authentication,
            @RequestParam(name = "search", required = false) String search
    ) {
        return ticketService.searchAgentCustomerTickets(authentication.getName(), search);
    }
}
