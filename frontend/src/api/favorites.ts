import { apiClient } from "./client";
import type { Movie } from "../types/Movie";

export async function getFavorites(): Promise<Movie[]> {
  const response = await apiClient.get<Movie[]>(
    "/favorites"
  );

  return response.data;
}

export async function addFavorite(
  movieId: number
): Promise<Movie> {
  const response = await apiClient.post<Movie>(
    `/favorites/${movieId}`
  );

  return response.data;
}

export async function removeFavorite(
  movieId: number
): Promise<void> {
  await apiClient.delete(
    `/favorites/${movieId}`
  );
}
