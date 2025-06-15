CREATE USER server;

-- grant access to the database
GRANT CONNECT ON DATABASE holybook TO server;

-- let the role create objects in the public schema
GRANT USAGE, CREATE ON SCHEMA public TO server;

-- keep the existing table-level privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO server;