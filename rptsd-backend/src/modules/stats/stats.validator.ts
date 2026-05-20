import { z } from 'zod';

export const syncStatsSchema = z.object({
  date: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be YYYY-MM-DD'),
  ridesAccepted: z.number().int().min(0),
  ridesSkipped: z.number().int().min(0),
  totalEarnings: z.number().min(0),
});
