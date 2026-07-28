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
  status: "DRAFT" | "PAYMENT_PENDING";
  subtotal: number;
  contactEmail: string | null;
  expiresAt: string | null;
  seatIds: number[];
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
