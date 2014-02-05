CREATE TABLE params (
  name TEXT,
  value TEXT
);
CREATE TABLE series (
  id INTEGER PRIMARY KEY,
  series_name varchar(255) DEFAULT NULL,
  series_path varchar(255) DEFAULT NULL
);
CREATE TABLE medias (
  id INTEGER PRIMARY KEY,
  type VARCHAR(255) NOT NULL,
  filename VARCHAR(255) DEFAULT (NULL),
  name VARCHAR(255) DEFAULT (NULL),
  jacket VARCHAR(255) DEFAULT (NULL),
  id_series INT(11) DEFAULT (NULL),
  numepisode INT(11) DEFAULT (NULL),
  numseason INT(11) DEFAULT (NULL),
  FOREIGN KEY (id_series) REFERENCES series(id)
);
CREATE TABLE fields (
    id_series INT(11) DEFAULT (NULL),
    id_media INT(11) DEFAULT (NULL),
    name VARCHAR(255) NOT NULL,
    value TEXT,
    PRIMARY KEY(id_series, id_media, name),
    FOREIGN KEY(id_series) REFERENCES series(id) ON DELETE CASCADE,
    FOREIGN KEY(id_media) REFERENCES medias(id) ON DELETE CASCADE
);
CREATE TABLE role (
    id_media INTEGER NOT NULL,
    id_actor INTEGER NOT NULL,
    description TEXT,
    FOREIGN KEY(id_media) REFERENCES medias(id) ON DELETE CASCADE,
    FOREIGN KEY(id_actor) REFERENCES actors(id) ON DELETE CASCADE
);
CREATE TABLE actors (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    actor_name TEXT NOT NULL
);