import type { IUser } from '@/types';
import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

type State = {
    isLoaded: boolean;
    user: IUser | null;
};

const initialState: State = {
    isLoaded: false,
    user: null,
};

const useAuthStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                setUser: (user: IUser | null) => set({ user, isLoaded: true }),
            },
        })),
        { name: 'authStore' }
    )
);

export const useUser = () => useAuthStore((store) => store.user);
export const useIsAuthLoaded = () => useAuthStore((store) => store.isLoaded);
export const useSetUser = () => useAuthStore((store) => store.actions.setUser);
