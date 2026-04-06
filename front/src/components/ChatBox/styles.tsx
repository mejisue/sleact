import styled from 'styled-components';

export const ChatArea = styled.div`
  padding: 0 20px 20px;
  flex-shrink: 0;
`;

export const FormWrapper = styled.div`
  position: relative;
`;

export const MentionDropdown = styled.div`
  position: absolute;
  bottom: 100%;
  left: 0;
  right: 0;
  background: #1a1d21;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 6px;
  z-index: 50;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.4);
`;

export const MentionDropdownItem = styled.div<{ $active: boolean }>`
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  background: ${(p) => (p.$active ? 'rgba(255, 255, 255, 0.1)' : 'transparent')};

  &:hover {
    background: rgba(255, 255, 255, 0.07);
  }
`;

export const MentionAvatar = styled.div<{ $color: string }>`
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: ${(p) => p.$color};
  font-size: 13px;
  font-weight: 700;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
`;

export const MentionName = styled.span`
  font-size: 15px;
  color: rgb(209, 210, 211);
`;

export const MentionHint = styled.div`
  padding: 6px 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: #111316;
`;

export const Form = styled.form`
  background: #222529;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  overflow: hidden;

  &:focus-within {
    border-color: rgba(255, 255, 255, 0.3);
  }
`;

export const MentionsTextarea = styled.textarea`
  font-family: Slack-Lato, appleLogo, sans-serif;
  font-size: 15px;
  padding: 10px 12px;
  width: 100%;
  resize: none;
  border: none;
  outline: none;
  background: transparent;
  color: rgb(209, 210, 211);
  line-height: 22px;

  &::placeholder {
    color: rgba(255, 255, 255, 0.3);
  }
`;

export const Toolbox = styled.div`
  position: relative;
  background: transparent;
  height: 40px;
  display: flex;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
`;

export const SendButton = styled.button`
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:not(:disabled):hover {
    background: rgba(255, 255, 255, 0.1);
    color: white;
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.3;
  }
`;
