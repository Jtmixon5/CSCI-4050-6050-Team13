CREATE TABLE movies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    synopsis TEXT,
    cast_members TEXT,
    director VARCHAR(200),
    producer VARCHAR(200),
    mpaa_rating VARCHAR(20),
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_movies PRIMARY KEY (id)
);