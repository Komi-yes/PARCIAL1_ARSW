package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.NOTUSING.Blueprint;
import edu.eci.arsw.blueprints.model.NOTUSING.Point;
import edu.eci.arsw.blueprints.model.Ticket;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static edu.eci.arsw.blueprints.model.TicketState.CALLED;

@Repository
@Primary
public class PostgresHospitalPersistence implements HospitalPersistence {

    private final JpaHospitalRepository repo;

    public PostgresHospitalPersistence(JpaHospitalRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveTicket(Ticket ticket) throws HospitalPersistenceException {
        if (repo.findById(ticket.getId()).isPresent()) {
            throw new HospitalPersistenceException("Blueprint already exists: "
                    + ticket.getId());
        }
        repo.save(ticket);
    }

    @Override
    public Ticket getCalledTicket() throws HospitalNotFoundException {
        return repo.findCalledTicket(CALLED)
                .orElseThrow(() -> new HospitalNotFoundException(
                        "Ticket not found: %s".formatted(CALLED)));
    }

    @Override
    public Ticket getTicketById(long id) throws HospitalNotFoundException {
        Optional<Ticket> list = repo.findById(id);
        if (list.isEmpty()) throw new HospitalNotFoundException(
                "No blueprints for author: " + id);
        return new HashSet<>(list);
    }

    @Override
    public Set<Ticket> getAllTickets() {
        return new HashSet<>(repo.findAll());
    }

    @Override
    public void CallTicket() throws HospitalNotFoundException {
        Blueprint bp = getCalledTicket();
        bp.addPoint(new Point(x, y));
        repo.save(bp);
    }
}