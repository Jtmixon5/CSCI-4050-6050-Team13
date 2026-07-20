import React from "react";
import type { Movie } from "../types/Movie";

interface MovieDetailsProps {
  movie: Movie;
  onSelectShowtime?: (showtime: string) => void;
}

const showtimes = ["2:00 PM", "5:00 PM", "8:00 PM"];

const toYoutubeEmbed = (url: string): string => {
  try {
    const parsedUrl = new URL(url);

    if (parsedUrl.hostname.includes("youtu.be")) {
      const videoId = parsedUrl.pathname.replace("/", "");

      if (videoId) {
        return `https://www.youtube.com/embed/${videoId}`;
      }
    }

    if (parsedUrl.hostname.includes("youtube.com")) {
      const videoId = parsedUrl.searchParams.get("v");

      if (videoId) {
        return `https://www.youtube.com/embed/${videoId}`;
      }

      if (parsedUrl.pathname.startsWith("/embed/")) {
        return url;
      }
    }
  } catch {
    // Return the original URL when it cannot be parsed.
  }

  return url;
};

const isYoutubeUrl = (url: string): boolean => {
  return url.includes("youtube.com") || url.includes("youtu.be");
};

const isVideoFile = (url: string): boolean => {
  return url.toLowerCase().endsWith(".mp4");
};

export const MovieDetails: React.FC<MovieDetailsProps> = ({
  movie,
  onSelectShowtime,
}) => {
  const {
    title,
    synopsis,
    posterUrl,
    trailerUrl,
    mpaaRating,
    category,
    status,
  } = movie;

  const formattedStatus = status
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

  const selectShowtime = (showtime: string) => {
    if (onSelectShowtime) {
      onSelectShowtime(showtime);
      return;
    }

    window.alert(`Selected showtime: ${showtime}`);
  };

  return (
    <div
      style={{
        maxWidth: 900,
        margin: "24px auto",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 20,
        }}
      >
        <img
          src={posterUrl || "/placeholder-poster.svg"}
          alt={`${title} poster`}
          onError={(event) => {
            event.currentTarget.onerror = null;
            event.currentTarget.src = "/placeholder-poster.svg";
          }}
          style={{
            width: 260,
            height: 380,
            objectFit: "cover",
            borderRadius: 6,
            margin: "0 auto",
          }}
        />

        <div
          style={{
            flex: 1,
            textAlign: "left",
          }}
        >
          <h2 style={{ margin: "8px 0" }}>{title}</h2>

          <p style={{ margin: "0 0 8px" }}>
            <strong>Genre:</strong> {category}
          </p>

          <p style={{ margin: "0 0 8px" }}>
            <strong>Status:</strong> {formattedStatus}
          </p>

          {mpaaRating && (
            <p style={{ margin: "0 0 12px" }}>
              <strong>Rating:</strong> {mpaaRating}
            </p>
          )}

          <h3
            style={{
              margin: "16px 0 8px",
              fontSize: 16,
            }}
          >
            Description
          </h3>

          <p style={{ marginTop: 0 }}>
            {synopsis || "No description is currently available."}
          </p>

          {status === "CURRENTLY_PLAYING" && (
            <>
              <h3
                style={{
                  marginTop: 12,
                  marginBottom: 8,
                  fontSize: 16,
                }}
              >
                Showtimes
              </h3>

              <div
                style={{
                  display: "flex",
                  flexWrap: "wrap",
                  gap: 8,
                  marginBottom: 12,
                }}
              >
                {showtimes.map((showtime) => (
                  <button
                    key={showtime}
                    type="button"
                    onClick={() => selectShowtime(showtime)}
                  >
                    {showtime}
                  </button>
                ))}
              </div>
            </>
          )}

          <div style={{ marginTop: 20 }}>
            <h3
              style={{
                marginBottom: 8,
                fontSize: 16,
              }}
            >
              Trailer
            </h3>

            {trailerUrl ? (
              <div
                style={{
                  position: "relative",
                  paddingBottom: "56.25%",
                  height: 0,
                  overflow: "hidden",
                  borderRadius: 8,
                }}
              >
                {isYoutubeUrl(trailerUrl) ? (
                  <iframe
                    src={toYoutubeEmbed(trailerUrl)}
                    title={`${title} trailer`}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                    style={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      width: "100%",
                      height: "100%",
                      border: 0,
                    }}
                  />
                ) : isVideoFile(trailerUrl) ? (
                  <video
                    src={trailerUrl}
                    controls
                    title={`${title} trailer`}
                    style={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      width: "100%",
                      height: "100%",
                    }}
                  />
                ) : (
                  <iframe
                    src={trailerUrl}
                    title={`${title} trailer`}
                    allowFullScreen
                    style={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      width: "100%",
                      height: "100%",
                      border: 0,
                    }}
                  />
                )}
              </div>
            ) : (
              <p>No trailer is currently available.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default MovieDetails;
