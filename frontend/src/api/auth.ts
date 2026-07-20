import axios from "axios";
import { apiClient } from "./client";

export interface RegistrationRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  password: string;
  promotionOptIn: boolean;
}

interface MessageResponse {
  message: string;
}

interface CsrfResponse {
  headerName: string;
  token: string;
}

export interface AuthUser {
  id: number;
  firstName: string;
  email: string;
  role: "CUSTOMER" | "ADMIN";
}

export async function initializeCsrf(): Promise<void> {
  const response = await apiClient.get<CsrfResponse>("/auth/csrf");
  apiClient.defaults.headers.common[response.data.headerName] =
    response.data.token;
}

export async function register(
  request: RegistrationRequest
): Promise<string> {
  const response = await apiClient.post<MessageResponse>(
    "/auth/register",
    request
  );
  return response.data.message;
}

export async function verifyEmail(
  token: string
): Promise<string> {
  const response = await apiClient.get<MessageResponse>(
    "/auth/verify",
    { params: { token } }
  );
  return response.data.message;
}

export async function login(
  email: string,
  password: string
): Promise<AuthUser> {
  const response = await apiClient.post<AuthUser>("/auth/login", {
    email,
    password,
  });
  await initializeCsrf();
  return response.data;
}

export async function getCurrentUser(): Promise<AuthUser | null> {
  try {
    const response = await apiClient.get<AuthUser>("/auth/me");
    return response.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      return null;
    }
    throw error;
  }
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post("/auth/logout");
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 403) {
      throw error;
    }

    await initializeCsrf();
    await apiClient.post("/auth/logout");
  }
}

export async function requestPasswordReset(email: string): Promise<string> {
  const response = await apiClient.post<MessageResponse>(
    "/auth/forgot-password",
    { email }
  );
  return response.data.message;
}

export async function resetPassword(
  token: string,
  password: string
): Promise<string> {
  const response = await apiClient.post<MessageResponse>(
    "/auth/reset-password",
    { token, password }
  );
  return response.data.message;
}

export async function changePassword(
  currentPassword: string,
  newPassword: string
): Promise<string> {
  const response = await apiClient.post<MessageResponse>(
    "/auth/change-password",
    { currentPassword, newPassword }
  );
  return response.data.message;
}

export function getApiErrorMessage(
  error: unknown,
  fallback: string
): string {
  if (!axios.isAxiosError(error)) {
    return fallback;
  }

  const data = error.response?.data as
    | {
        detail?: string;
        message?: string;
        errors?: Array<{ defaultMessage?: string }>;
      }
    | undefined;

  return (
    data?.errors?.[0]?.defaultMessage ??
    data?.detail ??
    data?.message ??
    fallback
  );
}
