# WebSocket Notifications

## Overview
Real-time notifications via STOMP over WebSocket. Critical events (SOS, unauthorized access, device offline) are broadcast to all connected clients.

## Endpoint
```
ws://host:port/ws
```
SockJS fallback available at same URL.

## Authentication
Pass JWT token in STOMP CONNECT frame header:
- Header: `Authorization: Bearer <JWT_TOKEN>`

Clients must authenticate via REST login first to obtain a valid JWT token.

## Subscribe
Subscribe to `/topic/alerts` to receive notifications:

```
SUBSCRIBE
destination:/topic/alerts
id:sub-1

^@
```

## Notification Message Format

```json
{
  "eventId": "uuid-string",
  "type": "SOS_ALERT",
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "message": "SOS alert triggered by device: AA:BB:CC:DD:EE:FF",
  "timestamp": "2024-01-15T10:30:00Z",
  "metadata": "{\"zoneId\":\"...\",\"extra\":\"data\"}"
}
```

## Notification Types

| Event Type | Notification Type | Description |
|------------|-------------------|-------------|
| `SOS_TRIGGERED` | `SOS_ALERT` | SOS button activated |
| `ACCESS_DENIED` | `UNAUTHORIZED_ACCESS` | Unauthorized zone access |
| `DEVICE_OFFLINE` | `DEVICE_OFFLINE` | Device went offline |
| `DEVICE_ONLINE` | `DEVICE_ONLINE` | Device came online |
| `ACCESS_GRANTED` | (none) | Normal access - no notification |

## Testing with stompy

### 1. Install stompy
```bash
npm install -g stompy
```

### 2. Get a JWT token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### 3. Connect and subscribe
```bash
stompy \
  --url ws://localhost:8080/ws \
  --header "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  --subscribe /topic/alerts
```

### 4. Trigger a notification
In another terminal, trigger an event:
```bash
curl -X POST http://localhost:8080/api/v1/events/device \
  -H "Content-Type: application/json" \
  -d '{"macAddress":"AA:BB:CC:DD:EE:FF","event":"SOS_TRIGGERED"}'
```

You should see the notification in stompy.

## Alternative: Browser Testing

Use https://webstomp.github.io/stomp-testing/ or the **STOMP Web Socket Client** Chrome extension.

Connection settings:
- URL: `ws://localhost:8080/ws`
- Headers:
  ```json
  {
    "Authorization": "Bearer <JWT_TOKEN>"
  }
  ```

Subscribe to `/topic/alerts`.

## Flutter Example

```dart
import 'package:stompdart/stomp.dart';

void connectWebSocket(String jwtToken) {
  var client = StompClient(
    url: 'ws://localhost:8080/ws',
    webSocketHeaders: {
      'Authorization': 'Bearer $jwtToken',
    },
  );

  client.connect();

  client.subscribe(
    destination: '/topic/alerts',
    callback: (message) {
      final data = jsonDecode(message.body);
      final type = data['type']; // SOS_ALERT, UNAUTHORIZED_ACCESS, etc.
      final macAddress = data['macAddress'];
      print('Notification: $type from $macAddress');
    },
  );
}
```

To trigger notifications:
- Send POST to `/api/v1/events/device` with:
  - `macAddress`: device MAC address
  - `event`: `SOS_TRIGGERED`, `ACCESS_DENIED`, or `DEVICE_OFFLINE`

## Production Notes

1. CORS is currently open (`setAllowedOriginPatterns("*")`). Restrict in production.
2. No reconnection logic included - add in Flutter client.
3. No heartbeat configured - add if needed for connection monitoring.