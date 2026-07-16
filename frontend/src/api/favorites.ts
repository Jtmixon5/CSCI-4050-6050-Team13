import { apiClient } from "./client";
import type { Movie } from "../types/Movie";

const USER_ID = 1;

export async function getFavorites(): Promise<Movie[]> {
  const response = await apiClient.get<Movie[]>(
    `/users/${USER_ID}/favorites`
  );

  return response.data;
}

export async function addFavorite(
  movieId: number
): Promise<Movie> {
  const response = await apiClient.post<Movie>(
    `/users/${USER_ID}/favorites/${movieId}`
  );

  return response.data;
}

export async function removeFavorite(
  movieId: number
): Promise<void> {
  await apiClient.delete(
    `/users/${USER_ID}/favorites/${movieId}`
  );
}
