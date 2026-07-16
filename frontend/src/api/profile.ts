import { apiClient } from "./client";

const USER_ID = 1;

export interface Address {
  street: string;
  city: string;
  state: string;
  zipCode: string;
}

export interface PaymentCard {
  id?: number;
  cardholderName: string;
  lastFour: string;
  billingZipCode: string;
}

export interface UserProfile {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  promotionOptIn: boolean;
  address: Address | null;
  paymentCards: PaymentCard[];
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phoneNumber: string;
  promotionOptIn: boolean;
  address: Address | null;
  paymentCards: PaymentCard[];
}

export async function getProfile(): Promise<UserProfile> {
  const response = await apiClient.get<UserProfile>(
    `/users/${USER_ID}/profile`
  );

  return response.data;
}

export async function updateProfile(
  request: UpdateProfileRequest
): Promise<UserProfile> {
  const response = await apiClient.put<UserProfile>(
    `/users/${USER_ID}/profile`,
    request
  );

  return response.data;
}
