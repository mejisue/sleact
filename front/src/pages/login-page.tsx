import useInput from '../hooks/useInput';
import { Button, Error, Form, Header, Input, Label, LinkContainer } from './sign-up-page.styles';
import { AxiosError } from 'axios';
import React, { useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { login } from '@/api/auth';
import { getWorkspaces } from '@/api/workspace';
import { toast } from 'sonner';
import { useSetUser } from '@/store/auth';
import { useUser } from '@/store/auth';
import { Navigate } from 'react-router-dom'

export default function LoginPage() {
    const user = useUser();



    const [logInError, setLogInError] = useState(false);
    const [email, onChangeEmail] = useInput('');
    const [password, onChangePassword] = useInput('');

    const navigate = useNavigate();
    const setUser = useSetUser();

    const { mutate } = useMutation({
        mutationFn: login,
        onSuccess: async (response) => {
            const { id, email, role, nickname } = response.data;
            setUser({ id, email, role, nickname });
            const workspacesRes = await getWorkspaces();
            const first = workspacesRes.data[0];
            if (first) {
                navigate(`/workspace/${first.id}/channel/일반`);
            } else {
                navigate('/workspace');
            }
        },
        onError: (error) => {
            toast.error("이메일이나 비밀번호가 일치하지 않습니다!", {
                position: "top-center"
            })
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

    if (user) {
        return <Navigate to={user ? '/workspace/1/channel/일반' : '/login'} replace />;
    }

    return (
        <div id="container" >
            <Header>TEST for Sleact </Header>
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
            <LinkContainer>
                비밀번호를 잃어버리셨나요? &nbsp;
                <a href="/forgot-password">비밀번호 재설정하기</a>
            </LinkContainer>
        </div>
    );
};

