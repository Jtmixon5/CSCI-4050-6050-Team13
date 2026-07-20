import { useEffect, useState } from "react";
import { apiClient } from "./api/client";
import {
  addFavorite,
  getFavorites,
  removeFavorite,
} from "./api/favorites";
import type { Movie } from "./types/Movie";
import MovieDetails from "./components/MovieDetails";
import FavoriteButton from "./components/FavoriteButton";
import ProfilePage from "./components/ProfilePage";
import RegistrationForm from "./components/RegistrationForm";
import EmailVerificationPage from "./components/EmailVerificationPage";
import LoginPage from "./components/LoginPage";
import PasswordResetPage from "./components/PasswordResetPage";
import AdminHome from "./components/AdminHome";
import {
  getCurrentUser,
  initializeCsrf,
  logout,
} from "./api/auth";
import type { AuthUser } from "./api/auth";
import "./App.css";

function App() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [selectedMovie, setSelectedMovie] =
    useState<Movie | null>(null);

  const [selectedShowtime, setSelectedShowtime] =
    useState<string | null>(null);

  const [selectedSeats, setSelectedSeats] =
    useState<string[]>([]);

  const [showProfile, setShowProfile] =
    useState(false);

  const [showRegistration, setShowRegistration] =
    useState(false);

  const [showLogin, setShowLogin] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [authUser, setAuthUser] = useState<AuthUser | null>(null);
  const [authLoading, setAuthLoading] = useState(true);

  const [favoriteIds, setFavoriteIds] =
    useState<number[]>([]);

  const [favoriteLoadingIds, setFavoriteLoadingIds] =
    useState<number[]>([]);

  const [favoriteError, setFavoriteError] =
    useState<string | null>(null);

  const [adultTickets, setAdultTickets] =
    useState(1);

  const [childTickets, setChildTickets] =
    useState(0);

  const [seniorTickets, setSeniorTickets] =
    useState(0);

  const [searchTerm, setSearchTerm] =
    useState("");

  const [selectedGenre, setSelectedGenre] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const showtimes = [
    "2:00 PM",
    "5:00 PM",
    "8:00 PM",
  ];

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

  // Temporary showtime seat data until persisted seat inventory is added.
  const bookedSeats = ["A2", "B4"];
  const unavailableSeats = ["C1"];

  const formatStatus = (status: string) => {
    return status
      .toLowerCase()
      .split("_")
      .map(
        (word) =>
          word.charAt(0).toUpperCase() +
          word.slice(1)
      )
      .join(" ");
  };

  useEffect(() => {
    async function loadMovies() {
      try {
        setLoading(true);
        setError(null);

        const params =
          new URLSearchParams();

        if (searchTerm.trim()) {
          params.append(
            "title",
            searchTerm.trim()
          );
        }

        if (selectedGenre) {
          params.append(
            "category",
            selectedGenre
          );
        }

        const queryString =
          params.toString();

        const url = queryString
          ? `/movies?${queryString}`
          : "/movies";

        const response =
          await apiClient.get<Movie[]>(
            url
          );

        setMovies(response.data);
      } catch {
        setError(
          "Unable to load movies."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadMovies();
  }, [searchTerm, selectedGenre]);

  useEffect(() => {
    async function loadUser() {
      try {
        await initializeCsrf();
        setAuthUser(await getCurrentUser());
      } finally {
        setAuthLoading(false);
      }
    }
    void loadUser();
  }, []);

  useEffect(() => {
    async function loadFavorites() {
      if (!authUser) {
        setFavoriteIds([]);
        return;
      }
      try {
        setFavoriteError(null);

        const favorites =
          await getFavorites();

        setFavoriteIds(
          favorites.map(
            (movie) => movie.id
          )
        );
      } catch {
        setFavoriteError(
          "Unable to load your favorite movies."
        );
      }
    }

    void loadFavorites();
  }, [authUser]);

  const openBookingPage = (
    movie: Movie,
    showtime: string
  ) => {
    setSelectedMovie(movie);
    setSelectedShowtime(showtime);
    setSelectedSeats([]);
  };

  const toggleSeat = (
    seat: string
  ) => {
    if (
      bookedSeats.includes(seat) ||
      unavailableSeats.includes(seat)
    ) {
      return;
    }

    setSelectedSeats(
      (currentSeats) =>
        currentSeats.includes(seat)
          ? currentSeats.filter(
              (currentSeat) =>
                currentSeat !== seat
            )
          : [...currentSeats, seat]
    );
  };

  const toggleFavorite = async (
    movieId: number
  ) => {
    if (!authUser) {
      setFavoriteError("Sign in to save favorite movies.");
      setShowLogin(true);
      return;
    }
    if (
      favoriteLoadingIds.includes(
        movieId
      )
    ) {
      return;
    }

    const wasFavorite =
      favoriteIds.includes(movieId);

    setFavoriteLoadingIds((ids) => [
      ...ids,
      movieId,
    ]);

    setFavoriteError(null);

    try {
      if (wasFavorite) {
        await removeFavorite(movieId);

        setFavoriteIds((ids) =>
          ids.filter(
            (id) => id !== movieId
          )
        );
      } else {
        await addFavorite(movieId);

        setFavoriteIds((ids) =>
          ids.includes(movieId)
            ? ids
            : [...ids, movieId]
        );
      }
    } catch {
      setFavoriteError(
        wasFavorite
          ? "Unable to remove the movie from favorites."
          : "Unable to add the movie to favorites."
      );
    } finally {
      setFavoriteLoadingIds(
        (ids) =>
          ids.filter(
            (id) => id !== movieId
          )
      );
    }
  };

  const total =
    adultTickets * 14.99 +
    childTickets * 9.99 +
    seniorTickets * 11.99;

  const currentlyRunningMovies =
    movies.filter(
      (movie) =>
        movie.status ===
        "CURRENTLY_PLAYING"
    );

  const comingSoonMovies =
    movies.filter(
      (movie) =>
        movie.status ===
        "COMING_SOON"
    );

  const verificationToken =
    window.location.pathname === "/verify-email"
      ? new URLSearchParams(window.location.search).get("token")
      : null;

  const resetToken =
    window.location.pathname === "/reset-password"
      ? new URLSearchParams(window.location.search).get("token")
      : null;

  const returnToLogin = () => {
    window.history.replaceState({}, "", "/");
    setShowForgotPassword(false);
    setShowRegistration(false);
    setShowLogin(true);
  };

  const signOut = async () => {
    try {
      setError(null);
      await logout();
      setAuthUser(null);
      setShowProfile(false);
      setSelectedMovie(null);
      setSelectedShowtime(null);
      await initializeCsrf();
    } catch {
      setError("Unable to log out. Please try again.");
    }
  };

  if (verificationToken) {
    return (
      <EmailVerificationPage
        token={verificationToken}
        onContinue={() => {
          window.history.replaceState({}, "", "/");
          window.location.reload();
        }}
      />
    );
  }

  if (resetToken) {
    return (
      <PasswordResetPage
        token={resetToken}
        onContinue={returnToLogin}
      />
    );
  }

  if (authLoading) {
    return <main><p>Loading...</p></main>;
  }

  if (authUser?.role === "ADMIN") {
    return (
      <AdminHome
        name={authUser.firstName}
        onLogout={() => void signOut()}
      />
    );
  }

  if (showForgotPassword) {
    return (
      <PasswordResetPage
        onContinue={returnToLogin}
      />
    );
  }

  if (showLogin) {
    return (
      <LoginPage
        onLogin={(user) => {
          setAuthUser(user);
          setShowLogin(false);
        }}
        onCancel={() => setShowLogin(false)}
        onForgotPassword={() => {
          setShowLogin(false);
          setShowForgotPassword(true);
        }}
        onRegister={() => {
          setShowLogin(false);
          setShowRegistration(true);
        }}
      />
    );
  }

  if (showRegistration) {
    return (
      <RegistrationForm
        onCancel={() => setShowRegistration(false)}
      />
    );
  }

  if (showProfile) {
    return (
      <main>
        <button
          type="button"
          onClick={() =>
            setShowProfile(false)
          }
        >
          Back to Movies
        </button>

        <ProfilePage />
      </main>
    );
  }

  if (
    selectedMovie &&
    selectedShowtime
  ) {
    return (
      <main>
        <button
          type="button"
          onClick={() => {
            setSelectedMovie(null);
            setSelectedShowtime(null);
          }}
        >
          Back to Movies
        </button>

        <h1>Booking Page</h1>

        <h2>
          {selectedMovie.title}
        </h2>

        <p>
          Showtime:{" "}
          {selectedShowtime}
        </p>

        <section>
          <h3>Tickets</h3>

          <label>
            Adult - $14.99
            <input
              type="number"
              min="0"
              value={adultTickets}
              onChange={(event) =>
                setAdultTickets(
                  Number(
                    event.target.value
                  )
                )
              }
            />
          </label>

          <label>
            Child - $9.99
            <input
              type="number"
              min="0"
              value={childTickets}
              onChange={(event) =>
                setChildTickets(
                  Number(
                    event.target.value
                  )
                )
              }
            />
          </label>

          <label>
            Senior - $11.99
            <input
              type="number"
              min="0"
              value={seniorTickets}
              onChange={(event) =>
                setSeniorTickets(
                  Number(
                    event.target.value
                  )
                )
              }
            />
          </label>
        </section>

        <section>
          <h3>Select Seats</h3>

          <div className="screen">
            SCREEN
          </div>

          <div
            className="seat-legend"
            aria-label="Seat status legend"
          >
            <span>
              <span className="legend-swatch available" />
              Available
            </span>
            <span>
              <span className="legend-swatch selected" />
              Selected
            </span>
            <span>
              <span className="legend-swatch booked" />
              Booked
            </span>
            <span>
              <span className="legend-swatch unavailable" />
              Not available
            </span>
          </div>

          <div className="seat-grid">
            {seats.map((seat) => {
              const isBooked =
                bookedSeats.includes(seat);
              const isUnavailable =
                unavailableSeats.includes(seat);
              const isSelected =
                selectedSeats.includes(seat);
              const status = isBooked
                ? "booked"
                : isUnavailable
                  ? "unavailable"
                  : isSelected
                    ? "selected"
                    : "available";

              return (
                <button
                  type="button"
                  key={seat}
                  className={`seat ${status}`}
                  disabled={isBooked || isUnavailable}
                  aria-label={`${seat}, ${
                    status === "unavailable"
                      ? "not available"
                      : status
                  }`}
                  title={
                    status === "unavailable"
                      ? "Not available"
                      : status.charAt(0).toUpperCase() +
                        status.slice(1)
                  }
                  onClick={() => toggleSeat(seat)}
                >
                  {seat}
                </button>
              );
            })}
          </div>

          <p>
            Selected Seats:{" "}
            {selectedSeats.join(", ") ||
              "None"}
          </p>
        </section>

        <h3>
          Total: ${total.toFixed(2)}
        </h3>

        <button type="button">
          Continue
        </button>
      </main>
    );
  }

  if (
    selectedMovie &&
    !selectedShowtime
  ) {
    return (
      <main>
        <button
          type="button"
          onClick={() => {
            setSelectedMovie(null);
            setSelectedShowtime(null);
          }}
        >
          Back to Movies
        </button>

        <MovieDetails
          movie={selectedMovie}
          onSelectShowtime={(time) =>
            openBookingPage(
              selectedMovie,
              time
            )
          }
        />
      </main>
    );
  }

  return (
    <main>
      <h1>Cinema E-Booking</h1>

      <button
        type="button"
        onClick={() => authUser
          ? setShowProfile(true)
          : setShowLogin(true)}
      >
        {authUser ? "My Profile" : "Sign In"}
      </button>

      {authUser ? (
        <>
          <span>Welcome, {authUser.firstName}</span>
          <button type="button" onClick={() => void signOut()}>
            Logout
          </button>
        </>
      ) : (
        <button
          type="button"
          onClick={() => setShowRegistration(true)}
        >
          Register
        </button>
      )}

      <section>
        <input
          className="search-box"
          type="text"
          placeholder="Search movies by title"
          value={searchTerm}
          onChange={(event) =>
            setSearchTerm(
              event.target.value
            )
          }
        />

        <select
          value={selectedGenre}
          onChange={(event) =>
            setSelectedGenre(
              event.target.value
            )
          }
        >
          <option value="">
            All Genres
          </option>

          <option value="Action">
            Action
          </option>

          <option value="Adventure">
            Adventure
          </option>

          <option value="Animation">
            Animation
          </option>

          <option value="Comedy">
            Comedy
          </option>

          <option value="Fantasy">
            Fantasy
          </option>

          <option value="Historical Drama">
            Historical Drama
          </option>

          <option value="Horror">
            Horror
          </option>

          <option value="Science Fiction">
            Science Fiction
          </option>
        </select>

        <input
          type="date"
          disabled
          title="Show date filtering will be implemented in a later sprint."
        />
      </section>

      <section>
        {loading && (
          <p>Loading movies...</p>
        )}

        {error && (
          <p role="alert">
            {error}
          </p>
        )}

        {favoriteError && (
          <p role="alert">
            {favoriteError}
          </p>
        )}

        {!loading &&
          !error &&
          movies.length === 0 && (
            <p>
              No movies match your
              search or filter.
            </p>
          )}

        {!loading &&
          !error &&
          currentlyRunningMovies.length >
            0 && (
            <section>
              <h2>
                Currently Running
              </h2>

              {currentlyRunningMovies.map(
                (movie) => (
                  <article key={movie.id} className="movie-card">
                    <img
                      className="movie-poster"
                      src={movie.posterUrl || "/placeholder-poster.svg"}
                      alt={`${movie.title} poster`}
                      loading="lazy"
                      onError={(event) => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = "/placeholder-poster.svg";
                      }}
                    />

                    <div className="movie-card-content">
                    <h3
                      style={{
                        display: "flex",
                        alignItems:
                          "center",
                        gap: "8px",
                      }}
                    >
                      <button
                        type="button"
                        onClick={() =>
                          setSelectedMovie(
                            movie
                          )
                        }
                        style={{
                          background:
                            "none",
                          border: "none",
                          padding: 0,
                          margin: 0,
                          color:
                            "var(--accent)",
                          textDecoration:
                            "underline",
                          cursor:
                            "pointer",
                          fontSize:
                            "inherit",
                        }}
                      >
                        {movie.title}
                      </button>

                      <FavoriteButton
                        isFavorite={favoriteIds.includes(
                          movie.id
                        )}
                        disabled={favoriteLoadingIds.includes(
                          movie.id
                        )}
                        onClick={() =>
                          toggleFavorite(
                            movie.id
                          )
                        }
                      />
                    </h3>

                    <p>
                      {movie.category}
                    </p>

                    <p>
                      {movie.synopsis}
                    </p>

                    <p>
                      Status:{" "}
                      {formatStatus(
                        movie.status
                      )}
                    </p>

                    <h4>Showtimes</h4>

                    {showtimes.map(
                      (time) => (
                        <button
                          type="button"
                          key={time}
                          onClick={() =>
                            openBookingPage(
                              movie,
                              time
                            )
                          }
                        >
                          {time}
                        </button>
                      )
                    )}
                    </div>
                  </article>
                )
              )}
            </section>
          )}

        {!loading &&
          !error &&
          comingSoonMovies.length >
            0 && (
            <section>
              <h2>Coming Soon</h2>

              {comingSoonMovies.map(
                (movie) => (
                  <article key={movie.id} className="movie-card">
                    <img
                      className="movie-poster"
                      src={movie.posterUrl || "/placeholder-poster.svg"}
                      alt={`${movie.title} poster`}
                      loading="lazy"
                      onError={(event) => {
                        event.currentTarget.onerror = null;
                        event.currentTarget.src = "/placeholder-poster.svg";
                      }}
                    />

                    <div className="movie-card-content">
                    <h3
                      style={{
                        display: "flex",
                        alignItems:
                          "center",
                        gap: "8px",
                      }}
                    >
                      <button
                        type="button"
                        onClick={() =>
                          setSelectedMovie(
                            movie
                          )
                        }
                        style={{
                          background:
                            "none",
                          border: "none",
                          padding: 0,
                          margin: 0,
                          color:
                            "var(--accent)",
                          textDecoration:
                            "underline",
                          cursor:
                            "pointer",
                          fontSize:
                            "inherit",
                        }}
                      >
                        {movie.title}
                      </button>

                      <FavoriteButton
                        isFavorite={favoriteIds.includes(
                          movie.id
                        )}
                        disabled={favoriteLoadingIds.includes(
                          movie.id
                        )}
                        onClick={() =>
                          toggleFavorite(
                            movie.id
                          )
                        }
                      />
                    </h3>

                    <p>
                      {movie.category}
                    </p>

                    <p>
                      {movie.synopsis}
                    </p>

                    <p>
                      Status:{" "}
                      {formatStatus(
                        movie.status
                      )}
                    </p>
                    </div>
                  </article>
                )
              )}
            </section>
          )}
      </section>
    </main>
  );
}

export default App;
