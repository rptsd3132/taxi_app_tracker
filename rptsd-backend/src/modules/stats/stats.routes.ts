import { Router } from 'express';
import { syncStats, getMyStats } from './stats.controller';
import { authUser } from '../../middleware/authUser.middleware';

const router = Router();

router.use(authUser);

router.post('/sync', syncStats);
router.get('/my', getMyStats);

export default router;
