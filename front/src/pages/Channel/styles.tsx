import styled from 'styled-components';

export const Container = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #1a1d21;
  color: rgb(209, 210, 211);
`;

const AVATAR_COLORS = ['#e01e5a', '#ecb22e', '#2eb67d', '#36c5f0', '#7c5cbf', '#1d9bd1', '#cc5de8'];
export const getAvatarColor = (id: number) => AVATAR_COLORS[id % AVATAR_COLORS.length];

export const MemberAvatarButton = styled.button`
  display: flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 4px 10px 4px 6px;
  cursor: pointer;
  color: white;
  font-size: 13px;
  font-weight: 600;
  position: relative;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.35);
  }

  &:hover > div[data-tooltip] {
    display: block;
  }
`;

export const AvatarOverlap = styled.div`
  display: flex;
  align-items: center;
`;

export const MiniAvatar = styled.div<{ $color: string; $index: number }>`
  width: 22px;
  height: 22px;
  border-radius: 4px;
  background: ${(p) => p.$color};
  font-size: 10px;
  font-weight: 700;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: ${(p) => (p.$index === 0 ? '0' : '-5px')};
  border: 2px solid #1a1d21;
  position: relative;
  z-index: ${(p) => 10 - p.$index};
`;

export const Tooltip = styled.div`
  display: none;
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  background: #1a1d21;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 6px;
  padding: 10px 14px;
  color: white;
  font-size: 13px;
  font-weight: 400;
  z-index: 50;
  min-width: 200px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.5);
  line-height: 1.5;
  white-space: normal;
  text-align: left;
  cursor: default;
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
