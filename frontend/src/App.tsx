import { useEffect, useState } from "react";
import { apiClient } from "./api/client";
import type { Movie } from "./types/Movie";
import "./App.css";

function App() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadMovies() {
      try {
        const response = await apiClient.get<Movie[]>("/movies");
        setMovies(response.data);
      } catch {
        setError("Unable to load movies.");
      }
    }

    void loadMovies();
  }, []);

  return (
    <main>
      <h1>Cinema E-Booking</h1>

      {error && <p>{error}</p>}

      <section>
        <h2>Movies</h2>

        {movies.length === 0 ? (
          <p>No movies are currently available.</p>
        ) : (
          movies.map((movie) => (
            <article key={movie.id}>
              <h3>{movie.title}</h3>
              <p>{movie.category}</p>
              <p>{movie.synopsis}</p>
              <p>Status: {movie.status}</p>
            </article>
          ))
        )}
      </section>
    </main>
  );
}

export default App;