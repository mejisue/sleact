import type { IChannel, IChat, IUser } from '@/types';
import api from './index';

export const getChannels = (workspaceId: number) =>
  api.get<IChannel[]>(`/workspace/${workspaceId}/channels`);

export const getChannelChats = (workspaceId: number, channelName: string, page: number, perPage: number) =>
  api.get<IChat[]>(`/workspace/${workspaceId}/channels/${channelName}/chats`, {
    params: { page, perPage },
  });

export const getChannelMembers = (workspaceId: number, channelName: string) =>
  api.get<IUser[]>(`/workspace/${workspaceId}/channels/${channelName}/members`);

export const createChannel = (workspaceId: number, data: { name: string }) =>
  api.post(`/workspace/${workspaceId}/channels`, data);

export const inviteMemberToChannel = (workspaceId: number, channelName: string, data: { email: string }) =>
  api.post(`/workspace/${workspaceId}/channels/${channelName}/members`, data);
