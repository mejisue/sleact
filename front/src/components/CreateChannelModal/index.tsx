import { createChannel } from '@/api/channel';
import { useChannelModalOpen, useModalActions } from '@/store/modal';
import { useQueryClient } from '@tanstack/react-query';
import { X } from 'lucide-react';
import { type FormEvent, useState } from 'react';
import { useParams } from 'react-router-dom';
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
  width: 440px;
  padding: 28px;
  color: rgb(209, 210, 211);
  position: relative;
`;

const Title = styled.h2`
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 700;
  color: white;
`;

const Description = styled.p`
  margin: 0 0 20px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
`;

const Label = styled.label`
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: rgb(209, 210, 211);
  margin-bottom: 6px;
`;

const InputWrapper = styled.div`
  display: flex;
  align-items: center;
  background: #1a1d21;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  padding: 0 12px;
  margin-bottom: 20px;

  &:focus-within {
    border-color: rgba(255, 255, 255, 0.5);
  }

  & > span {
    color: rgba(255, 255, 255, 0.4);
    font-size: 15px;
    margin-right: 4px;
  }
`;

const Input = styled.input`
  flex: 1;
  padding: 10px 0;
  background: transparent;
  border: none;
  color: white;
  font-size: 15px;
  outline: none;

  &::placeholder {
    color: rgba(255, 255, 255, 0.3);
  }
`;

const SubmitButton = styled.button`
  width: 100%;
  padding: 10px;
  background: #4a154b;
  border: none;
  border-radius: 4px;
  color: white;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background: #611f69;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
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

const CreateChannelModal = () => {
  const isOpen = useChannelModalOpen();
  const { closeChannelModal } = useModalActions();
  const { workspaceId } = useParams<{ workspaceId: string }>();
  const queryClient = useQueryClient();
  const [channelName, setChannelName] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!channelName.trim() || !workspaceId) return;
    setIsLoading(true);
    try {
      await createChannel(Number(workspaceId), { name: channelName.trim() });
      queryClient.invalidateQueries({ queryKey: ['channels', workspaceId] });
      setChannelName('');
      closeChannelModal();
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <Overlay onClick={closeChannelModal}>
      <Card onClick={(e) => e.stopPropagation()}>
        <CloseButton onClick={closeChannelModal}><X size={18} /></CloseButton>
        <Title>채널 추가</Title>
        <Description>채널은 팀이 소통하는 공간입니다.</Description>
        <form onSubmit={onSubmit}>
          <Label htmlFor="ch-name">채널 이름</Label>
          <InputWrapper>
            <span>#</span>
            <Input
              id="ch-name"
              value={channelName}
              onChange={(e) => setChannelName(e.target.value)}
              placeholder="예) 마케팅"
              autoFocus
            />
          </InputWrapper>
          <SubmitButton type="submit" disabled={!channelName.trim() || isLoading}>
            {isLoading ? '생성 중...' : '채널 만들기'}
          </SubmitButton>
        </form>
      </Card>
    </Overlay>
  );
};

export default CreateChannelModal;
