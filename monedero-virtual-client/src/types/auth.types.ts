export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface VerifyTokenRequest {
  username: string;
  token: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  roles?: string[];
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}
