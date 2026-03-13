import { useCallback, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';

type Handler = (e: React.ChangeEvent<HTMLInputElement>) => void;
type ReturnTypes<T = any> = [T, Handler, Dispatch<SetStateAction<T>>];

const useInput = <T = any>(initialValue: T): ReturnTypes<T> => {
  const [value, setValue] = useState(initialValue);
  const handler = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setValue(e.target.value as T);
  }, []);
  return [value, handler, setValue];
};

export default useInput;