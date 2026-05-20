// Augments Express.Request so req.user and req.admin are available in all handlers.
declare namespace Express {
  interface Request {
    user?: { id: string; email: string };
    admin?: { id: string; email: string; role: string };
  }
}
