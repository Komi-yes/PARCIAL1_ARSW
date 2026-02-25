import { useEffect, useState, useRef } from 'react'
import { createSocket } from './lib/socketIoClient.js'
import { createStompClient, subscribeToTickets } from './lib/stompClient.js'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'
const IO_BASE  = import.meta.env.VITE_IO_BASE  ?? 'http://localhost:3001'

export default function App() {
    const [tech, setTech] = useState('socketio')
    const [tickets, setTickets] = useState([])
    const [calledTicket, setCalledTicket] = useState(null)
    const [loading, setLoading] = useState(false)

    const socketRef = useRef(null)
    const stompRef = useRef(null)
    const unsubRef = useRef(null)

    // Fetch all tickets
    const fetchTickets = async () => {
        try {
            const base = tech === 'stomp' ? API_BASE : IO_BASE
            const response = await fetch(`${base}/api/v1/tickets`)
            const data = await response.json()
            if (data?.data) {
                setTickets(data.data)
            }
        } catch (error) {
            console.error('Error fetching tickets:', error)
        }
    }

    // Fetch called ticket
    const fetchCalledTicket = async () => {
        try {
            const base = tech === 'stomp' ? API_BASE : IO_BASE
            const response = await fetch(`${base}/api/v1/tickets/called`)
            const data = await response.json()
            if (data?.data) {
                setCalledTicket(data.data)
            }
        } catch (error) {
            console.error('Error fetching called ticket:', error)
        }
    }

    // Create new ticket
    const createTicket = async () => {
        setLoading(true)
        try {
            const base = tech === 'stomp' ? API_BASE : IO_BASE
            await fetch(`${base}/api/v1/tickets/create`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            // Refresh data after creating
            await fetchTickets()
            await fetchCalledTicket()
        } catch (error) {
            console.error('Error creating ticket:', error)
        } finally {
            setLoading(false)
        }
    }

    // Call next ticket
    const callNextTicket = async () => {
        setLoading(true)
        try {
            const base = tech === 'stomp' ? API_BASE : IO_BASE
            await fetch(`${base}/api/v1/tickets/call`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' }
            })
            await fetchTickets()
            await fetchCalledTicket()
        } catch (error) {
            console.error('Error calling ticket:', error)
        } finally {
            setLoading(false)
        }
    }

    // Load initial data only once when component mounts
    useEffect(() => {
        fetchTickets()
        fetchCalledTicket()
    }, [])

    useEffect(() => {
        socketRef.current?.disconnect?.()
        socketRef.current = null
        stompRef.current?.deactivate?.()
        stompRef.current = null
        unsubRef.current?.()
        unsubRef.current = null

        if (tech === 'socketio') {
            const s = createSocket(IO_BASE)
            socketRef.current = s

            const room = `HOSPITAL`

            s.on('connect', () => {
                s.emit('join-room', room)
            })

            s.on('ticket-update', (upd) => {
                console.log('Ticket update received:', upd)
                // No hacer fetch automático para evitar ciclo infinito
            })

            s.on('draw-error', (err) => {
                console.warn('Socket error:', err)
            })
        } else if (tech === 'stomp') {
            const client = createStompClient(API_BASE)
            stompRef.current = client

            client.onConnect = () => {
                console.log('STOMP connected')
                unsubRef.current = subscribeToTickets(client, (upd) => {
                    console.log('STOMP ticket update received:', upd)
                    // No hacer fetch automático para evitar ciclo infinito
                })
            }

            client.activate()
        }

        return () => {
            socketRef.current?.disconnect?.()
            stompRef.current?.deactivate?.()
            unsubRef.current?.()
        }
    }, [tech])

    const waitingTickets = tickets.filter(ticket => ticket.state === 'CREATED')
    const completedTickets = tickets.filter(ticket => ticket.state === 'COMPLETED')

    return (
        <div style={{ fontFamily: 'Inter, system-ui', padding: 16, maxWidth: 1200 }}>
            <h2>Sistema de Gestión de Tickets</h2>

            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 24 }}>
                <label>Tecnología:</label>
                <select value={tech} onChange={e => setTech(e.target.value)}>
                    <option value="stomp">STOMP (Spring)</option>
                    <option value="socketio">Socket.IO (Node)</option>
                </select>
            </div>

            {/* Action Buttons */}
            <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
                <button
                    onClick={createTicket}
                    disabled={loading}
                    style={{
                        padding: '8px 16px',
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        borderRadius: 6,
                        cursor: loading ? 'not-allowed' : 'pointer',
                        opacity: loading ? 0.6 : 1
                    }}
                >
                    {loading ? 'Procesando...' : 'Crear Nuevo Ticket'}
                </button>
                <button
                    onClick={callNextTicket}
                    disabled={loading || waitingTickets.length === 0}
                    style={{
                        padding: '8px 16px',
                        backgroundColor: '#28a745',
                        color: 'white',
                        border: 'none',
                        borderRadius: 6,
                        cursor: (loading || waitingTickets.length === 0) ? 'not-allowed' : 'pointer',
                        opacity: (loading || waitingTickets.length === 0) ? 0.6 : 1
                    }}
                >
                    {loading ? 'Procesando...' : 'Llamar Siguiente Ticket'}
                </button>
                <button
                    onClick={() => {
                        fetchTickets()
                        fetchCalledTicket()
                    }}
                    disabled={loading}
                    style={{
                        padding: '8px 16px',
                        backgroundColor: '#6c757d',
                        color: 'white',
                        border: 'none',
                        borderRadius: 6,
                        cursor: loading ? 'not-allowed' : 'pointer',
                        opacity: loading ? 0.6 : 1
                    }}
                >
                    Refrescar Datos
                </button>
            </div>

            {/* Called Ticket Display */}
            <div style={{ marginBottom: 24 }}>
                <h3>Ticket Actualmente Llamado</h3>
                {calledTicket ? (
                    <div style={{
                        backgroundColor: '#fff3cd',
                        border: '1px solid #ffeaa7',
                        borderRadius: 8,
                        padding: 16,
                        fontSize: '18px',
                        fontWeight: 'bold'
                    }}>
                        <div style={{ color: '#856404' }}>
                            Ticket #{calledTicket.id} - Estado: {calledTicket.state}
                        </div>
                    </div>
                ) : (
                    <div style={{
                        backgroundColor: '#f8d7da',
                        border: '1px solid #f5c6cb',
                        borderRadius: 8,
                        padding: 16,
                        color: '#721c24'
                    }}>
                        No hay tickets llamados actualmente
                    </div>
                )}
            </div>

            {/* Waiting Tickets */}
            <div style={{ marginBottom: 24 }}>
                <h3>Tickets en Espera ({waitingTickets.length})</h3>
                {waitingTickets.length > 0 ? (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                        gap: 12
                    }}>
                        {waitingTickets.map(ticket => (
                            <div
                                key={ticket.id}
                                style={{
                                    backgroundColor: '#d4edda',
                                    border: '1px solid #c3e6cb',
                                    borderRadius: 8,
                                    padding: 12,
                                    textAlign: 'center'
                                }}
                            >
                                <div style={{ fontWeight: 'bold', fontSize: '16px' }}>
                                    Ticket #{ticket.id}
                                </div>
                                <div style={{ fontSize: '14px', color: '#155724' }}>
                                    Estado: {ticket.state}
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div style={{
                        backgroundColor: '#e2e3e5',
                        border: '1px solid #d6d8db',
                        borderRadius: 8,
                        padding: 16,
                        textAlign: 'center',
                        color: '#6c757d'
                    }}>
                        No hay tickets en espera
                    </div>
                )}
            </div>

            {/* Completed Tickets */}
            <div>
                <h3>Tickets Completados ({completedTickets.length})</h3>
                {completedTickets.length > 0 ? (
                    <div style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))',
                        gap: 12
                    }}>
                        {completedTickets.map(ticket => (
                            <div
                                key={ticket.id}
                                style={{
                                    backgroundColor: '#e2e3e5',
                                    border: '1px solid #d6d8db',
                                    borderRadius: 8,
                                    padding: 12,
                                    textAlign: 'center',
                                    opacity: 0.7
                                }}
                            >
                                <div style={{ fontWeight: 'bold', fontSize: '16px' }}>
                                    Ticket #{ticket.id}
                                </div>
                                <div style={{ fontSize: '14px', color: '#6c757d' }}>
                                    Estado: {ticket.state}
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div style={{
                        backgroundColor: '#f8f9fa',
                        border: '1px solid #dee2e6',
                        borderRadius: 8,
                        padding: 16,
                        textAlign: 'center',
                        color: '#6c757d'
                    }}>
                        No hay tickets completados
                    </div>
                )}
            </div>

            <div style={{ marginTop: 24, fontSize: '12px', color: '#6c757d' }}>
                <p>Tip: Abre múltiples pestañas para ver las actualizaciones en tiempo real.</p>
                <p>Los tickets se actualizan automáticamente cuando se crean o llaman.</p>
            </div>
        </div>
    )
}