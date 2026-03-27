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
