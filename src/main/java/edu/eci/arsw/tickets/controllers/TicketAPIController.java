package edu.eci.arsw.tickets.controllers;

import edu.eci.arsw.tickets.model.Ticket;
import edu.eci.arsw.tickets.persistence.TicketNotFoundException;
import edu.eci.arsw.tickets.persistence.TicketPersistenceException;
import edu.eci.arsw.tickets.services.TicketServices;
import edu.eci.arsw.tickets.socket.SocketIOClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/tickets")
public class TicketAPIController {
    private final TicketServices services;
    private final SocketIOClientService socketIOClient;

    public TicketAPIController(TicketServices services, SocketIOClientService socketIOClient) {
        this.services = services;
        this.socketIOClient = socketIOClient;
    }
    // GET /api/v1/tickets
    @Operation(
            summary = "Obtener todos los tickets",
            description = "Retorna todos los tickets almacenados."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<?>>> getAll() {
        Set<Ticket> tickets = services.getAllTickets();
        socketIOClient.sendDrawEvent();
        return ResponseEntity.ok(
                new ApiResponse<>(200, "execute ok", tickets)
        );
    }
    // GET /api/v1/tickets/{id}
    @Operation(
            summary = "Obtener ticket por id",
            description = "Retorna el ticket asociado a un id."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "id no encontrado o sin ticket",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> byId(@PathVariable long id) {
        try {
            Ticket ticket = services.getTicketById(id);
            socketIOClient.sendDrawEvent();
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", ticket)
            );
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // GET /api/v1/tickets/called
    @Operation(
            summary = "Obtener el ticket con estado CALLED",
            description = "Retorna un ticket con el estado de CALLED"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/called")
    public ResponseEntity<ApiResponse<?>> calledTicket() {
        try {
            Ticket ticket = services.getCalledTicket();
            socketIOClient.sendDrawEvent();
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", ticket)
            );
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // POST /api/v1/tickets/create
    @Operation(
            summary = "Crear un nuevo ticket",
            description = "Crea un ticket con autor, nombre y lista de puntos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Ticket creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Ticket ya existe o no se puede persistir",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación en la solicitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> add() {
        try {
            Ticket ticket = new Ticket();
            services.addNewTicket(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(201, "Ticket created successfully", null)
            );
        } catch (TicketPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(403, e.getMessage(), null)
            );
        }
    }
    // PUT /api/v1/tickets/call
    @Operation(
            summary = "Llama al siguiente ticket que este en estado CREATED y cambia el estado del anterior CALLED",
            description = "Llama al siguiente ticket que este en estado CREATED y los cambia a CALLED, y cambia el estado del anterior CALLED"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Ticket no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @PutMapping("/call")
    public ResponseEntity<ApiResponse<?>> call() {
        try {
            services.callNextTicket();
            socketIOClient.sendDrawEvent();

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    new ApiResponse<>(202, "called successfully", null)
            );
        } catch (TicketNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiResponse<>(400, "Validation error: " + message, null)
        );
    }
    public record ApiResponse<T>(int code, String message, T data) {}
}
