import { useCallback, useRef, useState, useMemo, type KeyboardEvent, type ChangeEvent } from 'react';
import type { IUser } from '@/types';
import {
  ChatArea,
  Form,
  FormWrapper,
  MentionAvatar,
  MentionDropdown,
  MentionDropdownItem,
  MentionHint,
  MentionName,
  MentionsTextarea,
  SendButton,
  Toolbox,
} from './styles';

const AVATAR_COLORS = ['#e01e5a', '#ecb22e', '#2eb67d', '#36c5f0', '#7c5cbf', '#1d9bd1', '#cc5de8'];
const getAvatarColor = (id: number) => AVATAR_COLORS[id % AVATAR_COLORS.length];

interface Props {
  chat: string;
  onChangeChat: (e: ChangeEvent<HTMLTextAreaElement>) => void;
  onSubmitForm: () => void;
  placeholder: string;
  members?: IUser[];
  setChat?: (value: string) => void;
}

const ChatBox = ({ chat, onChangeChat, onSubmitForm, placeholder, members, setChat }: Props) => {
  const [mention, setMention] = useState<{ query: string; startIndex: number } | null>(null);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const filteredMembers = useMemo(() => {
    if (!mention || !members) return [];
    const q = mention.query.toLowerCase();
    return members.filter((m) => m.nickname.toLowerCase().includes(q));
  }, [mention, members]);

  const handleChange = useCallback(
    (e: ChangeEvent<HTMLTextAreaElement>) => {
      onChangeChat(e);
      const value = e.target.value;
      const cursor = e.target.selectionStart ?? 0;
      const textBeforeCursor = value.slice(0, cursor);
      const lastAt = textBeforeCursor.lastIndexOf('@');

      if (lastAt !== -1) {
        const textAfterAt = textBeforeCursor.slice(lastAt + 1);
        if (!textAfterAt.includes(' ')) {
          setMention({ query: textAfterAt, startIndex: lastAt });
          setSelectedIndex(0);
          return;
        }
      }
      setMention(null);
    },
    [onChangeChat],
  );

  const selectMember = useCallback(
    (member: IUser) => {
      if (!mention || !setChat) return;
      const before = chat.slice(0, mention.startIndex);
      const after = chat.slice(mention.startIndex + 1 + mention.query.length);
      setChat(`${before}@${member.nickname} ${after}`);
      setMention(null);
      textareaRef.current?.focus();
    },
    [mention, chat, setChat],
  );

  const onKeyDown = useCallback(
    (e: KeyboardEvent<HTMLTextAreaElement>) => {
      if (e.nativeEvent.isComposing) return;

      if (mention && filteredMembers.length > 0) {
        if (e.key === 'ArrowUp') {
          e.preventDefault();
          setSelectedIndex((i) => (i - 1 + filteredMembers.length) % filteredMembers.length);
          return;
        }
        if (e.key === 'ArrowDown') {
          e.preventDefault();
          setSelectedIndex((i) => (i + 1) % filteredMembers.length);
          return;
        }
        if (e.key === 'Enter') {
          e.preventDefault();
          selectMember(filteredMembers[selectedIndex]);
          return;
        }
        if (e.key === 'Escape') {
          e.preventDefault();
          setMention(null);
          return;
        }
      }

      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        e.stopPropagation();
        onSubmitForm();
      }
    },
    [mention, filteredMembers, selectedIndex, selectMember, onSubmitForm],
  );

  return (
    <ChatArea>
      <FormWrapper>
        {mention && filteredMembers.length > 0 && (
          <MentionDropdown>
            {filteredMembers.map((member, i) => (
              <MentionDropdownItem
                key={member.id}
                $active={i === selectedIndex}
                onMouseDown={(e) => {
                  e.preventDefault();
                  selectMember(member);
                }}
                onMouseEnter={() => setSelectedIndex(i)}
              >
                <MentionAvatar $color={getAvatarColor(member.id)}>
                  {member.nickname.slice(0, 1).toUpperCase()}
                </MentionAvatar>
                <MentionName>{member.nickname}</MentionName>
              </MentionDropdownItem>
            ))}
            <MentionHint>↑↓ 키로 이동 &nbsp;&nbsp; 선택할 ↵ &nbsp;&nbsp; esc 키를 눌러 해제</MentionHint>
          </MentionDropdown>
        )}
        <Form onSubmit={(e) => { e.preventDefault(); onSubmitForm(); }}>
          <MentionsTextarea
            ref={textareaRef}
            value={chat}
            onChange={handleChange}
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
      </FormWrapper>
    </ChatArea>
  );
};

export default ChatBox;
