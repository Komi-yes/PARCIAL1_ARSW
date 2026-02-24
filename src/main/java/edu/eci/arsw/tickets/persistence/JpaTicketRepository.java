package edu.eci.arsw.tickets.persistence;

import edu.eci.arsw.tickets.model.Ticket;
import edu.eci.arsw.tickets.model.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaTicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findTicketByState(TicketState state);
}
