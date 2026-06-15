import { toast } from "sonner";

const notify = {
  success: (msg, opts) => toast.success(msg, opts),
  error: (msg, opts) => toast.error(msg, opts),
  info: (msg, opts) => toast.info(msg, opts),
  warning: (msg, opts) => toast.warning(msg, opts),
  loading: (msg, opts) => toast.loading(msg, opts),
  promise: (promise, msgs, opts) => toast.promise(promise, msgs, opts),
  dismiss: (id) => toast.dismiss(id),
};

export default notify;
