import axios from "axios";
import { apiClient } from "./client";
import { initializeCsrf } from "./auth";

export interface Showroom {
  id: number;
  name: string;
  capacity: number;
  active: boolean;
}

export interface Showtime {
  id: number;
  movieId: number;
  movieTitle: string;
  showroomId: number;
  showroomName: string;
  startsAt: string;
  endsAt: string;
  adultPrice: number;
  childPrice: number;
  seniorPrice: number;
  status: string;
}

export interface CreateShowtimeInput {
  movieId: number;
  showroomId: number;
  startsAt: string;
  endsAt: string;
  adultPrice: number;
  childPrice: number;
  seniorPrice: number;
}

export async function getShowrooms(): Promise<Showroom[]> {
  const response = await apiClient.get<Showroom[]>("/showrooms");
  return response.data;
}

export async function getShowtimes(
  movieId?: number,
): Promise<Showtime[]> {
  const response = await apiClient.get<Showtime[]>("/showtimes", {
    params: movieId ? { movieId } : undefined,
  });

  return response.data;
}

export async function createShowtime(
  showtime: CreateShowtimeInput,
): Promise<Showtime> {
  try {
    const response = await apiClient.post<Showtime>(
      "/admin/showtimes",
      showtime,
    );

    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 403) {
      throw error;
    }

    await initializeCsrf();

    const response = await apiClient.post<Showtime>(
      "/admin/showtimes",
      showtime,
    );

    return response.data;
  }
}