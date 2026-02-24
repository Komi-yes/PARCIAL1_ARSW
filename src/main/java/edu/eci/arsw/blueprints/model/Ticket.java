package edu.eci.arsw.blueprints.model;

import jakarta.persistence.*;


@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private TicketState state;

    public Ticket() {
        this.state = TicketState.CREATED;
    }

    public long getId() {
        return id;
    }
}
