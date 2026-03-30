import { inviteMember } from '@/api/workspace';
import { getChannels } from '@/api/channel';
import { useInviteModalOpen, useModalActions } from '@/store/modal';
import { useQuery } from '@tanstack/react-query';
import { Hash, X } from 'lucide-react';
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { toast } from 'sonner';
import styled from 'styled-components';

function getKoreanParticle(name: string) {
  if (!name) return '으로';
  const code = name.charCodeAt(name.length - 1);
  if (code < 0xac00 || code > 0xd7a3) return '으로';
  return (code - 0xac00) % 28 === 0 ? '로' : '으로';
}

// ─── Styled Components ──────────────────────────────────────────────

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
  width: 520px;
  padding: 28px;
  color: rgb(209, 210, 211);
  position: relative;
`;

const Title = styled.h2`
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 700;
  color: white;
  padding-right: 32px;
`;

const CloseButton = styled.button`
  position: absolute;
  top: 16px;
  right: 16px;
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

const Label = styled.label`
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: rgb(209, 210, 211);
  margin-bottom: 8px;
`;

const EmailInput = styled.input`
  width: 100%;
  padding: 12px 14px;
  background: #1a1d21;
  border: 2px solid #1d9bd1;
  border-radius: 4px;
  color: white;
  font-size: 15px;
  outline: none;
  box-sizing: border-box;

  &::placeholder {
    color: rgba(255, 255, 255, 0.35);
  }
`;

const Divider = styled.hr`
  border: none;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin: 20px 0;
`;

const SectionTitle = styled.p`
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 700;
  color: white;
`;

const SectionDesc = styled.p`
  margin: 0 0 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
`;

const HintLabel = styled.p`
  margin: 0 0 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
`;

const ChipRow = styled.div`
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 10px;
`;

const ChannelChip = styled.button<{ $selected: boolean }>`
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 16px;
  border: 1px solid ${({ $selected }) => ($selected ? '#1d9bd1' : 'rgba(255,255,255,0.2)')};
  background: ${({ $selected }) => ($selected ? 'rgba(29,155,209,0.15)' : 'transparent')};
  color: ${({ $selected }) => ($selected ? '#1d9bd1' : 'rgba(255,255,255,0.6)')};
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s;

  &:hover {
    border-color: #1d9bd1;
    color: #1d9bd1;
  }
`;

const ChannelSearchInput = styled.input<{ $invalid?: boolean }>`
  width: 100%;
  padding: 8px 12px;
  background: #1a1d21;
  border: 1px solid ${({ $invalid }) => ($invalid ? '#e01e5a' : 'rgba(255,255,255,0.2)')};
  border-radius: 4px;
  color: white;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;

  &::placeholder {
    color: rgba(255, 255, 255, 0.3);
  }

  &:focus {
    border-color: ${({ $invalid }) => ($invalid ? '#e01e5a' : 'rgba(255,255,255,0.4)')};
  }
`;

const ChannelErrorMsg = styled.p`
  margin: 6px 0 0;
  font-size: 12px;
  color: #e01e5a;
`;

const SearchResultList = styled.div`
  margin-top: 6px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  overflow: hidden;
`;

const SearchResultItem = styled.button<{ $selected: boolean }>`
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: ${({ $selected }) => ($selected ? 'rgba(29,155,209,0.12)' : 'transparent')};
  border: none;
  color: ${({ $selected }) => ($selected ? '#1d9bd1' : 'rgba(255,255,255,0.8)')};
  font-size: 14px;
  cursor: pointer;
  text-align: left;

  &:hover {
    background: rgba(255, 255, 255, 0.07);
  }

  & + & {
    border-top: 1px solid rgba(255, 255, 255, 0.06);
  }
`;

const Footer = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
`;


const SendButton = styled.button`
  padding: 10px 20px;
  background: #007a5a;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background: #148567;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

// ─── Component ──────────────────────────────────────────────────────

interface Props {
  workspaceName: string;
}

export default function InviteMemberModal({ workspaceName }: Props) {
  const isOpen = useInviteModalOpen();
  const { closeInviteModal } = useModalActions();
  const { workspaceId } = useParams<{ workspaceId: string }>();

  const [email, setEmail] = useState('');
  const [channelSearch, setChannelSearch] = useState('');
  const [selectedChannels, setSelectedChannels] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const { data: channels = [] } = useQuery({
    queryKey: ['channels', workspaceId],
    queryFn: () => getChannels(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId && isOpen,
  });

  // 일반채널은 항상 추가되므로 선택 목록에서 제외
  const selectableChannels = channels.filter((c) => c.name !== '일반');
  const suggestedChannels = selectableChannels.slice(0, 3);
  const searchedChannels = channelSearch
    ? selectableChannels.filter((c) => c.name.includes(channelSearch))
    : [];
  const isChannelSearchInvalid = channelSearch.trim() !== '' && searchedChannels.length === 0;

  const toggleChannel = (name: string) => {
    setSelectedChannels((prev) =>
      prev.includes(name) ? prev.filter((c) => c !== name) : [...prev, name],
    );
  };

  const handleClose = () => {
    setEmail('');
    setChannelSearch('');
    setSelectedChannels([]);
    closeInviteModal();
  };

  const onSubmit: React.FormEventHandler = async (e) => {
    e.preventDefault();
    if (!email.trim() || !workspaceId) return;
    setIsLoading(true);
    try {
      await inviteMember(Number(workspaceId), {
        email: email.trim(),
        channelNames: selectedChannels,
      });
      const channelMsg =
        selectedChannels.length > 0
          ? ` (#일반, ${selectedChannels.map((c) => `#${c}`).join(', ')})`
          : ' (#일반)';
      toast.success(`${email.trim()} 님을 워크스페이스에 초대했습니다.${channelMsg}`);
      handleClose();
    } catch (err: any) {
      const msg: string = err?.response?.data?.message ?? '';
      if (msg.includes('존재하지 않습니다') || err?.response?.status === 404) {
        toast.error('해당 이메일로 가입된 사용자가 없습니다.');
      } else if (msg.includes('이미 회원')) {
        toast.error('이미 워크스페이스 멤버입니다.');
      } else {
        toast.error('초대에 실패했습니다. 다시 시도해주세요.');
      }
    } finally {
      setIsLoading(false);
    }
  };



  if (!isOpen) return null;

  return (
    <Overlay onClick={handleClose}>
      <Card onClick={(e) => e.stopPropagation()}>
        <CloseButton onClick={handleClose}>
          <X size={18} />
        </CloseButton>

        <Title>
          {workspaceName}
          {getKoreanParticle(workspaceName)} 사용자 초대
        </Title>

        <form onSubmit={onSubmit}>
          <Label htmlFor="invite-email">초대 받을 사람:</Label>
          <EmailInput
            id="invite-email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="name@gmail.com"
            autoFocus
          />

          {selectableChannels.length > 0 && (
            <>
              <Divider />
              <SectionTitle>팀 채널에 추가 <span style={{ fontWeight: 400, fontSize: 13, color: 'rgba(255,255,255,0.4)' }}>(선택 사항)</span></SectionTitle>
              <SectionDesc>팀원들이 처음부터 올바른 대화에 참여할 수 있도록 하세요.</SectionDesc>

              {suggestedChannels.length > 0 && (
                <>
                  <HintLabel>제안됨:</HintLabel>
                  <ChipRow>
                    {suggestedChannels.map((ch) => (
                      <ChannelChip
                        key={ch.id}
                        type="button"
                        $selected={selectedChannels.includes(ch.name)}
                        onClick={() => toggleChannel(ch.name)}
                      >
                        <Hash size={12} />
                        {ch.name}
                      </ChannelChip>
                    ))}
                  </ChipRow>
                </>
              )}

              <ChannelSearchInput
                value={channelSearch}
                onChange={(e) => setChannelSearch(e.target.value)}
                placeholder="채널 검색"
                $invalid={isChannelSearchInvalid}
              />
              {isChannelSearchInvalid && (
                <ChannelErrorMsg>존재하지 않는 채널입니다.</ChannelErrorMsg>
              )}

              {searchedChannels.length > 0 && (
                <SearchResultList>
                  {searchedChannels.map((ch) => (
                    <SearchResultItem
                      key={ch.id}
                      type="button"
                      $selected={selectedChannels.includes(ch.name)}
                      onClick={() => toggleChannel(ch.name)}
                    >
                      <Hash size={14} />
                      {ch.name}
                      {selectedChannels.includes(ch.name) && (
                        <span style={{ marginLeft: 'auto', fontSize: 12 }}>✓</span>
                      )}
                    </SearchResultItem>
                  ))}
                </SearchResultList>
              )}
            </>
          )}

          <Footer>
            <SendButton type="submit" disabled={!email.trim() || isLoading || isChannelSearchInvalid}>
              {isLoading ? '전송 중...' : '보내기'}
            </SendButton>
          </Footer>
        </form>
      </Card>
    </Overlay>
  );
}
