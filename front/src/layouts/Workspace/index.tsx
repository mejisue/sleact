import { useQuery } from '@tanstack/react-query';
import { useCallback } from 'react';
import { Link, Navigate, Outlet, useNavigate, useParams } from 'react-router-dom';
import { logout } from '@/api/auth';
import { getChannels } from '@/api/channel';
import { getWorkspaces } from '@/api/workspace';
import { useSetUser, useUser } from '@/store/auth';
import StompProvider from '@/providers/StompProvider';
import CreateChannelModal from '@/components/CreateChannelModal';
import CreateWorkspaceModal from '@/components/CreateWorkspaceModal';
import { useModalActions } from '@/store/modal';
import { ChevronDown, Hash, Home, MessageSquare, Plus, Settings, SquarePen } from 'lucide-react';
import {
  ChannelItem,
  Channels,
  Chats,
  DmAvatar,
  DmItem,
  LogoutBtn,
  MenuScroll,
  NavIconButton,
  SectionHeader,
  StatusDot,
  UserBar,
  UserName,
  WorkspaceAddButton,
  WorkspaceButton,
  WorkspaceDivider,
  WorkspaceIconBtn,
  WorkspaceName,
  Workspaces,
  WorkspaceWrapper,
} from './styles';

export default function Workspace() {
  const { workspaceId, channelName } = useParams<{ workspaceId: string; channelName: string }>();
  const navigate = useNavigate();
  const user = useUser();
  const setUser = useSetUser();

  const { data: workspaces } = useQuery({
    queryKey: ['workspaces'],
    queryFn: () => getWorkspaces().then((res) => res.data),
    enabled: !!user,
  });

  const { data: channels } = useQuery({
    queryKey: ['channels', workspaceId],
    queryFn: () => getChannels(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId && !!user,
  });

  const currentWorkspace = workspaces?.find((ws) => ws.id === Number(workspaceId));
  const { openWorkspaceModal, openChannelModal } = useModalActions();

  const onLogOut = useCallback(async () => {
    await logout();
    setUser(null);
    navigate('/login');
  }, [navigate, setUser]);

  if (!user) return <Navigate to="/login" />;

  return (
    <StompProvider>
      <WorkspaceWrapper>
        <Workspaces>
          {workspaces?.map((ws) => (
            <Link key={ws.id} to={`/workspace/${ws.id}/channel/일반`} style={{ textDecoration: 'none' }}>
              <WorkspaceButton className={ws.id === Number(workspaceId) ? 'active' : ''}>
                {ws.name.slice(0, 1).toUpperCase()}
              </WorkspaceButton>
            </Link>
          ))}
          <WorkspaceDivider />
          <NavIconButton $active>
            <Home size={18} />
            홈
          </NavIconButton>
          <NavIconButton>
            <MessageSquare size={18} />
            DM
          </NavIconButton>
          <WorkspaceAddButton onClick={openWorkspaceModal}>
            <Plus size={18} />
          </WorkspaceAddButton>
        </Workspaces>

        <Channels>
          <WorkspaceName>
            <span>{currentWorkspace?.name ?? ''}</span>
            <div>
              <WorkspaceIconBtn><Settings size={16} /></WorkspaceIconBtn>
              <WorkspaceIconBtn><SquarePen size={16} /></WorkspaceIconBtn>
            </div>
          </WorkspaceName>

          <MenuScroll>
            <SectionHeader>
              <ChevronDown size={14} />
              채널
              <WorkspaceIconBtn
                style={{ marginLeft: 'auto', padding: '2px' }}
                onClick={(e) => { e.stopPropagation(); openChannelModal(); }}
              >
                <Plus size={14} />
              </WorkspaceIconBtn>
            </SectionHeader>
            {channels?.map((channel) => (
              <ChannelItem
                key={channel.id}
                as={Link}
                to={`/workspace/${workspaceId}/channel/${channel.name}`}
                $active={channelName === channel.name}
              >
                <Hash size={15} />
                {channel.name}
              </ChannelItem>
            ))}

            <SectionHeader style={{ marginTop: 8 }}>
              <ChevronDown size={14} />
              다이렉트 메시지
            </SectionHeader>
            <DmItem>
              <DmAvatar $color="#e01e5a">{user.nickname.slice(0, 1).toUpperCase()}</DmAvatar>
              <StatusDot $online />
              {user.nickname} (나)
            </DmItem>
            <DmItem>
              <Plus size={14} style={{ opacity: 0.6 }} />
              팀원 추가
            </DmItem>
          </MenuScroll>

          <UserBar>
            <DmAvatar $color="#7c5cbf">{user.nickname.slice(0, 1).toUpperCase()}</DmAvatar>
            <UserName>{user.nickname}</UserName>
            <LogoutBtn onClick={onLogOut}>로그아웃</LogoutBtn>
          </UserBar>
        </Channels>

        <Chats>
          <Outlet />
        </Chats>
      </WorkspaceWrapper>
      <CreateWorkspaceModal />
      <CreateChannelModal />
    </StompProvider>
  );
}
