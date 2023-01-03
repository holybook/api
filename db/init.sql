CREATE USER server;

DROP TABLE paragraphs;
DROP TABLE translations;
DROP TABLE books;

CREATE TABLE books (
    id VARCHAR(32) NOT NULL,
    author VARCHAR(256) NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE translations (
    book VARCHAR(32) NOT NULL,
    language VARCHAR(3) NOT NULL,
    title VARCHAR(512) NOT NULL,
    last_modified TIMESTAMP NOT NULL,

    PRIMARY KEY (book, language),
    FOREIGN KEY (book) REFERENCES books(id)
);

CREATE TABLE paragraphs (
    book VARCHAR(32) NOT NULL,
    language VARCHAR(3) NOT NULL,
    index INT NOT NULL,
    type VARCHAR(64) NOT NULL,
    text TEXT NOT NULL,

    PRIMARY KEY (book, language, index),
    FOREIGN KEY (book) REFERENCES books(id)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO server;