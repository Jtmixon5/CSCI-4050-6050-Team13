import { apiClient } from "./client";
import type { Movie } from "../types/Movie";

const USER_ID = 1; // replace when authentication exists

export async function getFavorites() {
    const response = await apiClient.get<Movie[]>(
        `/users/${USER_ID}/favorites`
    );

    return response.data;
}

export async function addFavorite(movieId: number) {
    await apiClient.post(
        `/users/${USER_ID}/favorites/${movieId}`
    );
}

export async function removeFavorite(movieId: number) {
    await apiClient.delete(
        `/users/${USER_ID}/favorites/${movieId}`
    );
}
