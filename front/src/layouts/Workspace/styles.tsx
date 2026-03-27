import styled from 'styled-components';

const SIDEBAR_BG = '#3f0e40';
const SIDEBAR_BORDER = 'rgb(82, 38, 83)';
const SIDEBAR_TEXT = 'rgb(188, 171, 188)';
const MAIN_BG = '#1a1d21';

export const WorkspaceWrapper = styled.div`
  display: flex;
  height: 100vh;
  overflow: hidden;
`;

export const Workspaces = styled.div`
  width: 68px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: ${SIDEBAR_BG};
  border-right: 1px solid ${SIDEBAR_BORDER};
  padding: 12px 0;
  gap: 6px;
  flex-shrink: 0;
`;

export const WorkspaceButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: white;
  border: none;
  font-size: 16px;
  font-weight: 700;
  color: #3f0e40;
  cursor: pointer;
  transition: border-radius 0.2s;

  &.active,
  &:hover {
    border-radius: 14px;
  }
`;

export const WorkspaceDivider = styled.hr`
  width: 32px;
  border: none;
  border-top: 1px solid ${SIDEBAR_BORDER};
  margin: 2px 0;
`;

export const NavIconButton = styled.button<{ $active?: boolean }>`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: ${({ $active }) => ($active ? 'rgba(255,255,255,0.15)' : 'transparent')};
  border: none;
  border-radius: 8px;
  color: ${({ $active }) => ($active ? 'white' : SIDEBAR_TEXT)};
  cursor: pointer;
  gap: 2px;
  font-size: 9px;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
`;

export const WorkspaceAddButton = styled.button`
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: transparent;
  border: 2px dashed rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  font-size: 20px;
  margin-top: auto;

  &:hover {
    border-color: rgba(255, 255, 255, 0.6);
    color: rgba(255, 255, 255, 0.9);
  }
`;

export const Channels = styled.nav`
  width: 260px;
  display: flex;
  flex-direction: column;
  background: ${SIDEBAR_BG};
  color: ${SIDEBAR_TEXT};
  border-right: 1px solid ${SIDEBAR_BORDER};
  flex-shrink: 0;
`;

export const WorkspaceName = styled.div`
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid ${SIDEBAR_BORDER};
  flex-shrink: 0;

  & > span {
    font-weight: 900;
    font-size: 16px;
    color: white;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
  }

  & > div {
    display: flex;
    gap: 2px;
    flex-shrink: 0;
  }
`;

export const WorkspaceIconBtn = styled.button`
  background: transparent;
  border: none;
  color: ${SIDEBAR_TEXT};
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  display: flex;
  align-items: center;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
`;

export const MenuScroll = styled.div`
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 2px;
  }
`;

export const SectionHeader = styled.div`
  display: flex;
  align-items: center;
  padding: 6px 8px 4px 12px;
  font-size: 13px;
  font-weight: 700;
  color: ${SIDEBAR_TEXT};
  cursor: pointer;
  user-select: none;
  gap: 4px;

  &:hover {
    color: white;
  }
`;

export const ChannelItem = styled.a<{ $active?: boolean }>`
  display: flex;
  align-items: center;
  padding: 0 12px 0 24px;
  height: 28px;
  color: ${({ $active }) => ($active ? 'white' : SIDEBAR_TEXT)};
  background: ${({ $active }) => ($active ? 'rgba(255,255,255,0.15)' : 'transparent')};
  text-decoration: none;
  font-size: 15px;
  cursor: pointer;
  border-radius: 4px;
  margin: 1px 8px;
  gap: 6px;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
`;

export const DmItem = styled.div`
  display: flex;
  align-items: center;
  padding: 0 12px 0 24px;
  height: 28px;
  color: ${SIDEBAR_TEXT};
  font-size: 15px;
  cursor: pointer;
  border-radius: 4px;
  margin: 1px 8px;
  gap: 8px;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
`;

export const DmAvatar = styled.div<{ $color: string }>`
  width: 18px;
  height: 18px;
  border-radius: 3px;
  background: ${({ $color }) => $color};
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
`;

export const StatusDot = styled.span<{ $online?: boolean }>`
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: ${({ $online }) => ($online ? '#44b37b' : 'transparent')};
  border: ${({ $online }) => ($online ? 'none' : '1.5px solid rgba(255,255,255,0.3)')};
  flex-shrink: 0;
`;

export const UserBar = styled.div`
  height: 52px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  border-top: 1px solid ${SIDEBAR_BORDER};
  cursor: pointer;
  gap: 8px;
  flex-shrink: 0;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }
`;

export const UserName = styled.span`
  font-size: 14px;
  font-weight: 700;
  color: white;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
`;

export const LogoutBtn = styled.button`
  background: transparent;
  border: none;
  color: ${SIDEBAR_TEXT};
  cursor: pointer;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }
`;

export const Chats = styled.div`
  flex: 1;
  background: ${MAIN_BG};
  display: flex;
  flex-direction: column;
  overflow: hidden;
`;
