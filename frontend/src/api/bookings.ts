import { apiClient } from "./client";

export interface ReserveSeatsInput {
  showtimeId: number;
  seatIds: number[];
  adultTickets: number;
  childTickets: number;
  seniorTickets: number;
}

export interface Booking {
  id: number;
  showtimeId: number;
  status: "DRAFT" | "PAYMENT_PENDING" | "CONFIRMED";
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  confirmationNumber: string | null;
  cardLastFour: string | null;
  confirmedAt: string | null;
  contactEmail: string | null;
  expiresAt: string | null;
  seatIds: number[];
}

export interface ConfirmBookingInput {
  savedCardId?: number;
  cardNumber?: string;
  expirationMonth?: number;
  expirationYear?: number;
  securityCode: string;
}

export interface OrderHistoryItem {
  id: number;
  confirmationNumber: string;
  movieTitle: string;
  showtime: string;
  showroom: string;
  seats: string[];
  adultTickets: number;
  childTickets: number;
  seniorTickets: number;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  cardLastFour: string;
  confirmedAt: string;
}

export async function reserveSeats(
  input: ReserveSeatsInput,
): Promise<Booking> {
  const response = await apiClient.post<Booking>("/bookings/reserve", input);
  return response.data;
}

export async function checkoutBooking(
  contactEmail: string,
): Promise<Booking> {
  const response = await apiClient.post<Booking>("/bookings/checkout", {
    contactEmail,
  });
  return response.data;
}

export async function confirmBooking(
  input: ConfirmBookingInput,
): Promise<Booking> {
  const response = await apiClient.post<Booking>("/bookings/confirm", input);
  return response.data;
}

export async function getOrderHistory(): Promise<OrderHistoryItem[]> {
  const response = await apiClient.get<OrderHistoryItem[]>("/bookings/history");
  return response.data;
}
