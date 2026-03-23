import api from './index';

interface SignUpRequest {
    email: string;
    nickname: string;
    password: string;
}
interface LoginRequest {
    email: string;
    password: string;
}

export const signup = (data: SignUpRequest) => api.post('/users/signup', data);

export const login = (data: LoginRequest) => api.post('/users/login', data);
