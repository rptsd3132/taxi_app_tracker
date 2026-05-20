import { Router } from 'express';
import { getStatus, createPayment, confirmPayment } from './subscription.controller';
import { authUser } from '../../middleware/authUser.middleware';

const router = Router();

router.use(authUser);

router.get('/status', getStatus);
router.post('/create-payment', createPayment);
router.post('/confirm-payment', confirmPayment);

export default router;
