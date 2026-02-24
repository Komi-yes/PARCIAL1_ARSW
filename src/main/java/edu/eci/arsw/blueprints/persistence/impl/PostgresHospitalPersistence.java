package edu.eci.arsw.blueprints.persistence.impl;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
public class PostgresHospitalPersistence implements HospitalPersistence {

    private final JpaHospitalRepository repo;

    public PostgresHospitalPersistence(JpaHospitalRepository repo) {
        this.repo = repo;
    }

    @Override
    public void saveTicket(Blueprint bp) throws HospitalPersistenceException {
        if (repo.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new HospitalPersistenceException("Blueprint already exists: "
                    + bp.getAuthor() + "/" + bp.getName());
        }
        repo.save(bp);
    }

    @Override
    public Blueprint getCalledTicket() throws HospitalNotFoundException {
        return repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new HospitalNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    public Set<Blueprint> getTicketById(String id) throws HospitalNotFoundException {
        List<Blueprint> list = repo.findByAuthor(id);
        if (list.isEmpty()) throw new HospitalNotFoundException(
                "No blueprints for author: " + id);
        return new HashSet<>(list);
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        return new HashSet<>(repo.findAll());
    }

    @Override
    public void CallTicket(String id) throws HospitalNotFoundException {
        Blueprint bp = getCalledTicket();
        bp.addPoint(new Point(x, y));
        repo.save(bp);
    }
}