import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';
import { useShallow } from 'zustand/react/shallow';

const useModalStore = create(
  devtools(
    combine(
      {
        isWorkspaceModalOpen: false,
        isChannelModalOpen: false,
        isInviteModalOpen: false,
      },
      (set) => ({
        openWorkspaceModal: () => set({ isWorkspaceModalOpen: true }),
        closeWorkspaceModal: () => set({ isWorkspaceModalOpen: false }),
        openChannelModal: () => set({ isChannelModalOpen: true }),
        closeChannelModal: () => set({ isChannelModalOpen: false }),
        openInviteModal: () => set({ isInviteModalOpen: true }),
        closeInviteModal: () => set({ isInviteModalOpen: false }),
      }),
    ),
    { name: 'modalStore' },
  ),
);

export const useWorkspaceModalOpen = () => useModalStore((s) => s.isWorkspaceModalOpen);
export const useChannelModalOpen = () => useModalStore((s) => s.isChannelModalOpen);
export const useInviteModalOpen = () => useModalStore((s) => s.isInviteModalOpen);
export const useModalActions = () =>
  useModalStore(
    useShallow((s) => ({
      openWorkspaceModal: s.openWorkspaceModal,
      closeWorkspaceModal: s.closeWorkspaceModal,
      openChannelModal: s.openChannelModal,
      closeChannelModal: s.closeChannelModal,
      openInviteModal: s.openInviteModal,
      closeInviteModal: s.closeInviteModal,
    })),
  );
