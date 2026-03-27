import { createWorkspace } from '@/api/workspace';
import { useModalActions, useWorkspaceModalOpen } from '@/store/modal';
import { useQueryClient } from '@tanstack/react-query';
import { X } from 'lucide-react';
import { type FormEvent, useState } from 'react';
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
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 700;
  color: white;
`;

const Label = styled.label`
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: rgb(209, 210, 211);
  margin-bottom: 6px;
`;

const Input = styled.input`
  width: 100%;
  padding: 10px 12px;
  background: #1a1d21;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: white;
  font-size: 15px;
  outline: none;
  margin-bottom: 16px;

  &:focus {
    border-color: rgba(255, 255, 255, 0.5);
  }

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
  margin-top: 4px;

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

const CreateWorkspaceModal = () => {
  const isOpen = useWorkspaceModalOpen();
  const { closeWorkspaceModal } = useModalActions();
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !url.trim()) return;
    setIsLoading(true);
    try {
      await createWorkspace({ workspace: name.trim(), url: url.trim() });
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
      setName('');
      setUrl('');
      closeWorkspaceModal();
    } finally {
      setIsLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <Overlay onClick={closeWorkspaceModal}>
      <Card onClick={(e) => e.stopPropagation()}>
        <CloseButton onClick={closeWorkspaceModal}><X size={18} /></CloseButton>
        <Title>워크스페이스 추가</Title>
        <form onSubmit={onSubmit}>
          <Label htmlFor="ws-name">워크스페이스 이름</Label>
          <Input
            id="ws-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="예) 우리팀 채널"
            autoFocus
          />
          <Label htmlFor="ws-url">워크스페이스 URL</Label>
          <Input
            id="ws-url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="예) our-team"
          />
          <SubmitButton type="submit" disabled={!name.trim() || !url.trim() || isLoading}>
            {isLoading ? '생성 중...' : '워크스페이스 만들기'}
          </SubmitButton>
        </form>
      </Card>
    </Overlay>
  );
};

export default CreateWorkspaceModal;
