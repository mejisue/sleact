export interface IUser {
  id: number;
  email: string;
  nickname: string;
  role: string;
}

export interface IWorkspace {
  id: number;
  name: string;
  url: string;
  ownerId: number;
}

export interface IChannel {
  id: number;
  name: string;
  workspaceId: number;
}

export interface IChat {
  id: number;
  content: string;
  createdAt: string;
  user: IUser;
  channelId: number;
}

export interface IDm {
  id: number;
  content: string;
  createdAt: string;
  sender: IUser;
  receiver: IUser;
  senderId: number;
  receiverId: number;
}
