import { useEffect, useRef, useState } from 'react'
import { createStompClient, subscribeBlueprint } from './lib/stompClient.js'
import { createSocket } from './lib/socketIoClient.js'

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'
const IO_BASE  = import.meta.env.VITE_IO_BASE  ?? 'http://localhost:3001'

export default function App() {
    const [tech, setTech] = useState('socketio')
    const [author, setAuthor] = useState('ejemplo')
    const [name, setName] = useState('ejemplo')

    const [points, setPoints] = useState([])

    const canvasRef = useRef(null)
    const ctxRef = useRef(null)

    const drawnCountRef = useRef(0)

    const stompRef = useRef(null)
    const unsubRef = useRef(null)
    const socketRef = useRef(null)

    function samePoint(a, b) {
        return !!a && !!b && a.x === b.x && a.y === b.y
    }

    function appendPoints(incoming = []) {
        if (!incoming || incoming.length === 0) return
        setPoints(prev => {
            const last = prev[prev.length - 1]
            const filtered = incoming.filter(p => !samePoint(last, p))
            if (filtered.length === 0) return prev
            return [...prev, ...filtered]
        })
    }

    useEffect(() => {
        const ctx = canvasRef.current?.getContext('2d')
        if (!ctx) return
        ctxRef.current = ctx

        ctx.lineWidth = 2
        ctx.lineJoin = 'round'
        ctx.lineCap = 'round'
    }, [])

    useEffect(() => {
        drawnCountRef.current = 0

        const ctx = ctxRef.current
        if (ctx) {
            ctx.clearRect(0, 0, 600, 400)
            ctx.beginPath()
        }

        setPoints([])
    }, [tech, author, name])

    useEffect(() => {
        const base = tech === 'stomp' ? API_BASE : IO_BASE
        fetch(`${base}/api/v1/blueprints/${author}/${name}`)
            .then(r => r.json())
            .then(response => {
                const pts = response?.data?.points ?? []
                setPoints(pts)
            })
            .catch(() => setPoints([]))
    }, [tech, author, name])

    useEffect(() => {
        const ctx = ctxRef.current
        if (!ctx) return

        for (let i = drawnCountRef.current; i < points.length; i++) {
            const p = points[i]

            if (i === 0) {
                ctx.beginPath()
                ctx.moveTo(p.x, p.y)
            } else {
                ctx.lineTo(p.x, p.y)
                ctx.stroke()
            }
        }

        drawnCountRef.current = points.length
    }, [points])

    useEffect(() => {
        unsubRef.current?.()
        unsubRef.current = null

        stompRef.current?.deactivate?.()
        stompRef.current = null

        socketRef.current?.disconnect?.()
        socketRef.current = null

        if (tech === 'stomp') {
            const client = createStompClient(API_BASE)
            stompRef.current = client

            client.onConnect = () => {
                unsubRef.current = subscribeBlueprint(client, author, name, (upd) => {
                    appendPoints(upd?.points ?? [])
                })
            }

            client.activate()
        } else {
            const s = createSocket(IO_BASE)
            socketRef.current = s

            const room = `blueprints.${author}.${name}`

            s.on('connect', () => {
                s.emit('join-room', room)
            })

            s.on('blueprint-update', (upd) => {
                appendPoints(upd?.points ?? [])
            })

            s.on('draw-error', (err) => {
                console.warn('draw-error', err)
            })
        }

        return () => {
            unsubRef.current?.()
            unsubRef.current = null

            stompRef.current?.deactivate?.()
            socketRef.current?.disconnect?.()
        }
    }, [tech, author, name])

    function onClick(e) {
        const rect = e.target.getBoundingClientRect()
        const point = {
            x: Math.round(e.clientX - rect.left),
            y: Math.round(e.clientY - rect.top),
        }

        setPoints(prev => {
            const last = prev[prev.length - 1]
            if (samePoint(last, point)) return prev
            return [...prev, point]
        })

        if (tech === 'stomp' && stompRef.current?.connected) {
            stompRef.current.publish({
                destination: '/app/draw',
                body: JSON.stringify({ author, name, point }),
            })
        } else if (tech === 'socketio' && socketRef.current?.connected) {
            const room = `blueprints.${author}.${name}`
            socketRef.current.emit('draw-event', { room, author, name, point })
        }
    }

    return (
        <div style={{ fontFamily: 'Inter, system-ui', padding: 16, maxWidth: 900 }}>
            <h2>BluePrints RT – Socket.IO vs STOMP</h2>

            <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 8 }}>
                <label>Tecnología:</label>

                <select value={tech} onChange={e => setTech(e.target.value)}>
                    <option value="stomp">STOMP (Spring)</option>
                    <option value="socketio">Socket.IO (Node)</option>
                </select>

                <input value={author} onChange={e => setAuthor(e.target.value)} placeholder="autor" />
                <input value={name} onChange={e => setName(e.target.value)} placeholder="plano" />
            </div>

            <canvas
                ref={canvasRef}
                width={600}
                height={400}
                style={{ border: '1px solid #ddd', borderRadius: 12 }}
                onClick={onClick}
            />

            <p style={{ opacity: 0.7, marginTop: 8 }}>
                Tip: abre 2 pestañas y dibuja alternando para ver la colaboración.
            </p>

            <p style={{ opacity: 0.7, marginTop: 4 }}>
                Nota: el primer click no dibuja línea; la línea aparece a partir del segundo punto.
            </p>
        </div>
    )
}