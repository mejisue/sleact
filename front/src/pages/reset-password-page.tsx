import useInput from '../hooks/useInput';
import { Button, Error, Form, Header, Input, Label, LinkContainer, Success } from './sign-up-page.styles';
import { useCallback, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { confirmPasswordReset } from '@/api/auth';
import { AxiosError } from 'axios';

export default function ResetPasswordPage() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token') ?? '';

    const [password, onChangePassword] = useInput('');
    const [passwordConfirm, onChangePasswordConfirm] = useInput('');
    const [mismatchError, setMismatchError] = useState(false);
    const [isSuccess, setIsSuccess] = useState(false);
    const [isError, setIsError] = useState(false);

    const { mutate: resetPassword, isPending } = useMutation({
        mutationFn: () => confirmPasswordReset(token, password),
        onSuccess: () => {
            setIsSuccess(true);
            setIsError(false);
        },
        onError: (error) => {
            const axiosError = error as AxiosError;
            console.dir(axiosError);
            setIsError(true);
        },
    });

    const onSubmit = useCallback(
        (e: React.FormEvent<HTMLFormElement>) => {
            e.preventDefault();
            if (password !== passwordConfirm) {
                setMismatchError(true);
                return;
            }
            setMismatchError(false);
            setIsSuccess(false);
            setIsError(false);
            resetPassword();
        },
        [password, passwordConfirm, resetPassword],
    );

    if (!token) {
        return (
            <div id="container">
                <Header>Sleact</Header>
                <Error style={{ textAlign: 'center', marginTop: '40px' }}>
                    유효하지 않은 접근입니다. 이메일의 링크를 다시 확인해주세요.
                </Error>
                <LinkContainer style={{ textAlign: 'center' }}>
                    <a href="/login">로그인으로 돌아가기</a>
                </LinkContainer>
            </div>
        );
    }

    return (
        <div id="container">
            <Header>Sleact</Header>
            <Form onSubmit={onSubmit}>
                <Label id="password-label">
                    <span>새 비밀번호</span>
                    <div>
                        <Input
                            type="password"
                            id="password"
                            name="password"
                            value={password}
                            onChange={onChangePassword}
                            placeholder="새 비밀번호를 입력하세요"
                        />
                    </div>
                </Label>
                <Label id="password-confirm-label">
                    <span>새 비밀번호 확인</span>
                    <div>
                        <Input
                            type="password"
                            id="password-confirm"
                            name="password-confirm"
                            value={passwordConfirm}
                            onChange={onChangePasswordConfirm}
                            placeholder="새 비밀번호를 다시 입력하세요"
                        />
                    </div>
                    {mismatchError && <Error>비밀번호가 일치하지 않습니다.</Error>}
                </Label>
                {isSuccess && (
                    <Success>
                        비밀번호가 변경되었습니다.{' '}
                        <a href="/login" style={{ color: '#1264a3', fontWeight: 700 }}>
                            로그인하기
                        </a>
                    </Success>
                )}
                {isError && (
                    <Error>
                        비밀번호 재설정에 실패했습니다. 링크가 만료되었을 수 있습니다.
                    </Error>
                )}
                <Button type="submit" disabled={isPending || isSuccess}>
                    {isPending ? '변경 중...' : '비밀번호 변경'}
                </Button>
            </Form>
            <LinkContainer>
                <a href="/login">로그인으로 돌아가기</a>
            </LinkContainer>
        </div>
    );
}
