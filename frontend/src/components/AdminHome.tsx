import { useState } from "react";
import AddMovieForm from "./AddMovieForm";
import ScheduleShowtimeForm from "./ScheduleShowtimeForm";

interface AdminHomeProps {
  name: string;
  onLogout: () => void;
}

type AdminView = "home" | "add-movie" | "showtimes";

function AdminIcon({ type }: { type: "movie" | "promotion" | "users" | "showtime" }) {
  const paths = {
    movie: (
      <>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M3 10h18M8 5l2 5m4-5 2 5" />
      </>
    ),
    promotion: (
      <>
        <path d="m4 13 2 7 4-1-2-6" />
        <path d="M7 13h4l8 4V5l-8 4H7a2 2 0 0 0-2 2 2 2 0 0 0 2 2Z" />
      </>
    ),
    users: (
      <>
        <circle cx="9" cy="8" r="3" />
        <circle cx="17" cy="9" r="2" />
        <path d="M3 20a6 6 0 0 1 12 0m0-5a5 5 0 0 1 6 5" />
      </>
    ),
    showtime: (
      <>
        <rect x="3" y="5" width="18" height="16" rx="2" />
        <path d="M8 3v4m8-4v4M3 10h18" />
        <circle cx="12" cy="15" r="3" />
        <path d="M12 13.5V15l1 1" />
      </>
    ),
  };

  return (
    <svg
      className="admin-card-icon"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[type]}
    </svg>
  );
}

export default function AdminHome({
  name,
  onLogout,
}: AdminHomeProps) {
  const [view, setView] = useState<AdminView>("home");

  if (view === "add-movie") {
    return <AddMovieForm onBack={() => setView("home")} />;
  }

  if (view === "showtimes") {
    return <ScheduleShowtimeForm onBack={() => setView("home")} />;
  }

  return (
    <main className="admin-portal">
      <header className="admin-topbar">
        <a className="admin-brand" href="/" aria-label="Cinema E-Booking admin home">
          <span className="admin-brand-mark" aria-hidden="true">C</span>
          <span>Cinema E-Booking</span>
        </a>

        <div className="admin-account">
          <span className="admin-role-badge">Administrator</span>
          <button className="button-secondary" type="button" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      <section className="admin-hero">
        <p className="admin-eyebrow">Management dashboard</p>
        <h1>Welcome back, {name}</h1>
        <p>
          Keep the cinema catalog and screening schedule ready for moviegoers.
        </p>
      </section>

      <section className="admin-dashboard" aria-labelledby="admin-tools-heading">
        <div className="admin-section-heading">
          <div>
            <p className="admin-eyebrow">Admin tools</p>
            <h2 id="admin-tools-heading">What would you like to manage?</h2>
          </div>
        </div>

        <nav className="admin-menu" aria-label="Admin menu">
          <button
            className="admin-menu-card"
            type="button"
            onClick={() => setView("add-movie")}
          >
            <AdminIcon type="movie" />
            <span className="admin-menu-copy">
              <strong>Manage Movies</strong>
              <small>Add a movie and publish it to the customer catalog.</small>
            </span>
            <span className="admin-card-arrow" aria-hidden="true">-&gt;</span>
          </button>

          <button
            className="admin-menu-card"
            type="button"
            onClick={() => setView("showtimes")}
          >
            <AdminIcon type="showtime" />
            <span className="admin-menu-copy">
              <strong>Manage Showtimes</strong>
              <small>Schedule movies, showrooms, times, and ticket prices.</small>
            </span>
            <span className="admin-card-arrow" aria-hidden="true">-&gt;</span>
          </button>

          <button className="admin-menu-card is-disabled" type="button" disabled>
            <AdminIcon type="promotion" />
            <span className="admin-menu-copy">
              <strong>Manage Promotions</strong>
              <small>Create offers for subscribed customers.</small>
            </span>
            <span className="admin-coming-soon">Coming later</span>
          </button>

          <button className="admin-menu-card is-disabled" type="button" disabled>
            <AdminIcon type="users" />
            <span className="admin-menu-copy">
              <strong>Manage Users</strong>
              <small>Review customer and administrator accounts.</small>
            </span>
            <span className="admin-coming-soon">Coming later</span>
          </button>
        </nav>
      </section>
    </main>
  );
}
