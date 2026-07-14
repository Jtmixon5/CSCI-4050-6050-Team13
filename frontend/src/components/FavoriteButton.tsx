interface Props{
    isFavorite:boolean;
    onClick:()=>void;
}

export default function FavoriteButton({
    isFavorite,
    onClick
}:Props){

    return(
        <button
            title={
                isFavorite
                    ? "Remove Favorite"
                    : "Add Favorite"
            }
            onClick={onClick}
            style={{
                border:"none",
                background:"transparent",
                cursor:"pointer",
                fontSize:"24px"
            }}
        >
            {isFavorite ? "❤️" : "🤍"}
        </button>
    );
}
