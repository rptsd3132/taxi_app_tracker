import { Router } from 'express';
import {
  dashboard,
  listUsers,
  getUserDetail,
  suspendUser,
  extendSubscription,
  listPayments,
  listComments,
  replyToComment,
} from './admin.controller';
import { authAdmin } from '../../middleware/authAdmin.middleware';

const router = Router();

router.use(authAdmin);

router.get('/dashboard', dashboard);

router.get('/users', listUsers);
router.get('/users/:id', getUserDetail);
router.post('/users/:id/suspend', suspendUser);
router.post('/users/:id/extend-subscription', extendSubscription);

router.get('/payments', listPayments);

router.get('/comments', listComments);
router.post('/comments/:id/reply', replyToComment);

export default router;
