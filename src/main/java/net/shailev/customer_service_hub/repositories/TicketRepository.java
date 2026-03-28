package net.shailev.customer_service_hub.repositories;

import net.shailev.customer_service_hub.jpa.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerUsernameOrderByIdDesc(String customerUsername);

    List<Ticket> findByCustomerAgentUsernameOrderByIdDesc(String agentUsername);

    List<Ticket> findAllByOrderByIdDesc();
}
