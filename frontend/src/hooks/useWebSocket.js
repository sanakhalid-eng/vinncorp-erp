import { useEffect, useRef, useCallback, useState } from "react";
import { wsService } from "../api/websocket";
import { useAuth } from "../context/useAuth";
export function useWebSocket(workspaceId) {
  const { token } = useAuth();
  const [isConnected, setIsConnected] = useState(false);
  const [activeUsers, setActiveUsers] = useState([]);
  const [typingUsers, setTypingUsers] = useState({});
  useEffect(() => {
    if (!token || !workspaceId) return;
    const handleConnected = () => setIsConnected(true);
    const handleDisconnected = () => {
      setIsConnected(false);
      setActiveUsers([]);
      setTypingUsers({});
    };
    const handlePresence = (event) => {
      if (event?.data?.activeUsers) {
        setActiveUsers(event.data.activeUsers);
      }
      if (event?.event === "user_connected" && event?.data) {
        setActiveUsers((prev) => {
          const exists = prev.find((u) => u.userId === event.data.userId);
          if (exists) return prev;
          return [...prev, event.data];
        });
      }
      if (event?.event === "user_disconnected" && event?.data?.userId) {
        setActiveUsers((prev) =>
          prev.filter((u) => u.userId !== event.data.userId),
        );
      }
    };
    const handleTyping = (event) => {
      if (event?.data) {
        const { userId, userName, entityType, entityId, isTyping } = event.data;
        const key = `${entityType}:${entityId}`;
        setTypingUsers((prev) => {
          const current = prev[key] || [];
          if (isTyping) {
            const exists = current.find((u) => u.userId === userId);
            if (exists) return prev;
            return { ...prev, [key]: [...current, { userId, userName }] };
          } else {
            return {
              ...prev,
              [key]: current.filter((u) => u.userId !== userId),
            };
          }
        });
      }
    };
    wsService.on("connected", handleConnected);
    wsService.on("disconnected", handleDisconnected);
    wsService.on("presence", handlePresence);
    wsService.on("presence_update", handlePresence);
    wsService.on("typing", handleTyping);
    wsService.connect(token, workspaceId).catch((err) => {
      console.error("[WS] Connection failed:", err);
    });
    return () => {
      wsService.off("connected", handleConnected);
      wsService.off("disconnected", handleDisconnected);
      wsService.off("presence", handlePresence);
      wsService.off("presence_update", handlePresence);
      wsService.off("typing", handleTyping);
    };
  }, [token, workspaceId]);
  const startTyping = useCallback(
    (entityType, entityId) => {
      wsService.startTyping(workspaceId, entityType, entityId);
    },
    [workspaceId],
  );
  const stopTyping = useCallback(
    (entityType, entityId) => {
      wsService.stopTyping(workspaceId, entityType, entityId);
    },
    [workspaceId],
  );
  const getTypingUsers = useCallback(
    (entityType, entityId) => {
      const key = `${entityType}:${entityId}`;
      return typingUsers[key] || [];
    },
    [typingUsers],
  );
  const disconnect = useCallback(() => {
    wsService.disconnect();
    setIsConnected(false);
    setActiveUsers([]);
    setTypingUsers({});
  }, []);
  return {
    isConnected,
    activeUsers,
    typingUsers,
    startTyping,
    stopTyping,
    getTypingUsers,
    disconnect,
  };
}
export default useWebSocket;

