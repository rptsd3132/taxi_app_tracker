import { Router } from 'express';
import { register, login, logout, me, adminLogin } from './auth.controller';
import { authUser } from '../../middleware/authUser.middleware';

const router = Router();

router.post('/register', register);
router.post('/login', login);
router.post('/logout', authUser, logout);
router.get('/me', authUser, me);
router.post('/admin/login', adminLogin);

export default router;
