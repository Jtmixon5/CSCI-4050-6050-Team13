import { useEffect, useState, type FormEvent } from "react";
import axios from "axios";
import { getMovies } from "../api/movies";
import {
  createShowtime,
  getShowrooms,
  type CreateShowtimeInput,
  type Showroom,
} from "../api/showtimes";
import type { Movie } from "../types/Movie";

interface ScheduleShowtimeFormProps {
  onBack: () => void;
}

const initialShowtime: CreateShowtimeInput = {
  movieId: 0,
  showroomId: 0,
  startsAt: "",
  endsAt: "",
  adultPrice: 12,
  childPrice: 8,
  seniorPrice: 9,
};

export default function ScheduleShowtimeForm({
  onBack,
}: ScheduleShowtimeFormProps) {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [showrooms, setShowrooms] = useState<Showroom[]>([]);
  const [showtime, setShowtime] =
    useState<CreateShowtimeInput>(initialShowtime);
  const [showDate, setShowDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    const loadOptions = async () => {
      try {
        setLoading(true);
        setError(null);

        const [movieResults, showroomResults] = await Promise.all([
          getMovies(),
          getShowrooms(),
        ]);

        setMovies(movieResults);
        setShowrooms(
          showroomResults.filter((showroom) => showroom.active),
        );
      } catch {
        setError("Unable to load movies and showrooms.");
      } finally {
        setLoading(false);
      }
    };

    void loadOptions();
  }, []);

  const update = <K extends keyof CreateShowtimeInput>(
    field: K,
    value: CreateShowtimeInput[K],
  ) => {
    setShowtime((current) => ({
      ...current,
      [field]: value,
    }));
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);

    const startsAt = `${showDate}T${startTime}`;
    const endsAt = `${showDate}T${endTime}`;

    if (!showDate || !startTime || !endTime) {
      setError("Select a date, starting time, and ending time.");
      setSubmitting(false);
      return;
    }

    if (new Date(endsAt) <= new Date(startsAt)) {
      setError("The ending time must be later than the starting time.");
      setSubmitting(false);
      return;
    }

    try {
      const savedShowtime = await createShowtime({
        ...showtime,
        startsAt,
        endsAt,
      });

      setSuccess(
        `${savedShowtime.movieTitle} was scheduled in ` +
          `${savedShowtime.showroomName}.`,
      );

      setShowtime(initialShowtime);
      setShowDate("");
      setStartTime("");
      setEndTime("");
    } catch (requestError) {
      if (axios.isAxiosError(requestError)) {
        if (requestError.response?.status === 403) {
          setError(
            "The request was denied. Confirm that you are signed in as an administrator.",
          );
        } else if (requestError.response?.status === 409) {
          setError(
            "That showroom already has a movie scheduled during this time.",
          );
        } else if (requestError.response?.status === 400) {
          setError("Check the dates, prices, movie, and showroom.");
        } else {
          setError("Unable to schedule the showtime. Please try again.");
        }
      } else {
        setError("Unable to schedule the showtime. Please try again.");
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
        <h1>Schedule Showtime</h1>

        <p className="form-guidance">
          Choose a movie, showroom, time, and ticket prices.
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

        {loading ? (
          <p>Loading movies and showrooms...</p>
        ) : (
          <form
            className="registration-form"
            onSubmit={(event) => void submit(event)}
          >
            <label htmlFor="showtime-movie">Movie *</label>
            <select
              id="showtime-movie"
              value={showtime.movieId}
              required
              onChange={(event) =>
                update("movieId", Number(event.target.value))
              }
            >
              <option value={0}>Select a movie</option>

              {movies.map((movie) => (
                <option key={movie.id} value={movie.id}>
                  {movie.title}
                </option>
              ))}
            </select>

            <label htmlFor="showtime-showroom">Showroom *</label>
            <select
              id="showtime-showroom"
              value={showtime.showroomId}
              required
              onChange={(event) =>
                update("showroomId", Number(event.target.value))
              }
            >
              <option value={0}>Select a showroom</option>

              {showrooms.map((showroom) => (
                <option key={showroom.id} value={showroom.id}>
                  {showroom.name} — {showroom.capacity} seats
                </option>
              ))}
            </select>

            <fieldset className="schedule-time-fields">
              <legend>Date and time</legend>

              <label htmlFor="showtime-date">Show date *</label>
              <input
                id="showtime-date"
                type="date"
                value={showDate}
                required
                onChange={(event) => setShowDate(event.target.value)}
              />

              <div className="schedule-time-grid">
                <div>
                  <label htmlFor="showtime-start">Starting time *</label>
                  <input
                    id="showtime-start"
                    type="time"
                    value={startTime}
                    required
                    onChange={(event) => setStartTime(event.target.value)}
                  />
                </div>

                <div>
                  <label htmlFor="showtime-end">Ending time *</label>
                  <input
                    id="showtime-end"
                    type="time"
                    value={endTime}
                    required
                    onChange={(event) => setEndTime(event.target.value)}
                  />
                </div>
              </div>
            </fieldset>

            <label htmlFor="adult-price">Adult ticket price *</label>
            <input
              id="adult-price"
              type="number"
              min="0"
              step="0.01"
              value={showtime.adultPrice}
              required
              onChange={(event) =>
                update("adultPrice", Number(event.target.value))
              }
            />

            <label htmlFor="child-price">Child ticket price *</label>
            <input
              id="child-price"
              type="number"
              min="0"
              step="0.01"
              value={showtime.childPrice}
              required
              onChange={(event) =>
                update("childPrice", Number(event.target.value))
              }
            />

            <label htmlFor="senior-price">Senior ticket price *</label>
            <input
              id="senior-price"
              type="number"
              min="0"
              step="0.01"
              value={showtime.seniorPrice}
              required
              onChange={(event) =>
                update("seniorPrice", Number(event.target.value))
              }
            />

            <button
              type="submit"
              disabled={
                submitting ||
                showtime.movieId === 0 ||
                showtime.showroomId === 0
              }
            >
              {submitting ? "Scheduling..." : "Schedule Showtime"}
            </button>
          </form>
        )}
      </section>
    </main>
  );
}
