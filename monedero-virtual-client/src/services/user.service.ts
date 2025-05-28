import ApiService from './api.service';
import { User } from '../types/auth.types';

const UserService = {
  getRanking: (limit = 10) => ApiService.get<User[]>(`/api/users/ranking?limit=${limit}`),
  // ... otras funciones ...
};

export default UserService; 