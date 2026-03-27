import styled from 'styled-components';

export const ChatArea = styled.div`
  padding: 0 20px 20px;
  flex-shrink: 0;
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
