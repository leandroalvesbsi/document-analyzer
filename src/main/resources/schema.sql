-- User table
CREATE TABLE app_user
(
    email             VARCHAR(255) PRIMARY KEY,
    registration_date TIMESTAMP NOT NULL,
    active     BOOLEAN   NOT NULL DEFAULT TRUE
);

-- Team table
CREATE TABLE team
(
    id IDENTITY PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL UNIQUE
);

-- User-Team relationship
CREATE TABLE user_team
(
    user_team_id IDENTITY PRIMARY KEY,
    user_email VARCHAR(255) REFERENCES app_user (email),
    team_id    INT REFERENCES team (id),
    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date   TIMESTAMP,
    active     BOOLEAN   NOT NULL DEFAULT TRUE
);

-- Document table
CREATE TABLE document
(
    id IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL REFERENCES app_user(email),
    upload_timestamp TIMESTAMP NOT NULL,
    s3_object_key VARCHAR(255) NOT NULL
);

CREATE TABLE document_statistics
(
    id IDENTITY PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES document(id),
    word_count INT NOT NULL,
    shortest_word VARCHAR(255),
    longest_word VARCHAR(255)
);

CREATE TABLE word_frequency
(
    id IDENTITY PRIMARY KEY,
    document_statistics_id BIGINT NOT NULL REFERENCES document_statistics(id),
    word VARCHAR(255) NOT NULL,
    frequency INT NOT NULL
);

