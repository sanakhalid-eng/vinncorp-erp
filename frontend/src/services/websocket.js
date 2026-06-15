import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client/dist/sockjs.min.js";
class WebSocketService {
  constructor() {
    this.client = null;
    this.isConnected = false;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 10;
    this.reconnectDelay = 1000;
    this.heartbeatInterval = null;
    this.listeners = {};
    this.presenceHandlers = {};
    this.typingHandlers = {};
  }
  connect(token, workspaceId) {
    return new Promise((resolve, reject) => {
      if (this.client && this.client.connected) {
        resolve();
        return;
      }
      this.client = new Client({
        webSocketFactory: () => new SockJS(`${window.location.origin}/ws`),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
          "X-Workspace-Id": workspaceId?.toString() || "",
        },
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        reconnectDelay: this.reconnectDelay,
        debug: (str) => {
          if (import.meta.env.DEV) {
            console.log("[WS Debug]", str);
          }
        },
        onConnect: (frame) => {
          this.isConnected = true;
          this.reconnectAttempts = 0;
          console.log("[WS] Connected");
          if (workspaceId) {
            this.subscribeToWorkspace(workspaceId);
            this.sendPresenceUpdate(workspaceId);
          }
          this.startHeartbeat();
          this.emit("connected", frame);
          resolve();
        },
        onDisconnect: (frame) => {
          this.isConnected = false;
          console.log("[WS] Disconnected");
          this.stopHeartbeat();
          this.emit("disconnected", frame);
        },
        onStompError: (frame) => {
          console.error("[WS] STOMP Error:", frame);
          this.emit("error", frame);
          reject(frame);
        },
        onWebSocketError: (error) => {
          console.error("[WS] WebSocket Error:", error);
          this.emit("error", error);
        },
        onWebSocketClose: (event) => {
          console.log("[WS] WebSocket Close:", event.code, event.reason);
          this.isConnected = false;
          this.stopHeartbeat();
          this.emit("closed", event);
        },
      });
      this.client.activate();
    });
  }
  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.isConnected = false;
    this.stopHeartbeat();
    this.listeners = {};
    this.presenceHandlers = {};
    this.typingHandlers = {};
  }
  subscribeToWorkspace(workspaceId) {
    if (!this.client || !this.client.connected) {
      console.error("[WS] Cannot subscribe: not connected");
      return;
    }
    const workspaceTopic = `/topic/workspace/${workspaceId}`;
    this.client.subscribe(workspaceTopic, (message) => {
      const event = JSON.parse(message.body);
      this.handleWorkspaceEvent(event);
    });
    this.client.subscribe(`/user/queue/notifications`, (message) => {
      const event = JSON.parse(message.body);
      this.emit("notification", event);
    });
    this.client.subscribe(`/user/queue/presence`, (message) => {
      const event = JSON.parse(message.body);
      this.emit("presence", event);
    });
    console.log(`[WS] Subscribed to ${workspaceTopic}`);
  }
  subscribeToEntity(workspaceId, entityType, entityId) {
    if (!this.client || !this.client.connected) return;
    const topic = `/topic/workspace/${workspaceId}/${entityType}/${entityId}`;
    this.client.subscribe(topic, (message) => {
      const event = JSON.parse(message.body);
      this.handleEntityEvent(event, entityType, entityId);
    });
    console.log(`[WS] Subscribed to ${topic}`);
  }
  sendPresenceUpdate(workspaceId) {
    if (!this.client || !this.client.connected) return;
    this.client.publish({
      destination: "/app/presence.update",
      body: JSON.stringify({ workspaceId }),
    });
  }
  startTyping(workspaceId, entityType, entityId) {
    if (!this.client || !this.client.connected) return;
    this.client.publish({
      destination: "/app/typing.start",
      body: JSON.stringify({ workspaceId, entityType, entityId }),
    });
  }
  stopTyping(workspaceId, entityType, entityId) {
    if (!this.client || !this.client.connected) return;
    this.client.publish({
      destination: "/app/typing.stop",
      body: JSON.stringify({ workspaceId, entityType, entityId }),
    });
  }
  subscribeToWorkspace(workspaceId) {
    if (!this.client || !this.client.connected) return;
    this.client.publish({
      destination: "/app/workspace.subscribe",
      body: JSON.stringify({ workspaceId }),
    });
  }
  startHeartbeat() {
    this.stopHeartbeat();
    this.heartbeatInterval = setInterval(() => {
      if (this.client && this.client.connected) {
        this.client.publish({
          destination: "/app/presence.update",
          body: JSON.stringify({ heartbeat: true, timestamp: Date.now() }),
        });
      }
    }, 30000);
  }
  stopHeartbeat() {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }
  handleWorkspaceEvent(event) {
    const { type, event: eventName, workspaceId } = event;
    this.emit("workspace_event", event);
    if (type === "presence") {
      this.emit("presence", event);
      if (eventName === "user_connected" || eventName === "user_disconnected") {
        this.emit("presence_update", event);
      }
    } else if (type === "typing") {
      this.emit("typing", event);
    } else if (type === "entity_update") {
      this.emit(`${event.entityType}_update`, event);
    } else if (type === "notification") {
      this.emit("notification", event);
    } else if (type === "activity") {
      this.emit("activity", event);
    }
  }
  handleEntityEvent(event, entityType, entityId) {
    this.emit(`${entityType}_update`, event);
    this.emit(`${entityType}_${entityId}_update`, event);
  }
  on(event, handler) {
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(handler);
  }
  off(event, handler) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(
        (h) => h !== handler,
      );
    }
  }
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach((handler) => {
        try {
          handler(data);
        } catch (error) {
          console.error(`[WS] Error in listener for ${event}:`, error);
        }
      });
    }
  }
}
export const wsService = new WebSocketService();
export default wsService;
