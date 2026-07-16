interface FavoriteButtonProps {
  isFavorite: boolean;
  disabled?: boolean;
  onClick: () => void | Promise<void>;
}

export default function FavoriteButton({
  isFavorite,
  disabled = false,
  onClick,
}: FavoriteButtonProps) {
  return (
    <button
      type="button"
      aria-label={
        isFavorite
          ? "Remove from favorites"
          : "Add to favorites"
      }
      title={
        isFavorite
          ? "Remove from favorites"
          : "Add to favorites"
      }
      disabled={disabled}
      onClick={(event) => {
        event.stopPropagation();
        void onClick();
      }}
      style={{
        border: "none",
        background: "transparent",
        cursor: disabled ? "not-allowed" : "pointer",
        fontSize: "24px",
        padding: "2px",
        opacity: disabled ? 0.6 : 1,
      }}
    >
      {isFavorite ? "❤️" : "🤍"}
    </button>
  );
}
