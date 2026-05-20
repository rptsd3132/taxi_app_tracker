import { Router } from 'express';
import authRouter from '../modules/auth/auth.routes';
import subscriptionRouter from '../modules/subscription/subscription.routes';
import statsRouter from '../modules/stats/stats.routes';
import commentRouter from '../modules/comment/comment.routes';
import adminRouter from '../modules/admin/admin.routes';

const router = Router();

router.use('/auth', authRouter);
router.use('/subscription', subscriptionRouter);
router.use('/stats', statsRouter);
router.use('/comments', commentRouter);
router.use('/admin', adminRouter);

export default router;
