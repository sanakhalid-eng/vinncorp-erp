import { useEffect, useRef, useState, useCallback } from "react";

export function useNotificationWebSocket(onNotification, userId) {
  const [isConnected, setIsConnected] = useState(false);
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  const effectiveUserId = userId || localStorage.getItem("userId");

  const connect = useCallback(() => {
    if (!effectiveUserId) return;

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const wsUrl = import.meta.env.DEV
      ? `ws://localhost:8081/ws/notifications?userId=${effectiveUserId}`
      : `${protocol}//${window.location.host}/ws/notifications?userId=${effectiveUserId}`;

    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      setIsConnected(true);
      reconnectAttempts.current = 0;
    };

    ws.onclose = () => {
      setIsConnected(false);
      if (reconnectAttempts.current < maxReconnectAttempts) {
        const delay = Math.min(
          1000 * Math.pow(2, reconnectAttempts.current),
          30000,
        );
        reconnectTimeoutRef.current = setTimeout(() => {
          reconnectAttempts.current += 1;
          connect();
        }, delay);
      }
    };

    ws.onerror = () => {
      ws.close();
    };

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        if (message.type === "notification" && message.data) {
          onNotification?.(message.data);
        }
      } catch {
        // ignore parse errors
      }
    };
  }, [onNotification, effectiveUserId]);

  useEffect(() => {
    if (wsRef.current) {
      wsRef.current.close();
    }
    connect();

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [connect]);

  return { isConnected };
}
