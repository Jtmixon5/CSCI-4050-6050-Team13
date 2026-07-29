import { useEffect, useMemo, useState } from "react";
import { apiClient } from "./api/client";
import { addFavorite, getFavorites, removeFavorite } from "./api/favorites";
import { getSeatMap } from "./api/showtimes";
import type { Seat, Showtime } from "./api/showtimes";
import { checkoutBooking, confirmBooking, reserveSeats } from "./api/bookings";
import type { Booking } from "./api/bookings";
import { getProfile, type PaymentCard } from "./api/profile";
import { MOVIE_CATEGORIES, type Movie } from "./types/Movie";
import MovieDetails from "./components/MovieDetails";
import FavoriteButton from "./components/FavoriteButton";
import ProfilePage from "./components/ProfilePage";
import RegistrationForm from "./components/RegistrationForm";
import EmailVerificationPage from "./components/EmailVerificationPage";
import LoginPage from "./components/LoginPage";
import PasswordResetPage from "./components/PasswordResetPage";
import AdminHome from "./components/AdminHome";
import OrderHistoryPage from "./components/OrderHistoryPage";
import {
  getApiErrorMessage,
  getCurrentUser,
  initializeCsrf,
  logout,
} from "./api/auth";
import type { AuthUser } from "./api/auth";
import "./App.css";

type BookingStep = "tickets" | "seats" | "summary" | "payment" | "confirmation";

const formatDateTime = (value: string): string =>
  new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));

function App() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedGenre, setSelectedGenre] = useState("");

  const [selectedMovie, setSelectedMovie] = useState<Movie | null>(null);
  const [selectedShowtime, setSelectedShowtime] = useState<Showtime | null>(null);
  const [bookingStep, setBookingStep] = useState<BookingStep>("tickets");
  const [seatMap, setSeatMap] = useState<Seat[]>([]);
  const [seatLoading, setSeatLoading] = useState(false);
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [selectedSeats, setSelectedSeats] = useState<number[]>([]);
  const [adultTickets, setAdultTickets] = useState(1);
  const [childTickets, setChildTickets] = useState(0);
  const [seniorTickets, setSeniorTickets] = useState(0);
  const [checkoutEmail, setCheckoutEmail] = useState("");
  const [resumeCheckoutAfterLogin, setResumeCheckoutAfterLogin] = useState(false);
  const [bookingSubmitting, setBookingSubmitting] = useState(false);
  const [cardNumber, setCardNumber] = useState("");
  const [expirationMonth, setExpirationMonth] = useState("");
  const [expirationYear, setExpirationYear] = useState("");
  const [securityCode, setSecurityCode] = useState("");
  const [confirmedBooking, setConfirmedBooking] = useState<Booking | null>(null);
  const [savedCards, setSavedCards] = useState<PaymentCard[]>([]);
  const [selectedPaymentCard, setSelectedPaymentCard] = useState("new");

  const [showProfile, setShowProfile] = useState(false);
  const [showOrders, setShowOrders] = useState(false);
  const [showRegistration, setShowRegistration] = useState(false);
  const [showLogin, setShowLogin] = useState(false);
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [authUser, setAuthUser] = useState<AuthUser | null>(null);
  const [authLoading, setAuthLoading] = useState(true);

  const [favoriteIds, setFavoriteIds] = useState<number[]>([]);
  const [favoriteLoadingIds, setFavoriteLoadingIds] = useState<number[]>([]);
  const [favoriteError, setFavoriteError] = useState<string | null>(null);

  useEffect(() => {
    async function loadMovies() {
      try {
        setLoading(true);
        setError(null);
        const params = new URLSearchParams();
        if (searchTerm.trim()) params.append("title", searchTerm.trim());
        if (selectedGenre) params.append("category", selectedGenre);
        const query = params.toString();
        const response = await apiClient.get<Movie[]>(query ? `/movies?${query}` : "/movies");
        setMovies(response.data);
      } catch {
        setError("Unable to load movies.");
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
        const currentUser = await getCurrentUser();
        setAuthUser(currentUser);
        if (currentUser) {
          setCheckoutEmail(currentUser.email);
        }
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
        const favorites = await getFavorites();
        setFavoriteIds(favorites.map((movie) => movie.id));
      } catch {
        setFavoriteError("Unable to load your favorite movies.");
      }
    }
    void loadFavorites();
  }, [authUser]);

  useEffect(() => {
    if (!authUser) {
      setSavedCards([]);
      setSelectedPaymentCard("new");
      return;
    }
    getProfile()
      .then((profile) => {
        setSavedCards(profile.paymentCards);
        setSelectedPaymentCard(
          profile.paymentCards[0]?.id
            ? String(profile.paymentCards[0].id)
            : "new",
        );
      })
      .catch(() => {
        setSavedCards([]);
        setSelectedPaymentCard("new");
      });
  }, [authUser]);

  const totalTickets = adultTickets + childTickets + seniorTickets;
  const selectedSeatObjects = useMemo(
    () => seatMap.filter((seat) => selectedSeats.includes(seat.id)),
    [seatMap, selectedSeats],
  );

  const subtotal = selectedShowtime
    ? adultTickets * Number(selectedShowtime.adultPrice) +
      childTickets * Number(selectedShowtime.childPrice) +
      seniorTickets * Number(selectedShowtime.seniorPrice)
    : 0;

  const resetBooking = () => {
    setSelectedShowtime(null);
    setBookingStep("tickets");
    setSeatMap([]);
    setSelectedSeats([]);
    setAdultTickets(1);
    setChildTickets(0);
    setSeniorTickets(0);
    setBookingError(null);
    setResumeCheckoutAfterLogin(false);
    setBookingSubmitting(false);
    setCardNumber("");
    setExpirationMonth("");
    setExpirationYear("");
    setSecurityCode("");
    setConfirmedBooking(null);
    setSelectedPaymentCard(savedCards[0]?.id ? String(savedCards[0].id) : "new");
  };

  const closeMovie = () => {
    resetBooking();
    setSelectedMovie(null);
  };

  const startBooking = async (movie: Movie, showtime: Showtime) => {
    setSelectedMovie(movie);
    setSelectedShowtime(showtime);
    setBookingStep("tickets");
    setSelectedSeats([]);
    setBookingError(null);
    setSeatLoading(true);
    try {
      const response = await getSeatMap(showtime.id);
      setSeatMap(response.seats);
    } catch {
      setSeatMap([]);
      setBookingError("Unable to load the seat map for this showtime.");
    } finally {
      setSeatLoading(false);
    }
  };

  const goToSeats = () => {
    if (totalTickets < 1) {
      setBookingError("Select at least one ticket before continuing.");
      return;
    }
    setBookingError(null);
    setBookingStep("seats");
  };

  const toggleSeat = (seat: Seat) => {
    if (seat.status === "BOOKED") return;
    setBookingError(null);
    setSelectedSeats((current) => {
      if (current.includes(seat.id)) return current.filter((id) => id !== seat.id);
      if (current.length >= totalTickets) {
        setBookingError(`You may select exactly ${totalTickets} seat(s).`);
        return current;
      }
      return [...current, seat.id];
    });
  };

  const goToSummary = async () => {
    if (selectedSeats.length !== totalTickets) {
      setBookingError(`Select exactly ${totalTickets} seat(s) before continuing.`);
      return;
    }

    if (!selectedShowtime) return;

    setBookingSubmitting(true);
    setBookingError(null);
    try {
      await reserveSeats({
        showtimeId: selectedShowtime.id,
        seatIds: selectedSeats,
        adultTickets,
        childTickets,
        seniorTickets,
      });
      setBookingStep("summary");
    } catch (requestError) {
      setBookingError(getApiErrorMessage(
        requestError,
        "Unable to reserve those seats. Refresh the seat map and try again.",
      ));
      try {
        const refreshedMap = await getSeatMap(selectedShowtime.id);
        setSeatMap(refreshedMap.seats);
        const availableIds = new Set(
          refreshedMap.seats
            .filter((seat) => seat.status === "AVAILABLE")
            .map((seat) => seat.id),
        );
        setSelectedSeats((current) =>
          current.filter((seatId) => availableIds.has(seatId))
        );
      } catch {
        // Preserve the reservation error when the refresh also fails.
      }
    } finally {
      setBookingSubmitting(false);
    }
  };

  const finishCheckout = async (email: string) => {
    setBookingSubmitting(true);
    setBookingError(null);
    try {
      await checkoutBooking(email);
      setBookingStep("payment");
    } catch (requestError) {
      setBookingError(getApiErrorMessage(
        requestError,
        "Unable to continue to payment. Select your seats again.",
      ));
      setBookingStep("summary");
    } finally {
      setBookingSubmitting(false);
    }
  };

  const proceedToPayment = () => {
    const email = checkoutEmail.trim();
    if (!email || !email.includes("@")) {
      setBookingError("Enter a valid confirmation email address.");
      return;
    }
    if (!authUser) {
      setResumeCheckoutAfterLogin(true);
      setShowLogin(true);
      return;
    }
    void finishCheckout(email);
  };

  const submitPayment = async () => {
    const digits = cardNumber.replace(/\D/g, "");
    const usingSavedCard = selectedPaymentCard !== "new";
    if (!usingSavedCard && !/^\d{13,19}$/.test(digits)) {
      setBookingError("Enter a valid card number.");
      return;
    }
    if (!/^\d{3,4}$/.test(securityCode)) {
      setBookingError("Enter the card's 3 or 4 digit security code.");
      return;
    }
    setBookingSubmitting(true);
    setBookingError(null);
    try {
      const booking = await confirmBooking({
        ...(usingSavedCard
          ? { savedCardId: Number(selectedPaymentCard) }
          : {
              cardNumber: digits,
              expirationMonth: Number(expirationMonth),
              expirationYear: Number(expirationYear),
            }),
        securityCode,
      });
      setConfirmedBooking(booking);
      setBookingStep("confirmation");
    } catch (requestError) {
      setBookingError(getApiErrorMessage(requestError, "Payment could not be completed."));
    } finally {
      setBookingSubmitting(false);
    }
  };

  const toggleFavorite = async (movieId: number) => {
    if (!authUser) {
      setFavoriteError("Sign in to save favorite movies.");
      setShowLogin(true);
      return;
    }
    if (favoriteLoadingIds.includes(movieId)) return;
    const wasFavorite = favoriteIds.includes(movieId);
    setFavoriteLoadingIds((ids) => [...ids, movieId]);
    setFavoriteError(null);
    try {
      if (wasFavorite) {
        await removeFavorite(movieId);
        setFavoriteIds((ids) => ids.filter((id) => id !== movieId));
      } else {
        await addFavorite(movieId);
        setFavoriteIds((ids) => (ids.includes(movieId) ? ids : [...ids, movieId]));
      }
    } catch {
      setFavoriteError(
        wasFavorite
          ? "Unable to remove the movie from favorites."
          : "Unable to add the movie to favorites.",
      );
    } finally {
      setFavoriteLoadingIds((ids) => ids.filter((id) => id !== movieId));
    }
  };

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
      resetBooking();
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

  if (resetToken) return <PasswordResetPage token={resetToken} onContinue={returnToLogin} />;
  if (authLoading) return <main><p>Loading...</p></main>;
  if (authUser?.role === "ADMIN") {
    return <AdminHome name={authUser.firstName} onLogout={() => void signOut()} />;
  }
  if (showForgotPassword) return <PasswordResetPage onContinue={returnToLogin} />;
  if (showLogin) {
    return (
      <LoginPage
        onLogin={(user) => {
          setAuthUser(user);
          const email = checkoutEmail.trim() || user.email;
          setCheckoutEmail(email);
          setShowLogin(false);
          if (resumeCheckoutAfterLogin) {
            setResumeCheckoutAfterLogin(false);
            void finishCheckout(email);
          }
        }}
        onCancel={() => {
          setShowLogin(false);
          setResumeCheckoutAfterLogin(false);
        }}
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
  if (showRegistration) return <RegistrationForm onCancel={() => setShowRegistration(false)} />;
  if (showProfile) {
    return (
      <main>
        <button type="button" onClick={() => setShowProfile(false)}>Back to Movies</button>
        <ProfilePage />
      </main>
    );
  }
  if (showOrders) {
    return <OrderHistoryPage onBack={() => setShowOrders(false)} />;
  }

  if (selectedMovie && selectedShowtime) {
    return (
      <main className="booking-page">
        <button type="button" onClick={closeMovie}>Back to Movies</button>
        <h1>Book Tickets</h1>
        <div className="booking-progress" aria-label="Booking progress">
          {(["tickets", "seats", "summary", "payment", "confirmation"] as BookingStep[]).map((step, index) => (
            <span key={step} className={bookingStep === step ? "active" : ""}>
              {index + 1}. {step.charAt(0).toUpperCase() + step.slice(1)}
            </span>
          ))}
        </div>

        <section className="booking-heading">
          <h2>{selectedMovie.title}</h2>
          <p>{formatDateTime(selectedShowtime.startsAt)} | {selectedShowtime.showroomName}</p>
        </section>

        {bookingError && <p className="form-error" role="alert">{bookingError}</p>}

        {bookingStep === "tickets" && (
          <section className="booking-card">
            <h3>Select Tickets</h3>
            <div className="ticket-grid">
              <label>
                Adult - ${Number(selectedShowtime.adultPrice).toFixed(2)}
                <input type="number" min="0" value={adultTickets} onChange={(event) => setAdultTickets(Math.max(0, Number(event.target.value)))} />
              </label>
              <label>
                Child - ${Number(selectedShowtime.childPrice).toFixed(2)}
                <input type="number" min="0" value={childTickets} onChange={(event) => setChildTickets(Math.max(0, Number(event.target.value)))} />
              </label>
              <label>
                Senior - ${Number(selectedShowtime.seniorPrice).toFixed(2)}
                <input type="number" min="0" value={seniorTickets} onChange={(event) => setSeniorTickets(Math.max(0, Number(event.target.value)))} />
              </label>
            </div>
            <p><strong>Total tickets:</strong> {totalTickets}</p>
            <p><strong>Subtotal:</strong> ${subtotal.toFixed(2)}</p>
            <button type="button" onClick={goToSeats}>Choose Seats</button>
          </section>
        )}

        {bookingStep === "seats" && (
          <section className="booking-card">
            <h3>Select {totalTickets} Seat(s)</h3>
            <p>{selectedSeats.length} of {totalTickets} selected</p>
            <div className="screen">SCREEN</div>
            <div className="seat-legend" aria-label="Seat status legend">
              <span><span className="legend-swatch available" />Available</span>
              <span><span className="legend-swatch selected" />Selected</span>
              <span><span className="legend-swatch booked" />Booked</span>
              <span><span className="legend-swatch accessible" />Accessible</span>
            </div>
            {seatLoading && <p>Loading seat map...</p>}
            {!seatLoading && seatMap.length === 0 && <p>No seats are available for this showroom.</p>}
            <div className="seat-grid database-seat-grid">
              {seatMap.map((seat) => {
                const selected = selectedSeats.includes(seat.id);
                const statusClass = seat.status === "BOOKED" ? "booked" : selected ? "selected" : "available";
                return (
                  <button
                    type="button"
                    key={seat.id}
                    className={`seat ${statusClass} ${seat.accessible ? "accessible" : ""}`}
                    disabled={seat.status === "BOOKED"}
                    aria-label={`${seat.label}, ${seat.status === "BOOKED" ? "booked" : selected ? "selected" : "available"}${seat.accessible ? ", accessible" : ""}`}
                    title={`${seat.label}${seat.accessible ? " - Accessible" : ""}`}
                    onClick={() => toggleSeat(seat)}
                  >
                    {seat.label}
                  </button>
                );
              })}
            </div>
            <p><strong>Selected:</strong> {selectedSeatObjects.map((seat) => seat.label).join(", ") || "None"}</p>
            <div>
              <button type="button" onClick={() => setBookingStep("tickets")}>Back</button>
              <button
                type="button"
                disabled={bookingSubmitting}
                onClick={() => void goToSummary()}
              >
                {bookingSubmitting ? "Reserving Seats..." : "Proceed to Checkout"}
              </button>
            </div>
          </section>
        )}

        {bookingStep === "summary" && (
          <section className="booking-card order-summary">
            <h3>Order Summary</h3>
            <dl>
              <div><dt>Movie</dt><dd>{selectedMovie.title}</dd></div>
              <div><dt>Showtime</dt><dd>{formatDateTime(selectedShowtime.startsAt)}</dd></div>
              <div><dt>Showroom</dt><dd>{selectedShowtime.showroomName}</dd></div>
              <div><dt>Seats</dt><dd>{selectedSeatObjects.map((seat) => seat.label).join(", ")}</dd></div>
              <div><dt>Adult tickets</dt><dd>{adultTickets} x ${Number(selectedShowtime.adultPrice).toFixed(2)}</dd></div>
              <div><dt>Child tickets</dt><dd>{childTickets} x ${Number(selectedShowtime.childPrice).toFixed(2)}</dd></div>
              <div><dt>Senior tickets</dt><dd>{seniorTickets} x ${Number(selectedShowtime.seniorPrice).toFixed(2)}</dd></div>
              <div><dt>Total before tax</dt><dd><strong>${subtotal.toFixed(2)}</strong></dd></div>
            </dl>
            <label className="checkout-email">
              Confirmation email *
              <input
                type="email"
                required
                value={checkoutEmail}
                onChange={(event) => setCheckoutEmail(event.target.value)}
                placeholder="you@example.com"
              />
            </label>
            {!authUser && <p className="form-guidance">You may choose seats as a guest. You will be asked to sign in before payment.</p>}
            <div>
              <button type="button" onClick={() => setBookingStep("seats")}>Back</button>
              <button
                type="button"
                disabled={bookingSubmitting}
                onClick={proceedToPayment}
              >
                {bookingSubmitting ? "Preparing Payment..." : "Continue to Payment"}
              </button>
            </div>
          </section>
        )}

        {bookingStep === "payment" && (
          <section className="booking-card">
            <h3>Payment</h3>
            <p><strong>Movie:</strong> {selectedMovie.title}</p>
            <p><strong>Showtime:</strong> {formatDateTime(selectedShowtime.startsAt)}</p>
            <p><strong>Seats:</strong> {selectedSeatObjects.map((seat) => seat.label).join(", ")}</p>
            <p><strong>Confirmation email:</strong> {checkoutEmail}</p>
            <p><strong>Subtotal:</strong> ${subtotal.toFixed(2)}</p>
            <p><strong>Tax (7%):</strong> ${(subtotal * 0.07).toFixed(2)}</p>
            <p className="payment-total"><strong>Total: ${(subtotal * 1.07).toFixed(2)}</strong></p>
            <fieldset>
              <legend>Payment Information</legend>
              {savedCards.length > 0 && (
                <label>
                  Payment card
                  <select
                    value={selectedPaymentCard}
                    onChange={(event) => setSelectedPaymentCard(event.target.value)}
                  >
                    {savedCards.map((card) => (
                      <option key={card.id} value={card.id}>
                        {card.cardType} ending in {card.lastFour}
                      </option>
                    ))}
                    <option value="new">Use a different card</option>
                  </select>
                </label>
              )}
              {selectedPaymentCard === "new" && (
                <>
                  <label>Card number<input autoComplete="cc-number" inputMode="numeric" value={cardNumber} onChange={(event) => setCardNumber(event.target.value)} placeholder="4242 4242 4242 4242" /></label>
                  <label>Expiration month<input type="number" min="1" max="12" value={expirationMonth} onChange={(event) => setExpirationMonth(event.target.value)} placeholder="MM" /></label>
                  <label>Expiration year<input type="number" min={new Date().getFullYear()} value={expirationYear} onChange={(event) => setExpirationYear(event.target.value)} placeholder="YYYY" /></label>
                </>
              )}
              <label>Security code<input autoComplete="cc-csc" inputMode="numeric" value={securityCode} onChange={(event) => setSecurityCode(event.target.value)} placeholder="CVV" /></label>
              {selectedPaymentCard !== "new" && (
                <small>For security, enter the CVV for your saved card.</small>
              )}
            </fieldset>
            <button type="button" onClick={() => setBookingStep("summary")}>Back to Summary</button>
            <button type="button" disabled={bookingSubmitting} onClick={() => void submitPayment()}>
              {bookingSubmitting ? "Processing..." : `Pay $${(subtotal * 1.07).toFixed(2)}`}
            </button>
          </section>
        )}

        {bookingStep === "confirmation" && confirmedBooking && (
          <section className="booking-card order-summary">
            <h3>Order Confirmed</h3>
            <p>A confirmation email has been sent to {checkoutEmail}.</p>
            <p><strong>Confirmation number:</strong> {confirmedBooking.confirmationNumber}</p>
            <p><strong>Movie:</strong> {selectedMovie.title}</p>
            <p><strong>Seats:</strong> {selectedSeatObjects.map((seat) => seat.label).join(", ")}</p>
            <p><strong>Subtotal:</strong> ${Number(confirmedBooking.subtotal).toFixed(2)}</p>
            <p><strong>Tax:</strong> ${Number(confirmedBooking.taxAmount).toFixed(2)}</p>
            <p><strong>Total paid:</strong> ${Number(confirmedBooking.totalAmount).toFixed(2)}</p>
            <button type="button" onClick={closeMovie}>Return to Movies</button>
            <button type="button" onClick={() => { closeMovie(); setShowOrders(true); }}>View My Orders</button>
          </section>
        )}
      </main>
    );
  }

  if (selectedMovie) {
    return (
      <main>
        <button type="button" onClick={closeMovie}>Back to Movies</button>
        <MovieDetails movie={selectedMovie} onSelectShowtime={(showtime) => void startBooking(selectedMovie, showtime)} />
      </main>
    );
  }

  const currentlyRunningMovies = movies.filter((movie) => movie.status === "CURRENTLY_PLAYING");
  const comingSoonMovies = movies.filter((movie) => movie.status === "COMING_SOON");

  const renderMovie = (movie: Movie) => (
    <article key={movie.id} className="movie-card">
      <img
        className="movie-poster"
        src={movie.posterUrl || "/placeholder-poster.svg"}
        alt={`${movie.title} poster`}
        onError={(event) => {
          event.currentTarget.onerror = null;
          event.currentTarget.src = "/placeholder-poster.svg";
        }}
      />
      <div className="movie-card-content">
        <h3 className="movie-title-row">
          <button type="button" className="movie-title-button" onClick={() => setSelectedMovie(movie)}>{movie.title}</button>
          <FavoriteButton
            isFavorite={favoriteIds.includes(movie.id)}
            disabled={favoriteLoadingIds.includes(movie.id)}
            onClick={() => toggleFavorite(movie.id)}
          />
        </h3>
        <p>{movie.category}</p>
        <p>{movie.synopsis}</p>
        <p>Status: {movie.status.toLowerCase().split("_").map((word) => word.charAt(0).toUpperCase() + word.slice(1)).join(" ")}</p>
        <button type="button" onClick={() => setSelectedMovie(movie)}>
          {movie.status === "CURRENTLY_PLAYING" ? "View Showtimes" : "View Details"}
        </button>
      </div>
    </article>
  );

  return (
    <main className="customer-portal">
      <header className="admin-topbar">
        <a className="admin-brand" href="/" aria-label="Cinema E-Booking home">
          <span className="admin-brand-mark" aria-hidden="true">C</span>
          <span>Cinema E-Booking</span>
        </a>

        <div className="admin-account">
          <button type="button" onClick={() => (authUser ? setShowProfile(true) : setShowLogin(true))}>
            {authUser ? "My Profile" : "Sign In"}
          </button>
          {authUser && (
            <button type="button" onClick={() => setShowOrders(true)}>My Orders</button>
          )}
          {authUser ? (
            <button className="button-secondary" type="button" onClick={() => void signOut()}>
              Logout
            </button>
          ) : (
            <button className="button-secondary" type="button" onClick={() => setShowRegistration(true)}>
              Register
            </button>
          )}
        </div>
      </header>

      <div className="customer-portal-content">
        <h1>Cinema E-Booking</h1>
        {authUser && <p className="customer-welcome">Welcome, {authUser.firstName}</p>}

        <section className="movie-filters">
          <input className="search-box" type="text" placeholder="Search movies by title" value={searchTerm} onChange={(event) => setSearchTerm(event.target.value)} />
          <select value={selectedGenre} onChange={(event) => setSelectedGenre(event.target.value)}>
            <option value="">All Genres</option>
            {MOVIE_CATEGORIES.map((genre) => (
              <option key={genre} value={genre}>{genre}</option>
            ))}
          </select>
        </section>

        {loading && <p>Loading movies...</p>}
        {error && <p role="alert">{error}</p>}
        {favoriteError && <p role="alert">{favoriteError}</p>}
        {!loading && !error && movies.length === 0 && <p>No movies match your search or filter.</p>}

        {currentlyRunningMovies.length > 0 && <section><h2>Currently Running</h2>{currentlyRunningMovies.map(renderMovie)}</section>}
        {comingSoonMovies.length > 0 && <section><h2>Coming Soon</h2>{comingSoonMovies.map(renderMovie)}</section>}
      </div>
    </main>
  );
}

export default App;
