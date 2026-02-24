package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.NOTUSING.Blueprint;
import edu.eci.arsw.blueprints.model.NOTUSING.Point;
import edu.eci.arsw.blueprints.model.Ticket;
import edu.eci.arsw.blueprints.persistence.HospitalNotFoundException;
import edu.eci.arsw.blueprints.persistence.HospitalPersistenceException;
import edu.eci.arsw.blueprints.services.HospitalServices;
import edu.eci.arsw.blueprints.socket.SocketIOClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/blueprints")
public class HospitalAPIController {
    private final HospitalServices services;
    private final SocketIOClientService socketIOClient;

    public HospitalAPIController(HospitalServices services, SocketIOClientService socketIOClient) {
        this.services = services;
        this.socketIOClient = socketIOClient;
    }
    // GET /api/v1/blueprints
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
        return ResponseEntity.ok(
                new ApiResponse<>(200, "execute ok", tickets)
        );
    }
    // GET /api/v1/blueprints/{author}
    @Operation(
            summary = "Obtener blueprints por autor",
            description = "Retorna los blueprints asociados a un autor."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor no encontrado o sin blueprints",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byId(@PathVariable String id) {
        try {
            Ticket ticket = services.getTicketById(id);
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", ticket)
            );
        } catch (HospitalNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation(
            summary = "Obtener blueprint por autor y nombre",
            description = "Retorna un blueprint específico identificado por autor y nombre."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @GetMapping("/called")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName() {
        try {
            Ticket ticket = services.getCalledTicket();
            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", ticket)
            );
        } catch (HospitalNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ApiResponse<>(404, e.getMessage(), null)
            );
        }
    }
    // POST /api/v1/blueprints
    @Operation(
            summary = "Crear un nuevo blueprint",
            description = "Crea un blueprint con autor, nombre y lista de puntos."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint ya existe o no se puede persistir",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación en la solicitud",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<?>> add() {
        try {
            Ticket ticket = new Ticket();
            services.addNewTicket(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(201, "Blueprint created successfully", null)
            );
        } catch (HospitalPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ApiResponse<>(403, e.getMessage(), null)
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
