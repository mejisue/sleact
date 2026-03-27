import { useCallback, type KeyboardEvent, type ChangeEvent } from 'react';
import { ChatArea, Form, MentionsTextarea, SendButton, Toolbox } from './styles';

interface Props {
  chat: string;
  onChangeChat: (e: ChangeEvent<HTMLTextAreaElement>) => void;
  onSubmitForm: () => void;
  placeholder: string;
}

const ChatBox = ({ chat, onChangeChat, onSubmitForm, placeholder }: Props) => {
  const onKeyDown = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.nativeEvent.isComposing) return;
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        e.stopPropagation();
        onSubmitForm();
      }
    },
    [onSubmitForm],
  );

  return (
    <ChatArea>
      <Form onSubmit={(e) => { e.preventDefault(); onSubmitForm(); }}>
        <MentionsTextarea
          value={chat}
          onChange={onChangeChat}
          onKeyDown={onKeyDown}
          placeholder={placeholder}
          rows={1}
        />
        <Toolbox>
          <SendButton type="submit" disabled={!chat?.trim()}>
            ➤
          </SendButton>
        </Toolbox>
      </Form>
    </ChatArea>
  );
};

export default ChatBox;
