import { useEffect, useState } from "react";
import { apiClient } from "./api/client";
import type { Movie } from "./types/Movie";
import "./App.css";

function App() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [selectedShowtime, setSelectedShowtime] = useState<string | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);

  const [adultTickets, setAdultTickets] = useState(1);
  const [childTickets, setChildTickets] = useState(0);
  const [seniorTickets, setSeniorTickets] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedGenre, setSelectedGenre] = useState("");
  const [loading, setLoading] = useState(false);

  const showtimes = ["2:00 PM", "5:00 PM", "8:00 PM"];

  const seats = [
    "A1",
    "A2",
    "A3",
    "A4",
    "A5",
    "B1",
    "B2",
    "B3",
    "B4",
    "B5",
    "C1",
    "C2",
    "C3",
    "C4",
    "C5",
  ];

  const formatStatus = (status: string) => {
    return status
      .toLowerCase()
      .split("_")
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(" ");
  };

  useEffect(() => {
    async function loadMovies() {
      try {
        setLoading(true);
        setError(null);

        const params = new URLSearchParams();

        if (searchTerm.trim()) {
          params.append("title", searchTerm.trim());
        }

        if (selectedGenre) {
          params.append("category", selectedGenre);
        }

        const queryString = params.toString();
        const url = queryString ? `/movies?${queryString}` : "/movies";

        const response = await apiClient.get<Movie[]>(url);
        setMovies(response.data);
      } catch {
        setError("Unable to load movies.");
      } finally {
        setLoading(false);
      }
    }

    void loadMovies();
  }, [searchTerm, selectedGenre]);

  const openBookingPage = (movie: Movie, showtime: string) => {
    setSelectedMovie(movie);
    setSelectedShowtime(showtime);
    setSelectedSeats([]);
  };

  const toggleSeat = (seat: string) => {
    setSelectedSeats((currentSeats) =>
      currentSeats.includes(seat)
        ? currentSeats.filter((s) => s !== seat)
        : [...currentSeats, seat]
    );
  };

  const total =
    adultTickets * 14.99 + childTickets * 9.99 + seniorTickets * 11.99;

  const currentlyRunningMovies = movies.filter(
    (movie) => movie.status === "CURRENTLY_PLAYING"
  );

  const comingSoonMovies = movies.filter(
    (movie) => movie.status === "COMING_SOON"
  );

  if (selectedMovie && selectedShowtime) {
    return (
      <main>
        <button
          onClick={() => {
            setSelectedMovie(null);
            setSelectedShowtime(null);
          }}
        >
          Back to Movies
        </button>

        <h1>Booking Page</h1>
        <h2>{selectedMovie.title}</h2>
        <p>Showtime: {selectedShowtime}</p>

        <section>
          <h3>Tickets</h3>

          <label>
            Adult - $14.99
            <input
              type="number"
              min="0"
              value={adultTickets}
              onChange={(e) => setAdultTickets(Number(e.target.value))}
            />
          </label>

          <label>
            Child - $9.99
            <input
              type="number"
              min="0"
              value={childTickets}
              onChange={(e) => setChildTickets(Number(e.target.value))}
            />
          </label>

          <label>
            Senior - $11.99
            <input
              type="number"
              min="0"
              value={seniorTickets}
              onChange={(e) => setSeniorTickets(Number(e.target.value))}
            />
          </label>
        </section>

        <section>
          <h3>Select Seats</h3>
          <div className="screen">SCREEN</div>

          <div className="seat-grid">
            {seats.map((seat) => (
              <button
                key={seat}
                className={
                  selectedSeats.includes(seat) ? "seat selected" : "seat"
                }
                onClick={() => toggleSeat(seat)}
              >
                {seat}
              </button>
            ))}
          </div>

          <p>Selected Seats: {selectedSeats.join(", ") || "None"}</p>
        </section>

        <h3>Total: ${total.toFixed(2)}</h3>
        <button>Continue</button>
      </main>
    );
  }

  return (
    <main>
      <h1>Cinema E-Booking</h1>

      <section>
        <input
          className="search-box"
          type="text"
          placeholder="Search movies by title"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />

        <select
          value={selectedGenre}
          onChange={(e) => setSelectedGenre(e.target.value)}
        >
          <option value="">All Genres</option>
          <option value="Action">Action</option>
          <option value="Adventure">Adventure</option>
          <option value="Animation">Animation</option>
          <option value="Comedy">Comedy</option>
          <option value="Fantasy">Fantasy</option>
          <option value="Historical Drama">Historical Drama</option>
          <option value="Horror">Horror</option>
          <option value="Science Fiction">Science Fiction</option>
        </select>

        <input
          type="date"
          disabled
          title="Show date filtering will be implemented in a later sprint."
        />
      </section>

      <section>
        {loading && <p>Loading movies...</p>}

        {error && <p>{error}</p>}

        {!loading && !error && movies.length === 0 && (
          <p>No movies match your search or filter.</p>
        )}

        {!loading && !error && currentlyRunningMovies.length > 0 && (
          <section>
            <h2>Currently Running</h2>

            {currentlyRunningMovies.map((movie) => (
              <article key={movie.id}>
                <h3>{movie.title}</h3>
                <p>{movie.category}</p>
                <p>{movie.synopsis}</p>
                <p>Status: {formatStatus(movie.status)}</p>

                <h4>Showtimes</h4>
                {showtimes.map((time) => (
                  <button
                    key={time}
                    onClick={() => openBookingPage(movie, time)}
                  >
                    {time}
                  </button>
                ))}
              </article>
            ))}
          </section>
        )}

        {!loading && !error && comingSoonMovies.length > 0 && (
          <section>
            <h2>Coming Soon</h2>

            {comingSoonMovies.map((movie) => (
              <article key={movie.id}>
                <h3>{movie.title}</h3>
                <p>{movie.category}</p>
                <p>{movie.synopsis}</p>
                <p>Status: {formatStatus(movie.status)}</p>
              </article>
            ))}
          </section>
        )}
      </section>
    </main>
  );
}

export default App;
