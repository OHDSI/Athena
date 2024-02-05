CREATE TABLE vocabulary_release_version
(
    id INT,
    vocabulary_name VARCHAR(255) NOT NULL,
    athena_name  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);