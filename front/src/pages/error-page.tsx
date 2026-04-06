import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

interface ErrorPageProps {
  status?: number;
  message?: string;
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: 12px;
  background: #1a1d21;
  color: #fff;
`;

const StatusCode = styled.h1`
  font-size: 72px;
  font-weight: 700;
  color: #4a154b;
  margin: 0;
`;

const Message = styled.p`
  font-size: 18px;
  color: #d1d2d3;
  margin: 0;
`;

const BackButton = styled.button`
  margin-top: 16px;
  padding: 10px 24px;
  background: #4a154b;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;

  &:hover {
    background: #611f69;
  }
`;

const ERROR_MESSAGES: Record<number, string> = {
  400: '잘못된 요청입니다.',
  401: '로그인이 필요합니다.',
  403: '접근 권한이 없습니다.',
  404: '페이지를 찾을 수 없습니다.',
};

export default function ErrorPage({ status = 400, message }: ErrorPageProps) {
  const navigate = useNavigate();
  return (
    <Wrapper>
      <StatusCode>{status}</StatusCode>
      <Message>{message ?? ERROR_MESSAGES[status] ?? '오류가 발생했습니다.'}</Message>
      <BackButton onClick={() => navigate(-1)}>이전 페이지로 돌아가기</BackButton>
    </Wrapper>
  );
}
