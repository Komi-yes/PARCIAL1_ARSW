package edu.eci.arsw.blueprints.services;

import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.persistence.impl.HospitalNotFoundException;
import edu.eci.arsw.blueprints.persistence.impl.HospitalPersistence;
import edu.eci.arsw.blueprints.persistence.impl.HospitalPersistenceException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HospitalServices {

    private final HospitalPersistence persistence;
    private final BlueprintsFilter filter;

    public HospitalServices(HospitalPersistence persistence, BlueprintsFilter filter) {
        this.persistence = persistence;
        this.filter = filter;
    }

    public void addNewBlueprint(Blueprint bp) throws HospitalPersistenceException {
        persistence.saveTicket(bp);
    }

    public Set<Blueprint> getAllBlueprints() {
        return persistence.getAllBlueprints();
    }

    public Set<Blueprint> getBlueprintsByAuthor(String author) throws HospitalNotFoundException {
        return persistence.getTicketById(author);
    }

    public Blueprint getBlueprint(String author, String name) throws HospitalNotFoundException {
        return filter.apply(persistence.getCalledTicket());
    }

    public void addPoint(String author, String name, int x, int y) throws HospitalNotFoundException {
        persistence.CallTicket(author);
    }
}
