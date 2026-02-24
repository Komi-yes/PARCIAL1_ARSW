import express from 'express';
import http from 'http';
import { Server } from 'socket.io';
import cors from 'cors';

const SPRING_BASE = process.env.SPRING_BASE ?? 'http://localhost:8080';
const PORT = process.env.PORT ?? 3001;

const app = express();
app.use(cors({ origin: '*' }));
app.use(express.json());

async function proxyToSpring(req, res, path) {
    try {
        const url = `${SPRING_BASE}${path}`;
        const init = {
            method: req.method,
            headers: { 'Content-Type': 'application/json' },
        };
        if (req.method !== 'GET' && req.method !== 'HEAD') {
            init.body = JSON.stringify(req.body ?? {});
        }

        const r = await fetch(url, init);
        const text = await r.text();

        res.status(r.status);
        if (!text) return res.end();
        try { return res.json(JSON.parse(text)); }
        catch { return res.send(text); }

    } catch (e) {
        console.error('Proxy error:', e);
        return res.status(502).json({ code: 502, message: 'Bad gateway (Node -> Spring)', data: null });
    }
}

app.get('/api/v1/tickets', (req, res) =>
    proxyToSpring(req, res, '/api/v1/tickets')
);

app.get('/api/v1/tickets/:id', (req, res) =>
    proxyToSpring(req, res, `/api/v1/tickets/${req.params.id}`)
);

app.get('/api/v1/tickets/called', (req, res) =>
    proxyToSpring(req, res, '/api/v1/tickets/called')
);

app.post('/api/v1/tickets/create', (req, res) =>
    proxyToSpring(req, res, '/api/v1/tickets/create')
);

app.put('/api/v1/tickets/call', (req, res) =>
    proxyToSpring(req, res, `/api/v1/tickets/call`)
);

const server = http.createServer(app);
const io = new Server(server, { cors: { origin: '*' } });


io.on('connection', (socket) => {
    console.log('socket connected', socket.id)

    socket.on('join-room', (room) => {
        console.log('join-room', socket.id, room)
        socket.join(room)
    })

    socket.on('draw-event', async (payload) => {
        console.log('draw-event', payload)
        const { room } = payload

        try {
            const url = `${SPRING_BASE}/api/v1/tickets/create`
            const r = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
            })

            if (!r.ok) {
                const errTxt = await r.text()
                console.warn('Spring rejected draw-event:', r.status, errTxt)
                socket.emit('draw-error', { status: r.status, body: errTxt })
            } else {
                io.to(room).emit('ticket-update', { message: 'New ticket created' })
            }

        } catch (e) {
            console.error('Error forwarding draw-event to Spring:', e)
            socket.emit('draw-error', { status: 502, body: 'Cannot reach Spring' })
        }
    })
})



server.listen(PORT, () => console.log(`Socket.IO up on :${PORT}, proxying Spring at ${SPRING_BASE}`));