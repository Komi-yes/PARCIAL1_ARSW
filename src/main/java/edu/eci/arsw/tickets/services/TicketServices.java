package edu.eci.arsw.tickets.services;

import edu.eci.arsw.tickets.model.Ticket;
import edu.eci.arsw.tickets.persistence.TicketNotFoundException;
import edu.eci.arsw.tickets.persistence.TicketPersistence;
import edu.eci.arsw.tickets.persistence.TicketPersistenceException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TicketServices {

    private final TicketPersistence persistence;

    public TicketServices(TicketPersistence persistence) {
        this.persistence = persistence;
    }

    public void addNewTicket(Ticket ticket) throws TicketPersistenceException {
        persistence.saveTicket(ticket);
    }

    public Set<Ticket> getAllTickets() {
        return persistence.getAllTickets();
    }

    public Ticket getTicketById(long id) throws TicketNotFoundException {
        return persistence.getTicketById(id);
    }

    public Ticket getCalledTicket() throws TicketNotFoundException {
        return persistence.getCalledTicket();
    }

    public void callNextTicket() throws TicketNotFoundException {
        persistence.CallNextTicket();
    }
}
