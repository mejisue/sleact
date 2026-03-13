import api from './index';

interface SignUpRequest {
    email: string;
    nickname: string;
    password: string;
}

export const signup = (data: SignUpRequest) => api.post('/users/signup', data);
