import styled from 'styled-components';

export const Container = styled.div`
  display: flex;
  width: 100%;
  height: 100vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #611f69;
`;

export const Header = styled.header`
  margin-bottom: 24px;

  a {
    color: #ffffff;
    font-size: 48px;
    font-weight: 700;
    text-decoration: none;
  }
`;

export const Form = styled.form`
  width: 400px;
  padding: 40px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
`;

export const Label = styled.label`
  display: block;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;

  span {
    display: block;
    margin-bottom: 6px;
    color: #1d1c1d;
  }
`;

export const Input = styled.input`
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 18px;
  box-sizing: border-box;
  outline: none;

  &:focus {
    border-color: #1264a3;
    box-shadow: 0 0 0 3px rgba(18, 100, 163, 0.2);
  }
`;

export const Button = styled.button`
  width: 100%;
  padding: 14px;
  margin-top: 8px;
  background-color: #4a154b;
  color: #ffffff;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.1s ease;

  &:hover {
    background-color: #611f69;
  }
`;

export const Error = styled.p`
  margin-top: 8px;
  color: #e01e5a;
  font-size: 13px;
`;

export const Success = styled.p`
  margin-top: 8px;
  color: #2eb67d;
  font-size: 13px;
`;

export const LinkContainer = styled.p`
  margin-top: 16px;
  font-size: 14px;
  color: #ffffff;

  a {
    color: #ffffff;
    text-decoration: underline;

    &:hover {
      color: #ddd;
    }
  }
`;
