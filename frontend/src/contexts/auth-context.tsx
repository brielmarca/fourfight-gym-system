import { createContext, useContext, useCallback, useRef, useState, useEffect, type ReactNode } from "react";
import {
  api,
  setTokens,
  clearTokens,
  getUser,
  restoreAuthSession,
  isAuthenticated as checkIsAuthenticated,
} from "@/lib/api";
import type { RegisterRequest, User } from "@/types";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
  clearAuthState: () => void;
  hasRole: (roles: string[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const resetVersionRef = useRef(0);

  useEffect(() => {
    let mounted = true;

    const restore = async () => {
      const restoreVersion = resetVersionRef.current;
      try {
        await restoreAuthSession();
      } finally {
        if (!mounted) return;
        if (restoreVersion !== resetVersionRef.current) {
          clearTokens();
          setUser(null);
          setIsLoading(false);
          return;
        }
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
    resetVersionRef.current += 1;
    clearTokens();
    setUser(null);

    try {
      const response = await api.auth.login(email, password);
      if (!response.accessToken) {
        throw new Error("Falha no login. Tente novamente.");
      }
      setTokens(response.accessToken);
      setUser(getUser());
    } catch (error) {
      clearTokens();
      setUser(null);
      throw error;
    }
  }, []);

  const register = useCallback(async (data: RegisterRequest) => {
    await api.auth.register(data);
  }, []);

  const logout = useCallback(() => {
    resetVersionRef.current += 1;
    clearTokens();
    setUser(null);
  }, []);

  const clearAuthState = useCallback(() => {
    resetVersionRef.current += 1;
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
        clearAuthState,
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
