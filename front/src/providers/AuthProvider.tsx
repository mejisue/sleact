import { useEffect, type ReactNode } from 'react';
import { getMe } from '@/api/auth';
import { useIsAuthLoaded, useSetUser } from '@/store/auth';

export default function AuthProvider({ children }: { children: ReactNode }) {
    const setUser = useSetUser();
    const isLoaded = useIsAuthLoaded();

    useEffect(() => {
        getMe()
            .then((res) => setUser(res.data))
            .catch(() => setUser(null));
    }, [setUser]);

    if (!isLoaded) return null;

    return <>{children}</>;
}
