import { useEffect, useState } from "react";
import type { Movie } from "../types/Movie";
import { getShowtimes } from "../api/showtimes";
import type { Showtime } from "../api/showtimes";

interface MovieDetailsProps {
  movie: Movie;
  onSelectShowtime?: (showtime: Showtime) => void;
}

const toYoutubeEmbed = (url: string): string => {
  try {
    const parsedUrl = new URL(url);

    if (parsedUrl.hostname.includes("youtu.be")) {
      const videoId = parsedUrl.pathname.replace("/", "");
      return videoId ? `https://www.youtube.com/embed/${videoId}` : url;
    }

    if (parsedUrl.hostname.includes("youtube.com")) {
      const videoId = parsedUrl.searchParams.get("v");
      if (videoId) return `https://www.youtube.com/embed/${videoId}`;
      if (parsedUrl.pathname.startsWith("/embed/")) return url;
    }
  } catch {
    // Use the original URL when it cannot be parsed.
  }

  return url;
};

const formatDateTime = (value: string): string =>
  new Intl.DateTimeFormat("en-US", {
    weekday: "short",
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(value));

export default function MovieDetails({
  movie,
  onSelectShowtime,
}: MovieDetailsProps) {
  const [showtimes, setShowtimes] = useState<Showtime[]>([]);
  const [loadingShowtimes, setLoadingShowtimes] = useState(false);
  const [showtimeError, setShowtimeError] = useState<string | null>(null);

  useEffect(() => {
    async function loadShowtimes() {
      if (movie.status !== "CURRENTLY_PLAYING") {
        setShowtimes([]);
        return;
      }

      try {
        setLoadingShowtimes(true);
        setShowtimeError(null);
        setShowtimes(await getShowtimes(movie.id));
      } catch {
        setShowtimeError("Unable to load showtimes for this movie.");
      } finally {
        setLoadingShowtimes(false);
      }
    }

    void loadShowtimes();
  }, [movie.id, movie.status]);

  const formattedStatus = movie.status
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

  const trailerUrl = movie.trailerUrl;
  const isYoutube =
    trailerUrl?.includes("youtube.com") || trailerUrl?.includes("youtu.be");

  return (
    <div className="movie-details">
      <img
        src={movie.posterUrl || "/placeholder-poster.svg"}
        alt={`${movie.title} poster`}
        onError={(event) => {
          event.currentTarget.onerror = null;
          event.currentTarget.src = "/placeholder-poster.svg";
        }}
        className="movie-details-poster"
      />

      <div>
        <h2>{movie.title}</h2>
        <p><strong>Genre:</strong> {movie.category}</p>
        <p><strong>Status:</strong> {formattedStatus}</p>
        {movie.mpaaRating && <p><strong>Rating:</strong> {movie.mpaaRating}</p>}

        <h3>Description</h3>
        <p>{movie.synopsis || "No description is currently available."}</p>

        {movie.status === "CURRENTLY_PLAYING" && (
          <section>
            <h3>Available Showtimes</h3>
            {loadingShowtimes && <p>Loading showtimes...</p>}
            {showtimeError && <p role="alert">{showtimeError}</p>}
            {!loadingShowtimes && !showtimeError && showtimes.length === 0 && (
              <p>No upcoming showtimes are currently scheduled.</p>
            )}
            <div className="showtime-list">
              {showtimes.map((showtime) => (
                <button
                  key={showtime.id}
                  type="button"
                  className="showtime-option"
                  onClick={() => onSelectShowtime?.(showtime)}
                >
                  <span>{formatDateTime(showtime.startsAt)}</span>
                  <small>{showtime.showroomName}</small>
                </button>
              ))}
            </div>
          </section>
        )}

        <section className="trailer-section">
          <h3>Trailer</h3>
          {trailerUrl ? (
            <div className="trailer-frame">
              {isYoutube ? (
                <iframe
                  src={toYoutubeEmbed(trailerUrl)}
                  title={`${movie.title} trailer`}
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                />
              ) : trailerUrl.toLowerCase().endsWith(".mp4") ? (
                <video src={trailerUrl} controls title={`${movie.title} trailer`} />
              ) : (
                <iframe src={trailerUrl} title={`${movie.title} trailer`} allowFullScreen />
              )}
            </div>
          ) : (
            <p>No trailer is currently available.</p>
          )}
        </section>
      </div>
    </div>
  );
}
