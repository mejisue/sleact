import type { IUser, IWorkspace } from '@/types';
import api from './index';

export const getWorkspaces = () => api.get<IWorkspace[]>('/workspace');

export const createWorkspace = (data: { workspace: string; url: string }) =>
  api.post<IWorkspace>('/workspace', data);

export const inviteMember = (workspaceId: number, data: { email: string; channelNames?: string[] }) =>
  api.post(`/workspace/${workspaceId}/member`, data);

export const getWorkspaceMembers = (workspaceId: number) =>
  api.get<IUser[]>(`/workspace/${workspaceId}/members`);

export const getOnlineMembers = (workspaceId: number) =>
  api.get<string[]>(`/workspace/${workspaceId}/members/online`);
