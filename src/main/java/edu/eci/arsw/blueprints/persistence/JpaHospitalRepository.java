package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Ticket;
import edu.eci.arsw.blueprints.model.TicketState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaHospitalRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findCalledTicket(TicketState state);
}
