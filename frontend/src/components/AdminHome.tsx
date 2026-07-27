import { useState } from "react";
import AddMovieForm from "./AddMovieForm";
import ScheduleShowtimeForm from "./ScheduleShowtimeForm";

interface AdminHomeProps {
  name: string;
  onLogout: () => void;
}

type AdminView = "home" | "add-movie" | "showtimes";

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
    <main>
      <h1>Admin Portal</h1>
      <p>Welcome, {name}.</p>

      <nav className="admin-menu" aria-label="Admin menu">
        <button type="button" onClick={() => setView("add-movie")}>
          Manage Movies
        </button>

        <button type="button">Promotions</button>

        <button type="button">Users</button>

        <button type="button" onClick={() => setView("showtimes")}>
          Showtimes
        </button>
      </nav>

      <button type="button" onClick={onLogout}>
        Logout
      </button>
    </main>
  );
}