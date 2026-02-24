package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Ticket;

import java.util.Set;

public interface HospitalPersistence {

    void saveTicket(Ticket ticket) throws HospitalPersistenceException;

    Ticket getCalledTicket() throws HospitalNotFoundException;

    Ticket getTicketById(long id) throws HospitalNotFoundException;

    Set<Ticket> getAllTickets();

    void CallTicket() throws HospitalNotFoundException;
}
