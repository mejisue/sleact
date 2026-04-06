import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Link, Navigate, Outlet, useNavigate, useParams } from 'react-router-dom';
import type { AxiosError } from 'axios';
import ErrorPage from '@/pages/error-page';
import { logout } from '@/api/auth';
import { getChannels } from '@/api/channel';
import { getWorkspaces, getWorkspaceMembers, getOnlineMembers } from '@/api/workspace';
import { useSetUser, useUser } from '@/store/auth';
import StompProvider, { useStompClient } from '@/providers/StompProvider';
import CreateChannelModal from '@/components/CreateChannelModal';
import CreateWorkspaceModal from '@/components/CreateWorkspaceModal';
import InviteMemberModal from '@/components/InviteMemberModal';
import { useModalActions } from '@/store/modal';
import { Building2, ChevronDown, Hash, Plus, UserPlus } from 'lucide-react';
import {
  AddButtonWrapper,
  ChannelItem,
  Channels,
  Chats,
  DmAvatar,
  DmItem,
  DropdownDivider,
  DropdownHeader,
  DropdownIconBox,
  DropdownItem,
  DropdownMenu,
  LogoutBtn,
  MenuScroll,
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

// 멤버 아바타 색상 (id 기반 순환)
const AVATAR_COLORS = ['#e01e5a', '#ecb22e', '#2eb67d', '#36c5f0', '#7c5cbf', '#1d9bd1', '#cc5de8'];
const getAvatarColor = (id: number) => AVATAR_COLORS[id % AVATAR_COLORS.length];

// useStompClient()는 StompProvider 내부에 있어야 하므로
// 실제 레이아웃 로직은 분리된 내부 컴포넌트에서 처리
function WorkspaceLayout() {
  const { workspaceId, channelName } = useParams<{ workspaceId: string; channelName: string }>();
  const navigate = useNavigate();
  const user = useUser();
  const setUser = useSetUser();
  const { client, isConnected } = useStompClient();
  const queryClient = useQueryClient();

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  // STOMP로 수신한 접속 상태 변경 델타 (email → true: 온라인, false: 오프라인)
  const [stompPresence, setStompPresence] = useState<Map<string, boolean>>(new Map());
  const dropdownRef = useRef<HTMLDivElement>(null);

  const { data: workspaces } = useQuery({
    queryKey: ['workspaces'],
    queryFn: () => getWorkspaces().then((res) => res.data),
    enabled: !!user,
  });

  const { data: channels, isError: isChannelsError, error: channelsError } = useQuery({
    queryKey: ['channels', workspaceId],
    queryFn: () => getChannels(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId && !!user,
    retry: false,
  });

  const { data: workspaceMembers = [] } = useQuery({
    queryKey: ['workspaceMembers', workspaceId],
    queryFn: () => getWorkspaceMembers(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId && !!user,
  });

  const { data: initialOnlineEmails } = useQuery({
    queryKey: ['onlineMembers', workspaceId],
    queryFn: () => getOnlineMembers(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId && !!user,
    refetchInterval: 10000, // STOMP 이벤트를 놓친 경우 10초마다 REST로 보정
  });

  // REST 초기값 + STOMP 델타를 합산한 온라인 이메일 Set
  const onlineEmails = useMemo(() => {
    const base = new Set(initialOnlineEmails ?? []);
    stompPresence.forEach((isOnline, email) => {
      if (isOnline) base.add(email);
      else base.delete(email);
    });
    return base;
  }, [initialOnlineEmails, stompPresence]);

  // STOMP 연결 완료 후 온라인 멤버 목록 강제 재조회
  // invalidateQueries → stale 표시만 하므로 refetchQueries로 즉시 재조회
  // stompPresence도 초기화해 이전 워크스페이스/연결의 stale 상태가 섞이는 것을 방지
  useEffect(() => {
    if (!isConnected || !workspaceId) return;
    setStompPresence(new Map());
    queryClient.refetchQueries({ queryKey: ['onlineMembers', workspaceId] });
  }, [isConnected, workspaceId, queryClient]);

  // STOMP 구독: 실시간 접속 상태 변경 반영
  useEffect(() => {
    if (!isConnected || !client || !workspaceId) return;

    const sub = client.subscribe(
      `/topic/workspace/${workspaceId}/presence`,
      (msg) => {
        const { email, status }: { email: string; status: string } = JSON.parse(msg.body);
        setStompPresence((prev) => {
          const next = new Map(prev);
          next.set(email, status === 'online');
          return next;
        });
      },
    );

    return () => sub.unsubscribe();
  }, [client, isConnected, workspaceId]);

  const currentWorkspace = workspaces?.find((ws) => ws.id === Number(workspaceId));
  const { openWorkspaceModal, openChannelModal, openInviteModal } = useModalActions();

  // 드롭다운 외부 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsDropdownOpen(false);
      }
    };
    if (isDropdownOpen) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isDropdownOpen]);

  const onLogOut = useCallback(async () => {
    await logout();
    setUser(null);
    navigate('/login');
  }, [navigate, setUser]);

  if (!user) return <Navigate to="/login" />;

  if (isChannelsError) {
    const status = (channelsError as AxiosError)?.response?.status ?? 400;
    return <ErrorPage status={status} />;
  }

  return (
    <>
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
          <AddButtonWrapper ref={dropdownRef}>
            <WorkspaceAddButton onClick={() => setIsDropdownOpen((prev) => !prev)}>
              <Plus size={18} />
            </WorkspaceAddButton>
            {isDropdownOpen && (
              <DropdownMenu>
                <DropdownHeader>새로 만들기</DropdownHeader>
                <DropdownItem
                  onClick={() => {
                    setIsDropdownOpen(false);
                    openWorkspaceModal();
                  }}
                >
                  <DropdownIconBox $bg="#4a154b">
                    <Building2 size={16} />
                  </DropdownIconBox>
                  워크스페이스 추가
                </DropdownItem>
                <DropdownDivider />
                <DropdownItem
                  onClick={() => {
                    setIsDropdownOpen(false);
                    openInviteModal();
                  }}
                >
                  <DropdownIconBox $bg="#2d6a4f">
                    <UserPlus size={16} />
                  </DropdownIconBox>
                  사람 초대
                </DropdownItem>
              </DropdownMenu>
            )}
          </AddButtonWrapper>
        </Workspaces>

        <Channels>
          <WorkspaceName>
            <span>{currentWorkspace?.name ?? ''}</span>
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

            {workspaceMembers.length > 0 && (
              <>
                {workspaceMembers
                  .filter((m) => m.id !== user.id)
                  .map((member) => (
                    <DmItem
                      key={member.id}
                      as={Link}
                      to={`/workspace/${workspaceId}/dm/${member.id}`}
                    >
                      <DmAvatar $color={getAvatarColor(member.id)}>
                        {member.nickname.slice(0, 1).toUpperCase()}
                      </DmAvatar>
                      <StatusDot $online={onlineEmails.has(member.email)} />
                      {member.nickname}
                    </DmItem>
                  ))}
              </>
            )}
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
      <InviteMemberModal workspaceName={currentWorkspace?.name ?? ''} />
    </>
  );
}

// StompProvider를 바깥에 두고 WorkspaceLayout이 그 안에서 useStompClient()를 호출
export default function Workspace() {
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const user = useUser();
  if (!user) return <Navigate to="/login" />;

  const workspaceIdNum = Number(workspaceId);
  if (!Number.isInteger(workspaceIdNum) || workspaceIdNum <= 0) {
    return <ErrorPage status={400} message="유효하지 않은 워크스페이스입니다." />;
  }

  return (
    <StompProvider workspaceId={workspaceId}>
      <WorkspaceLayout />
    </StompProvider>
  );
}
