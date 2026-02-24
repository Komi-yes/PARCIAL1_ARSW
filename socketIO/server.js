import express from 'express';
import http from 'http';
import { Server } from 'socket.io';
import cors from 'cors';

const SPRING_BASE = process.env.SPRING_BASE ?? 'http://localhost:8080';
const PORT = process.env.PORT ?? 3001;

const app = express();
app.use(cors({ origin: '*' }));
app.use(express.json());

// ---------- PROXY REST -> SPRING ----------

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

app.get('/api/v1/blueprints', (req, res) =>
    proxyToSpring(req, res, '/api/v1/blueprints')
);

app.get('/api/v1/blueprints/:author', (req, res) =>
    proxyToSpring(req, res, `/api/v1/blueprints/${req.params.author}`)
);

app.get('/api/v1/blueprints/:author/:bpname', (req, res) =>
    proxyToSpring(req, res, `/api/v1/blueprints/${req.params.author}/${req.params.bpname}`)
);

app.post('/api/v1/blueprints', (req, res) =>
    proxyToSpring(req, res, '/api/v1/blueprints')
);

app.put('/api/v1/blueprints/:author/:bpname/points', (req, res) =>
    proxyToSpring(req, res, `/api/v1/blueprints/${req.params.author}/${req.params.bpname}/points`)
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
        const { room, point, author, name, fromSpring } = payload

        if (fromSpring) {
            io.to(room).emit('blueprint-update', { author, name, points: [point] })
            return
        }

        try {
            const url = `${SPRING_BASE}/api/v1/blueprints/${author}/${name}/points`
            const r = await fetch(url, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(point),
            })


            if (!r.ok) {
                const errTxt = await r.text()
                console.warn('Spring rejected draw-event:', r.status, errTxt)

                if (r.status === 404) {
                    try {
                        const createUrl = `${SPRING_BASE}/api/v1/blueprints`
                        const createPayload = { author, name, points: [point] }

                        const cr = await fetch(createUrl, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(createPayload),
                        })

                        if (cr.ok) {
                            const roomSafe = room ?? `blueprints.${author}.${name}`
                            io.to(roomSafe).emit('blueprint-update', { author, name, points: [point] })
                            console.log(`Blueprint creado automáticamente: ${author}/${name}`)
                            return
                        }

                        if (cr.status === 403) {
                            const retry = await fetch(url, {
                                method: 'PUT',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(point),
                            })

                            if (retry.ok) return

                            const retryErr = await retry.text()
                            socket.emit('draw-error', { status: retry.status, body: retryErr })
                            return
                        }

                        const createErr = await cr.text()
                        socket.emit('draw-error', { status: cr.status, body: createErr })
                        return

                    } catch (e) {
                        console.error('Error creating blueprint on 404:', e)
                        socket.emit('draw-error', { status: 502, body: 'Cannot create blueprint (Node -> Spring)' })
                        return
                    }
                }

                socket.emit('draw-error', { status: r.status, body: errTxt })
            }

        } catch (e) {
            console.error('Error forwarding draw-event to Spring:', e)
            socket.emit('draw-error', { status: 502, body: 'Cannot reach Spring' })
        }
    })
})



server.listen(PORT, () => console.log(`Socket.IO up on :${PORT}, proxying Spring at ${SPRING_BASE}`));