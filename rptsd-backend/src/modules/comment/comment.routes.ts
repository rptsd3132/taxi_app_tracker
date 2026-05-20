import { Router } from 'express';
import { createComment, getMyComments } from './comment.controller';
import { authUser } from '../../middleware/authUser.middleware';

const router = Router();

router.use(authUser);

router.post('/', createComment);
router.get('/my', getMyComments);

export default router;
