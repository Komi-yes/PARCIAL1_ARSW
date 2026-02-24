package edu.eci.arsw.tickets.persistence;

import edu.eci.arsw.tickets.model.Ticket;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.eci.arsw.tickets.model.TicketState.CALLED;
import static edu.eci.arsw.tickets.model.TicketState.COMPLETED;

@Repository
@Primary
public class PostgresTicketPersistence implements TicketPersistence {

    private final JpaTicketRepository repo;

    public PostgresTicketPersistence(JpaTicketRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveTicket(Ticket ticket) throws TicketPersistenceException {
        if (repo.findById(ticket.getId()).isPresent()) {
            throw new TicketPersistenceException("Blueprint already exists: "
                    + ticket.getId());
        }
        repo.save(ticket);
    }

    @Override
    public Ticket getCalledTicket() throws TicketNotFoundException {
        return repo.findCalledTicket(CALLED)
                .orElseThrow(() -> new TicketNotFoundException(
                        "Ticket not found: %s".formatted(CALLED)));
    }

    @Override
    public Ticket getTicketById(long id) throws TicketNotFoundException {
        Optional<Ticket> ticket = repo.findById(id);
        if (ticket.isEmpty()) throw new TicketNotFoundException(
                "No blueprints for author: " + id);
        return ticket.get();
    }

    @Override
    public Set<Ticket> getAllTickets() {
        return new HashSet<>(repo.findAll());
    }

    @Override
    public void CallNextTicket() throws TicketNotFoundException {
        Ticket ticket = getCalledTicket();
        long nextTicketId = ticket.getId() + 1;
        ticket.setState(COMPLETED);
        repo.save(ticket);
        ticket = getTicketById(nextTicketId);
        ticket.setState(CALLED);
        repo.save(ticket);
    }
}