import { create } from 'zustand';
import { combine, devtools } from 'zustand/middleware';

export interface AuthUser {
    email: string;
    role: string;
    nickname: string;
}

type State = {
    isLoaded: boolean;
    user: AuthUser | null;
};

const initialState: State = {
    isLoaded: false,
    user: null,
};

const useAuthStore = create(
    devtools(
        combine(initialState, (set) => ({
            actions: {
                setUser: (user: AuthUser | null) => set({ user, isLoaded: true }),
            },
        })),
        { name: 'authStore' }
    )
);

export const useUser = () => useAuthStore((store) => store.user);
export const useIsAuthLoaded = () => useAuthStore((store) => store.isLoaded);
export const useSetUser = () => useAuthStore((store) => store.actions.setUser);
