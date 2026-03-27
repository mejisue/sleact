import styled from 'styled-components';

export const ScrollArea = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 20px 0 8px;

  &::-webkit-scrollbar {
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
  }
`;

export const DateSeparator = styled.div`
  display: flex;
  align-items: center;
  margin: 16px 20px;
  gap: 12px;

  & > hr {
    flex: 1;
    border: none;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
  }
`;

export const DateLabel = styled.span`
  font-size: 12px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.4);
  white-space: nowrap;
  padding: 2px 10px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 20px;
`;
