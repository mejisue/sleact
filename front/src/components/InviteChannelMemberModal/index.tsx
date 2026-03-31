import { inviteMemberToChannel } from '@/api/channel';
import { useInviteChannelMemberModalOpen, useModalActions } from '@/store/modal';
import { UserPlus, X } from 'lucide-react';
import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { toast } from 'sonner';
import styled from 'styled-components';

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

const Title = styled.h2`
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 700;
  color: white;
  padding-right: 32px;
`;

const Subtitle = styled.p`
  margin: 0 0 24px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
`;

const Label = styled.label`
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: rgb(209, 210, 211);
  margin-bottom: 8px;
`;

const InputWrapper = styled.div`
  display: flex;
  align-items: center;
  background: #1a1d21;
  border: 2px solid #1d9bd1;
  border-radius: 4px;
  padding: 0 12px;
  gap: 8px;
`;

const EmailInput = styled.input`
  flex: 1;
  padding: 12px 0;
  background: transparent;
  border: none;
  color: white;
  font-size: 15px;
  outline: none;

  &::placeholder {
    color: rgba(255, 255, 255, 0.35);
  }
`;

const Footer = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
`;

const AddButton = styled.button`
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
    opacity: 0.4;
    cursor: not-allowed;
  }
`;

export default function InviteChannelMemberModal() {
  const isOpen = useInviteChannelMemberModalOpen();
  const { closeInviteChannelMemberModal } = useModalActions();
  const { workspaceId, channelName } = useParams<{ workspaceId: string; channelName: string }>();

  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleClose = () => {
    setEmail('');
    closeInviteChannelMemberModal();
  };

  const onSubmit: React.FormEventHandler = async (e) => {
    e.preventDefault();
    if (!email.trim() || !workspaceId || !channelName) return;
    setIsLoading(true);
    try {
      await inviteMemberToChannel(Number(workspaceId), channelName, { email: email.trim() });
      toast.success(`${email.trim()} 님을 #${channelName} 채널에 추가했습니다.`);
      handleClose();
    } catch (err: any) {
      const msg: string = err?.response?.data?.message ?? '';
      if (err?.response?.status === 404) {
        toast.error('해당 이메일로 가입된 사용자가 없거나, 워크스페이스 멤버가 아닙니다.');
      } else if (msg.includes('이미')) {
        toast.error('이미 채널 멤버입니다.');
      } else {
        toast.error('추가에 실패했습니다. 다시 시도해주세요.');
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

        <Title>#{channelName}에 사람 추가</Title>
        <Subtitle>워크스페이스에 속한 멤버만 초대할 수 있습니다.</Subtitle>

        <form onSubmit={onSubmit}>
          <Label htmlFor="channel-invite-email">초대할 사람의 이메일</Label>
          <InputWrapper>
            <UserPlus size={16} color="rgba(255,255,255,0.4)" />
            <EmailInput
              id="channel-invite-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="name@gmail.com"
              autoFocus
            />
          </InputWrapper>

          <Footer>
            <AddButton type="submit" disabled={!email.trim() || isLoading}>
              {isLoading ? '추가 중...' : '추가하기'}
            </AddButton>
          </Footer>
        </form>
      </Card>
    </Overlay>
  );
}
