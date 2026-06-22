/**
 * Parse axios/fetch errors into user-friendly messages for ErrorState.
 */
export function parseApiError(error, fallback = "An unexpected error occurred.") {
  if (!error) {
    return {
      type: "unknown",
      title: "Something went wrong",
      message: fallback,
    };
  }

  if (!error.response) {
    if (error.code === "ECONNABORTED") {
      return {
        type: "timeout",
        title: "Request timed out",
        message: "The server took too long to respond. Please try again.",
      };
    }
    if (error.message === "Network Error" || !navigator.onLine) {
      return {
        type: "network",
        title: "Connection problem",
        message: "Check your internet connection and try again.",
      };
    }
    return {
      type: "network",
      title: "Connection problem",
      message: error.message || "Unable to reach the server. Please try again.",
    };
  }

  const status = error.response.status;
  const data = error.response.data;
  const serverMessage =
    (typeof data === "string" ? data : null) ||
    data?.message ||
    data?.error ||
    error.message ||
    fallback;

  if (status === 400 || status === 422) {
    return {
      type: "validation",
      title: "Invalid request",
      message: serverMessage,
    };
  }
  if (status === 401) {
    return {
      type: "unauthorized",
      title: "Session expired",
      message: "Please sign in again to continue.",
    };
  }
  if (status === 403) {
    return {
      type: "forbidden",
      title: "Access denied",
      message: serverMessage || "You don't have permission to view this content.",
    };
  }
  if (status === 404) {
    return {
      type: "notFound",
      title: "Not found",
      message: serverMessage || "The requested resource could not be found.",
    };
  }
  if (status >= 500) {
    return {
      type: "server",
      title: "Server error",
      message: serverMessage || "Something went wrong on our end. Please try again later.",
    };
  }

  return {
    type: "unknown",
    title: "Something went wrong",
    message: serverMessage,
  };
}
