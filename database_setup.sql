-- Region Plugin Database Setup
-- Run this script in your MySQL database to set up the required tables

-- Create the database (uncomment if you need to create it)
CREATE DATABASE IF NOT EXISTS regions;
USE regions;

-- Regions table - stores region data and coordinates
CREATE TABLE IF NOT EXISTS regions (
    name VARCHAR(64) PRIMARY KEY,
    owner VARCHAR(36) NOT NULL,
    world VARCHAR(64) NOT NULL,
    corner1_x INT NOT NULL,
    corner1_y INT NOT NULL,
    corner1_z INT NOT NULL,
    corner2_x INT NOT NULL,
    corner2_y INT NOT NULL,
    corner2_z INT NOT NULL,
    created_at BIGINT NOT NULL
);

-- Region whitelist table - stores player whitelist mappings
CREATE TABLE IF NOT EXISTS region_whitelist (
    region_name VARCHAR(64) NOT NULL,
    player_uuid VARCHAR(36) NOT NULL,
    PRIMARY KEY (region_name, player_uuid),
    FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE
);

-- Region flags table - stores flag state configurations
CREATE TABLE IF NOT EXISTS region_flags (
    region_name VARCHAR(64) NOT NULL,
    flag_name VARCHAR(32) NOT NULL,
    flag_state VARCHAR(16) NOT NULL,
    PRIMARY KEY (region_name, flag_name),
    FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE
);



-- Show the created tables
SHOW TABLES; 