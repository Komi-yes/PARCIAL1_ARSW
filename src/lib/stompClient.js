import { Client } from '@stomp/stompjs'
// import SockJS from 'sockjs-client' // si quieres fallback

export function createStompClient(baseUrl) {
  const client = new Client({
    brokerURL: `${baseUrl.replace(/\/$/,'')}/ws-tickets`,
    // webSocketFactory: () => new SockJS(`${baseUrl}/ws-tickets`),
    reconnectDelay: 1000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onStompError: (f) => console.error('STOMP error', f.headers['message']),
  })
  return client
}

export function subscribeToTickets(client, onMsg) {
  return client.subscribe('/topic/tickets', (m) => {
    onMsg(JSON.parse(m.body))
  })
}
