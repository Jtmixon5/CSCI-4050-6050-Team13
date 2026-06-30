import React from "react";
import type { Movie } from "../types/Movie";

interface MovieDetailsProps {
  movie: Movie & {
    posterUrl?: string;
    rating?: string | number;
    trailerUrl?: string;
  };
  onSelectShowtime?: (showtime: string) => void;
}

const showtimes = ["2:00 PM", "5:00 PM", "8:00 PM"];

export const MovieDetails: React.FC<MovieDetailsProps> = ({ movie, onSelectShowtime }) => {
  const { title, synopsis, posterUrl, trailerUrl } = movie as any;
  const rating = (movie as any).mpaaRating ?? (movie as any).rating;

  // No external fallback links provided; trailers are embedded when available.

  const toYoutubeEmbed = (url: string) => {
    try {
      const u = new URL(url);
      if (u.hostname.includes("youtu.be")) {
        return `https://www.youtube.com/embed${u.pathname}`;
      }
      if (u.hostname.includes("youtube.com")) {
        const v = u.searchParams.get("v");
        if (v) return `https://www.youtube.com/embed/${v}`;
      }
    } catch (e) {
      // ignore and fall through
    }
    return url;
  };

  return (
    <div style={{ maxWidth: 900, margin: "24px auto", fontFamily: "Arial, sans-serif" }}>
      <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
        <img
          src={posterUrl || "/public/placeholder-poster.png"}
          alt={`${title} poster`}
          style={{ width: 260, height: 380, objectFit: "cover", borderRadius: 6, margin: "0 auto" }}
        />

        <div style={{ flex: 1, textAlign: "left" }}>
          <h2 style={{ margin: "8px 0" }}>{title}</h2>
          {rating !== undefined && (
            <p style={{ margin: "0 0 12px", fontWeight: 600 }}>Rating: {rating}</p>
          )}

          <h3 style={{ margin: "0 0 8px", fontSize: 16 }}>Description</h3>
          <p style={{ marginTop: 0 }}>{synopsis || "No description available."}</p>

          <h3 style={{ marginTop: 12, marginBottom: 8, fontSize: 16 }}>Showtimes</h3>
          <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
            {showtimes.map((t) => (
              <button
                key={t}
                onClick={() =>
                  onSelectShowtime ? onSelectShowtime(t) : alert(`Selected showtime: ${t}`)
                }
              >
                {t}
              </button>
            ))}
          </div>

          <div style={{ marginTop: 12 }}>
            {trailerUrl ? (
              <div style={{ position: "relative", paddingBottom: "56.25%", height: 0, overflow: "hidden", borderRadius: 8 }}>
                {trailerUrl.includes("youtube.com") || trailerUrl.includes("youtu.be") ? (
                  <iframe
                    src={toYoutubeEmbed(trailerUrl)}
                    frameBorder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    title={`${title} trailer`}
                    style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%" }}
                  />
                ) : trailerUrl.endsWith(".mp4") ? (
                  <video
                    src={trailerUrl}
                    controls
                    style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%" }}
                  />
                ) : (
                  <iframe
                    src={trailerUrl}
                    frameBorder="0"
                    allowFullScreen
                    title={`${title} trailer`}
                    style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%" }}
                  />
                )}
              </div>
              ) : (
              <p>No trailer available.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MovieDetails;
