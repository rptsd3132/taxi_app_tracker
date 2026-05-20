export interface RegisterRequest {
  name: string;
  email: string;
  phone: string;
  password: string;
  deviceId?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  deviceId?: string;
}

export interface AdminLoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  user: {
    id: string;
    name: string;
    email: string;
    phone: string;
    subscriptionStatus: string;
    subscriptionEndDate: Date | null;
  };
  token: string;
}

export interface JWTPayload {
  id: string;
  email: string;
  role?: string;
}
