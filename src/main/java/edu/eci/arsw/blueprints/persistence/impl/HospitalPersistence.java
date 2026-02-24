package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Ticket;

import java.util.Set;

public interface HospitalPersistence {

    void saveTicket(Blueprint bp) throws HospitalPersistenceException;

    Ticket getCalledTicket() throws HospitalNotFoundException;

    Set<Ticket> getTicketById(String id) throws HospitalNotFoundException;

    Set<Ticket> getAllBlueprints();

    void CallTicket(String id) throws HospitalNotFoundException;
}
