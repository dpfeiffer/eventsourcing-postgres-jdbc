create table if not exists journal(
    id BIGSERIAL PRIMARY KEY, 
    aggregate_id VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    tags JSONB NOT NULL,
    timestamp TIMESTAMP NOT NULL
);