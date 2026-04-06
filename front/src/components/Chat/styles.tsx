import styled from 'styled-components';

export const ChatWrapper = styled.div`
  display: flex;
  padding: 8px 20px;
  gap: 10px;

  &:hover {
    background: rgba(255, 255, 255, 0.03);
  }
`;

export const Content = styled.div`
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
`;

export const Meta = styled.div`
  display: flex;
  align-items: baseline;
  gap: 8px;
`;

export const Nickname = styled.span`
  font-weight: 700;
  font-size: 15px;
  color: white;
`;

export const Timestamp = styled.span`
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
`;

export const Text = styled.p`
  margin: 0;
  font-size: 15px;
  line-height: 22px;
  white-space: pre-wrap;
  word-break: break-word;
  color: rgb(209, 210, 211);
`;
