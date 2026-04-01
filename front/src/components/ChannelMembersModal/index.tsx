import { Search, X } from 'lucide-react';
import { useState } from 'react';
import styled from 'styled-components';
import type { IUser } from '@/types';

const AVATAR_COLORS = ['#e01e5a', '#ecb22e', '#2eb67d', '#36c5f0', '#7c5cbf', '#1d9bd1', '#cc5de8'];
const getAvatarColor = (id: number) => AVATAR_COLORS[id % AVATAR_COLORS.length];

const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
`;

const Card = styled.div`
  background: #222529;
  border-radius: 8px;
  width: 480px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  color: rgb(209, 210, 211);
  position: relative;
`;

const ModalHeader = styled.div`
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
`;

const Title = styled.h2`
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: white;
`;

const CloseButton = styled.button`
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  display: flex;

  &:hover {
    color: white;
    background: rgba(255, 255, 255, 0.1);
  }
`;

const SearchWrapper = styled.div`
  padding: 12px 16px;
  flex-shrink: 0;
`;

const SearchInputRow = styled.div`
  display: flex;
  align-items: center;
  background: #1a1d21;
  border: 2px solid #1d9bd1;
  border-radius: 6px;
  padding: 0 12px;
  gap: 8px;
`;

const SearchInput = styled.input`
  flex: 1;
  padding: 10px 0;
  background: transparent;
  border: none;
  color: white;
  font-size: 15px;
  outline: none;

  &::placeholder {
    color: rgba(255, 255, 255, 0.35);
  }
`;

const MemberList = styled.div`
  overflow-y: auto;
  padding: 4px 8px 12px;
`;

const MemberRow = styled.div`
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    background: rgba(255, 255, 255, 0.07);
  }
`;

const Avatar = styled.div<{ $color: string }>`
  width: 36px;
  height: 36px;
  border-radius: 6px;
  background: ${(p) => p.$color};
  font-size: 14px;
  font-weight: 700;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
`;

const MemberName = styled.span`
  font-size: 15px;
  color: rgb(209, 210, 211);
`;

const EmptyText = styled.p`
  text-align: center;
  color: rgba(255, 255, 255, 0.35);
  font-size: 14px;
  padding: 24px 0;
`;

interface Props {
  channelName: string;
  members: IUser[];
  onClose: () => void;
}

export default function ChannelMembersModal({ channelName, members, onClose }: Props) {
  const [search, setSearch] = useState('');

  const filtered = members.filter((m) =>
    m.nickname.toLowerCase().includes(search.toLowerCase()),
  );

  return (
    <Overlay onClick={onClose}>
      <Card onClick={(e) => e.stopPropagation()}>
        <ModalHeader>
          <Title>#{channelName} · 멤버 {members.length}명</Title>
          <CloseButton onClick={onClose}>
            <X size={18} />
          </CloseButton>
        </ModalHeader>

        <SearchWrapper>
          <SearchInputRow>
            <Search size={16} color="rgba(255,255,255,0.4)" />
            <SearchInput
              autoFocus
              placeholder="멤버 찾기"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </SearchInputRow>
        </SearchWrapper>

        <MemberList>
          {filtered.length === 0 ? (
            <EmptyText>검색 결과가 없습니다.</EmptyText>
          ) : (
            filtered.map((member) => (
              <MemberRow key={member.id}>
                <Avatar $color={getAvatarColor(member.id)}>
                  {member.nickname.slice(0, 1).toUpperCase()}
                </Avatar>
                <MemberName>{member.nickname}</MemberName>
              </MemberRow>
            ))
          )}
        </MemberList>
      </Card>
    </Overlay>
  );
}
