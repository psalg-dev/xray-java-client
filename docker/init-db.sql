-- Xray DB (only used with --profile pro)
CREATE DATABASE xray;
CREATE USER xray WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE xray TO xray;
