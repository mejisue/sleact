import styled from 'styled-components';

const MentionTag = styled.span`
  display: inline;
  background: rgba(82, 104, 209, 0.3);
  color: #7b9fff;
  border-radius: 3px;
  padding: 0 2px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    background: rgba(82, 104, 209, 0.5);
  }
`;

export const renderWithMentions = (content: string) => {
  const parts = content.split(/(@\S+)/g);
  return parts.map((part, i) => {
    if (/^@\S+$/.test(part)) {
      return <MentionTag key={i}>{part}</MentionTag>;
    }
    return part;
  });
};
