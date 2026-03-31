import type { IDm } from '@/types';
import api from './index';

export const getDms = (workspaceId: number, userId: number, page: number, perPage: number) =>
  api.get<IDm[]>(`/workspaces/${workspaceId}/dms/${userId}`, {
    params: { page, perPage },
  });
