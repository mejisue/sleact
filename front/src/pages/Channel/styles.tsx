import styled from 'styled-components';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #1a1d21;
  color: rgb(209, 210, 211);
`;

export const Header = styled.header`
  height: 56px;
  display: flex;
  width: 100%;
  align-items: center;
  padding: 0 16px 0 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  font-weight: 700;
  font-size: 15px;
  color: white;
  flex-shrink: 0;
  gap: 6px;
`;

export const InviteButton = styled.button`
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  padding: 6px 14px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s, border-color 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.4);
  }
`;
