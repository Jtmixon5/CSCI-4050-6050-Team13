import { useState, type FormEvent } from "react";
import axios from "axios";
import { createMovie, type CreateMovieInput } from "../api/movies";
import type { MovieStatus } from "../types/Movie";

interface AddMovieFormProps {
  onBack: () => void;
}

const initialMovie: CreateMovieInput = {
  title: "",
  category: "",
  synopsis: "",
  castMembers: "",
  director: "",
  producer: "",
  mpaaRating: "",
  posterUrl: "",
  trailerUrl: "",
  status: "CURRENTLY_PLAYING",
};

export default function AddMovieForm({ onBack }: AddMovieFormProps) {
  const [movie, setMovie] = useState<CreateMovieInput>(initialMovie);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const update = <K extends keyof CreateMovieInput>(
    field: K,
    value: CreateMovieInput[K],
  ) => {
    setMovie((current) => ({ ...current, [field]: value }));
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const savedMovie = await createMovie(movie);
      setSuccess(`“${savedMovie.title}” was added successfully.`);
      setMovie(initialMovie);
    } catch (requestError) {
      if (axios.isAxiosError(requestError)) {
        if (requestError.response?.status === 403) {
          setError(
            "The request was denied. Confirm that you are signed in as an " +
              "administrator, then sign out and back in if the problem continues.",
          );
        } else if (requestError.response?.status === 400) {
          setError("Check the required fields and enter valid HTTP or HTTPS URLs.");
        } else {
          setError("Unable to add the movie. Please try again.");
        }
      } else {
        setError("Unable to add the movie. Please try again.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="registration-page">
      <button type="button" onClick={onBack}>
        Back to Admin Portal
      </button>

      <section className="registration-card">
        <h1>Add Movie</h1>
        <p className="form-guidance">
          All fields are required. The movie will appear in the customer portal
          immediately after it is saved.
        </p>

        {error && (
          <p className="form-message error-message" role="alert">
            {error}
          </p>
        )}
        {success && (
          <p className="form-message success-message" role="status">
            {success}
          </p>
        )}

        <form className="registration-form" onSubmit={(event) => void submit(event)}>
          <label htmlFor="movie-title">Title *</label>
          <input
            id="movie-title"
            value={movie.title}
            maxLength={200}
            required
            onChange={(event) => update("title", event.target.value)}
          />

          <label htmlFor="movie-category">Category *</label>
          <input
            id="movie-category"
            value={movie.category}
            maxLength={100}
            required
            onChange={(event) => update("category", event.target.value)}
          />

          <label htmlFor="movie-synopsis">Synopsis *</label>
          <textarea
            id="movie-synopsis"
            value={movie.synopsis}
            maxLength={5000}
            rows={5}
            required
            onChange={(event) => update("synopsis", event.target.value)}
          />

          <label htmlFor="movie-cast">Cast members *</label>
          <textarea
            id="movie-cast"
            value={movie.castMembers}
            maxLength={5000}
            rows={3}
            required
            onChange={(event) => update("castMembers", event.target.value)}
          />

          <label htmlFor="movie-director">Director *</label>
          <input
            id="movie-director"
            value={movie.director}
            maxLength={200}
            required
            onChange={(event) => update("director", event.target.value)}
          />

          <label htmlFor="movie-producer">Producer *</label>
          <input
            id="movie-producer"
            value={movie.producer}
            maxLength={200}
            required
            onChange={(event) => update("producer", event.target.value)}
          />

          <label htmlFor="movie-rating">MPAA rating *</label>
          <select
            id="movie-rating"
            value={movie.mpaaRating}
            required
            onChange={(event) => update("mpaaRating", event.target.value)}
          >
            <option value="">Select a rating</option>
            <option value="G">G</option>
            <option value="PG">PG</option>
            <option value="PG-13">PG-13</option>
            <option value="R">R</option>
            <option value="NC-17">NC-17</option>
            <option value="NR">Not Rated</option>
          </select>

          <label htmlFor="movie-poster">Poster URL *</label>
          <input
            id="movie-poster"
            type="url"
            value={movie.posterUrl}
            maxLength={500}
            placeholder="https://example.com/poster.jpg"
            required
            onChange={(event) => update("posterUrl", event.target.value)}
          />

          <label htmlFor="movie-trailer">Trailer URL *</label>
          <input
            id="movie-trailer"
            type="url"
            value={movie.trailerUrl}
            maxLength={500}
            placeholder="https://www.youtube.com/embed/..."
            required
            onChange={(event) => update("trailerUrl", event.target.value)}
          />

          <label htmlFor="movie-status">Release status *</label>
          <select
            id="movie-status"
            value={movie.status}
            required
            onChange={(event) =>
              update("status", event.target.value as MovieStatus)
            }
          >
            <option value="CURRENTLY_PLAYING">Currently Playing</option>
            <option value="COMING_SOON">Coming Soon</option>
            <option value="INACTIVE">Inactive</option>
          </select>

          <button type="submit" disabled={submitting}>
            {submitting ? "Adding Movie..." : "Add Movie"}
          </button>
        </form>
      </section>
    </main>
  );
}
