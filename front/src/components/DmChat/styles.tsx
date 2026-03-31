import styled from 'styled-components';

export const DmWrapper = styled.div<{ $isMine: boolean }>`
  display: flex;
  flex-direction: ${({ $isMine }) => ($isMine ? 'row-reverse' : 'row')};
  padding: 8px 20px;
  gap: 10px;

  &:hover {
    background: rgba(255, 255, 255, 0.03);
  }
`;

export const Content = styled.div<{ $isMine: boolean }>`
  display: flex;
  flex-direction: column;
  align-items: ${({ $isMine }) => ($isMine ? 'flex-end' : 'flex-start')};
  gap: 2px;
  min-width: 0;
`;

export const Meta = styled.div<{ $isMine: boolean }>`
  display: flex;
  flex-direction: ${({ $isMine }) => ($isMine ? 'row-reverse' : 'row')};
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

export const Bubble = styled.p<{ $isMine: boolean }>`
  margin: 0;
  padding: 8px 12px;
  border-radius: ${({ $isMine }) => ($isMine ? '18px 4px 18px 18px' : '4px 18px 18px 18px')};
  background: ${({ $isMine }) => ($isMine ? '#1264a3' : 'rgba(255, 255, 255, 0.08)')};
  font-size: 15px;
  line-height: 22px;
  white-space: pre-wrap;
  word-break: break-word;
  color: rgb(209, 210, 211);
  max-width: 480px;
`;
