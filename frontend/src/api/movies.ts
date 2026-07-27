import axios from "axios";
import { apiClient } from "./client";
import { initializeCsrf } from "./auth";
import type { Movie, MovieStatus } from "../types/Movie";

export interface CreateMovieInput {
  title: string;
  category: string;
  synopsis: string;
  castMembers: string;
  director: string;
  producer: string;
  mpaaRating: string;
  posterUrl: string;
  trailerUrl: string;
  status: MovieStatus;
}

export async function createMovie(
  movie: CreateMovieInput,
): Promise<Movie> {
  try {
    const response = await apiClient.post<Movie>("/movies", movie);
    return response.data;
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 403) {
      throw error;
    }

    // A CSRF token can become stale when the server rotates the session ID.
    // Refresh it once before treating the response as an authorization failure.
    await initializeCsrf();
    const response = await apiClient.post<Movie>("/movies", movie);
    return response.data;
  }
}
