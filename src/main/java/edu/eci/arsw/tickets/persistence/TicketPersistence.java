package edu.eci.arsw.tickets.persistence;

import edu.eci.arsw.tickets.model.Ticket;

import java.util.Set;

public interface TicketPersistence {

    void saveTicket(Ticket ticket) throws TicketPersistenceException;

    Ticket getCalledTicket() throws TicketNotFoundException;

    Ticket getTicketById(long id) throws TicketNotFoundException;

    Set<Ticket> getAllTickets();

    void CallNextTicket() throws TicketNotFoundException;
}
