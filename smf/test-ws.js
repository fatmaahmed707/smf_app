const Stomp = require("@stomp/stompjs");
const axios = require("axios");

const BASE_URL = "http://localhost:8080";

async function main() {
	console.log("1. Logging in...");

	const loginRes = await axios.post(`${BASE_URL}/api/v1/auth/login`, {
		email: "admin@smf.com",
		password: "password",
	});

	const token = loginRes.data.data.accessToken;

	if (!token) {
		console.error("Login failed:", loginRes.data);
		process.exit(1);
	}

	console.log("Login successful, token:", token.substring(0, 20) + "...");

	console.log("2. Connecting to WebSocket...");

	const stompClient = new Stomp.Client({
		brokerURL: "ws://localhost:8080/ws",
		webSocketHeaders: {
			Authorization: `Bearer ${token}`,
		},
		reconnectDelay: 5000,
		heartbeat: { incoming: 10000, outgoing: 10000 },
	});

	stompClient.onConnect = () => {
		console.log("Connected!");

		const subscription = stompClient.subscribe("/topic/alerts", (message) => {
			console.log("\n=== NOTIFICATION RECEIVED ===");
			console.log(message.body);
			console.log("========================\n");
		});

		console.log("Subscribed to /topic/alerts! Waiting for events...\n");

		setTimeout(async () => {
			console.log("3. Triggering SOS event...");

			await axios.post(
				`${BASE_URL}/api/v1/events/device`,
				{
					macAddress: "AA:BB:CC:DD:EE:FF",
					event: "SOS_TRIGGERED",
				},
				{
					headers: { "X-Device-Mac": "AA:BB:CC:DD:EE:FF" },
				},
			);

			console.log("Event sent!");

			setTimeout(() => {
				stompClient.deactivate();
				process.exit(0);
			}, 2000);
		}, 2000);
	};

	stompClient.onStompError = (frame) => {
		console.error("STOMP error:", frame.headers.message);
	};

	stompClient.onWebSocketError = (error) => {
		console.error("WebSocket error:", error.message);
	};

	stompClient.onDisconnect = () => {
		console.log("Disconnected");
	};

	console.log("Activating STOMP client...");
	stompClient.activate();
}

main();

