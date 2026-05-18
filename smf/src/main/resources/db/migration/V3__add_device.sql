CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS devices (
    id UUID PRIMARY KEY,
    device_id VARCHAR(255) UNIQUE NOT NULL,
    owner_id UUID NOT NULL,
    device_name VARCHAR(255) NOT NULL,
    last_location_lat DOUBLE PRECISION,
    last_location_lon DOUBLE PRECISION,
    last_seen_timestamp TIMESTAMP,
    
    CONSTRAINT fk_devices_owner 
        FOREIGN KEY (owner_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);
