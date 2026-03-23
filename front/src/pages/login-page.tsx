import useInput from '../hooks/useInput';
import { Button, Error, Form, Header, Input, Label, LinkContainer } from './sign-up-page.styles';
import { AxiosError } from 'axios';
import { useCallback, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { useMutation } from '@tanstack/react-query';
import { login } from '@/api/auth';
import { toast } from 'sonner';

interface JwtPayload {
    sub?: string;
    role?: string;
}

export default function LoginPage() {

    const [logInError, setLogInError] = useState(false);
    const [email, onChangeEmail] = useInput('');
    const [password, onChangePassword] = useInput('');

    const navigate = useNavigate();

    const { mutate } = useMutation({
        mutationFn: login,
        onSuccess: (response) => {
            console.log(response.data);
            const token = response.data.token;
            localStorage.setItem('token', token);
            const decoded = jwtDecode<JwtPayload>(token);
            localStorage.setItem('role', decoded.role ?? '');
            localStorage.setItem('email', decoded.sub ?? '');
            navigate('/workspace/sleact/channel/일반');
        },
        onError: (error) => {
            toast.error("이메일이나 비밀번호가 일치하지 않습니다!", {
                position: "top-center"
            })
            console.dir(error);
            const axiosError = error as AxiosError;
            setLogInError(axiosError.response?.status === 400 || axiosError.response?.status === 401);
        }
    });
    const onSubmit = useCallback(
        (e: React.FormEvent<HTMLFormElement>) => {
            e.preventDefault();
            setLogInError(false);
            mutate({ email, password })
        }, [email, password, mutate]);

    if (localStorage.getItem('token')) {
        // console.log('로그인됨');
        return <Navigate to="/workspace/sleact/channel/일반" />;
    }

    return (
        <div id="container" >
            <Header>Sleact </Header>
            <Form onSubmit={onSubmit} >
                <Label id="email-label" >
                    <span>이메일 주소 </span>
                    <div>
                        <Input type="email" id="email" name="email" value={email} onChange={onChangeEmail} />
                    </div>
                </Label>
                < Label id="password-label" >
                    <span>비밀번호 </span>
                    <div >
                        <Input type="password" id="password" name="password" value={password} onChange={onChangePassword} />
                    </div>
                    {logInError && <Error>이메일과 비밀번호 조합이 일치하지 않습니다.</Error>}
                </Label>
                <Button type="submit"> 로그인 </Button>
            </Form>
            <LinkContainer>
                아직 회원이 아니신가요 ? &nbsp;
                <a href="/signup" > 회원가입 하러가기 </a>
            </LinkContainer>
        </div>
    );
};

