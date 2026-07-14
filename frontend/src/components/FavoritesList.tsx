import type { Movie } from "../types/Movie";

interface Props{
    favorites:Movie[];
}

export default function FavoritesList({
    favorites
}:Props){

    return(

        <section>

            <h2>Favorite Movies</h2>

            {favorites.length===0 &&

                <p>No favorite movies.</p>

            }

            {favorites.map(movie=>

                <div key={movie.id}>

                    <h3>{movie.title}</h3>

                    <p>{movie.category}</p>

                </div>

            )}

        </section>

    );

}
