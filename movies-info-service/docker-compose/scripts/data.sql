CREATE TABLE movie_info (
  movie_info_id serial PRIMARY KEY,
  name varchar NOT NULL,
  year int NOT NULL,
  actors varchar[] NOT NULL,
  release_date DATE NOT NULL
);

CREATE TABLE review (
  review_id serial PRIMARY KEY,
  movie_info_id bigint NOT NULL,
  comment varchar NOT NULL,
  rating double PRECISION NOT NULL
);