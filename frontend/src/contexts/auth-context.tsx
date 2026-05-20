import { createContext, useContext, useCallback, useState, useEffect, type ReactNode } from "react";
import {
  api,
  setTokens,
  clearTokens,
  getUser,
  restoreAuthSession,
  isAuthenticated as checkIsAuthenticated,
} from "@/lib/api";
import type { User } from "@/types";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: {
    name: string;
    email: string;
    password: string;
    phone?: string;
    dateOfBirth?: string;
  }) => Promise<void>;
  logout: () => void;
  hasRole: (roles: string[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let mounted = true;

    const restore = async () => {
      try {
        await restoreAuthSession();
      } finally {
        if (!mounted) return;
        setUser(getUser());
        setIsLoading(false);
      }
    };

    void restore();

    return () => {
      mounted = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await api.auth.login(email, password);
    if (!response.accessToken) {
      throw new Error("Falha no login. Tente novamente.");
    }
    setTokens(response.accessToken);
    setUser(getUser());
  }, []);

  const register = useCallback(
    async (data: { name: string; email: string; password: string; phone?: string; dateOfBirth?: string }) => {
      await api.auth.register(data);
    },
    [],
  );

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (roles: string[]) => {
      if (!user) return false;
      return roles.includes(user.role);
    },
    [user],
  );

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user && checkIsAuthenticated(),
        isLoading,
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
