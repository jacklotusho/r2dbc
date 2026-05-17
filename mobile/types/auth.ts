// Backend DTO types - match Spring Boot DTOs exactly
// Shared with web frontend
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  uuid: string;
  username: string;
  roles: string[];
}

export interface UserProfileResponse {
  uuid: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
  confirmPassword: string;
}


