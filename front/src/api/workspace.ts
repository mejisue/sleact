import type { IWorkspace } from '@/types';
import api from './index';

export const getWorkspaces = () => api.get<IWorkspace[]>('/workspace');

export const createWorkspace = (data: { workspace: string; url: string }) =>
  api.post<IWorkspace>('/workspace', data);
