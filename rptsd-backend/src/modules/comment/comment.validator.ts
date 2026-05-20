import { z } from 'zod';

export const createCommentSchema = z.object({
  message: z.string().min(1, 'Message is required').max(2000, 'Message too long'),
});
