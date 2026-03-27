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

export const requestPasswordReset = (email: string) =>
    api.post('/users/password-reset/request', { email });

export const confirmPasswordReset = (token: string, password: string) =>
    api.post('/users/password-reset/confirm', { token, password });
