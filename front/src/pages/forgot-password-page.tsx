import useInput from '../hooks/useInput';
import { Button, Error, Form, Header, Input, Label, LinkContainer, Success } from './sign-up-page.styles';
import { useCallback, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { requestPasswordReset } from '@/api/auth';
import { AxiosError } from 'axios';

export default function ForgotPasswordPage() {
    const [email, onChangeEmail] = useInput('');
    const [isSuccess, setIsSuccess] = useState(false);
    const [isError, setIsError] = useState(false);

    const { mutate, isPending } = useMutation({
        mutationFn: () => requestPasswordReset(email),
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
            setIsSuccess(false);
            setIsError(false);
            mutate();
        },
        [mutate],
    );

    return (
        <div id="container">
            <Header>Sleact</Header>
            <Form onSubmit={onSubmit}>
                <Label id="email-label">
                    <span>이메일 주소</span>
                    <div>
                        <Input
                            type="email"
                            id="email"
                            name="email"
                            value={email}
                            onChange={onChangeEmail}
                            placeholder="가입한 이메일을 입력하세요"
                        />
                    </div>
                </Label>
                {isSuccess && (
                    <Success>비밀번호 재설정 링크를 이메일로 발송했습니다.</Success>
                )}
                {isError && (
                    <Error>이메일 발송에 실패했습니다. 가입된 이메일인지 확인해주세요.</Error>
                )}
                <Button type="submit" disabled={isPending}>
                    {isPending ? '발송 중...' : '재설정 링크 보내기'}
                </Button>
            </Form>
            <LinkContainer>
                <a href="/login">로그인으로 돌아가기</a>
            </LinkContainer>
        </div>
    );
}
