export type MovieStatus =
  | "CURRENTLY_PLAYING"
  | "COMING_SOON"
  | "INACTIVE";

export const MOVIE_CATEGORIES = [
  "Action",
  "Adventure",
  "Animation",
  "Comedy",
  "Fantasy",
  "Historical Drama",
  "Horror",
  "Science Fiction",
] as const;

export interface Movie {
  id: number;
  title: string;
  category: string;
  synopsis: string | null;
  status: MovieStatus;
  posterUrl?: string | null;
  trailerUrl?: string | null;
  mpaaRating?: string | null;
}
