package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.model.NOTUSING.Blueprint;
import edu.eci.arsw.blueprints.model.Ticket;
import edu.eci.arsw.blueprints.persistence.HospitalNotFoundException;
import edu.eci.arsw.blueprints.persistence.HospitalPersistence;
import edu.eci.arsw.blueprints.persistence.HospitalPersistenceException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HospitalServices {

    private final HospitalPersistence persistence;

    public HospitalServices(HospitalPersistence persistence) {
        this.persistence = persistence;
    }

    public void addNewTicket(Ticket ticket) throws HospitalPersistenceException {
        persistence.saveTicket(ticket);
    }

    public Set<Ticket> getAllTickets() {
        return persistence.getAllTickets();
    }

    public Ticket getTicketById(long id) throws HospitalNotFoundException {
        return persistence.getTicketById(id);
    }

    public Ticket getCalledTicket() throws HospitalNotFoundException {
        return persistence.getCalledTicket();
    }
}
