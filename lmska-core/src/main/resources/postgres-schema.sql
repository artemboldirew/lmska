CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tasks (
    id UUID PRIMARY KEY,
    category_id UUID REFERENCES categories(id),
    title VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS task_attempts (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    task_id UUID REFERENCES tasks(id),
    answer VARCHAR(255),
    status VARCHAR(255),
    score FLOAT8,
    startedat BIGINT,
    completedat BIGINT
);
