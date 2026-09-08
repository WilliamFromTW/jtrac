-- =============================================================================
-- JTrac Database Upgrade Script: 2.3.3-1.0.0 -> 2.3.3-2.0.0
-- Compatible with: MySQL, PostgreSQL, Microsoft SQL Server, Oracle, HSQLDB
-- =============================================================================
-- Summary of Changes:
-- 1. Adds default pagination configuration parameters for User and Space lists
--    ('users.list.pageSize' and 'spaces.list.pageSize') into the `config` table.
-- 2. Password column compatibility notice for BCrypt hashing upgrade.
-- 3. Embedded HSQLDB automatic migration notes.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Configuration Table Updates (`config`)
-- -----------------------------------------------------------------------------
-- Default page size for User Management List and Space Management List (default: 25 items).
-- Options in UI: 10, 25, 50, 100, -1 (All).

-- Standard ANSI SQL (Insert if not already present)
-- Note: If your database already has these keys, skip or ignore duplicate key errors.

-- MySQL / MariaDB:
-- INSERT IGNORE INTO config (param, value) VALUES ('users.list.pageSize', '25');
-- INSERT IGNORE INTO config (param, value) VALUES ('spaces.list.pageSize', '25');

-- PostgreSQL:
-- INSERT INTO config (param, value) VALUES ('users.list.pageSize', '25') ON CONFLICT (param) DO NOTHING;
-- INSERT INTO config (param, value) VALUES ('spaces.list.pageSize', '25') ON CONFLICT (param) DO NOTHING;

-- Microsoft SQL Server (T-SQL):
-- IF NOT EXISTS (SELECT 1 FROM config WHERE param = 'users.list.pageSize')
--     INSERT INTO config (param, value) VALUES ('users.list.pageSize', '25');
-- IF NOT EXISTS (SELECT 1 FROM config WHERE param = 'spaces.list.pageSize')
--     INSERT INTO config (param, value) VALUES ('spaces.list.pageSize', '25');

-- Oracle Database (PL/SQL / MERGE):
-- MERGE INTO config c USING (SELECT 'users.list.pageSize' AS param, '25' AS value FROM dual) src
-- ON (c.param = src.param)
-- WHEN NOT MATCHED THEN INSERT (param, value) VALUES (src.param, src.value);
-- MERGE INTO config c USING (SELECT 'spaces.list.pageSize' AS param, '25' AS value FROM dual) src
-- ON (c.param = src.param)
-- WHEN NOT MATCHED THEN INSERT (param, value) VALUES (src.param, src.value);

-- Standard fallback (Run directly if keys do not exist):
INSERT INTO config (param, value) VALUES ('users.list.pageSize', '25');
INSERT INTO config (param, value) VALUES ('spaces.list.pageSize', '25');

-- -----------------------------------------------------------------------------
-- 2. User Password Hash Upgrade Notice (`users.password`)
-- -----------------------------------------------------------------------------
-- In JTrac 2.3.3-2.0.0, the authentication framework is upgraded to Spring Security 5.8
-- with strong BCrypt hashing.
-- 
-- The `users.password` column must be able to hold 60-character BCrypt strings.
-- In default schemas, `password` is VARCHAR(255), which is sufficient.
-- If your legacy database restricted `password` to VARCHAR(32) for MD5, expand it:
--
-- MySQL / PostgreSQL / HSQLDB:
-- ALTER TABLE users ALTER COLUMN password TYPE VARCHAR(255);
-- Or MySQL:
-- ALTER TABLE users MODIFY COLUMN password VARCHAR(255);
-- Or SQL Server / Oracle:
-- ALTER TABLE users ALTER COLUMN password VARCHAR(255);
--
-- *Seamless Migration*:
-- DO NOT manually re-hash existing passwords in the database!
-- JTrac's built-in `JtracHybridPasswordEncoder` automatically detects legacy MD5 hashes
-- upon each user's successful login, transparently re-hashes their password into BCrypt,
-- and saves it to the database with zero downtime or user disruption.

-- -----------------------------------------------------------------------------
-- 3. Embedded HSQLDB 1.8 -> 2.x Migration
-- -----------------------------------------------------------------------------
-- If you are using the default embedded HSQLDB database (data/db/jtrac.*):
-- NO manual SQL execution is needed!
-- When starting JTrac 2.3.3-2.0.0, the built-in `HsqldbDatabaseMigrator` will:
-- 1. Automatically detect legacy HSQLDB 1.8 database files.
-- 2. Create a timestamped backup in data/db/backup-hsqldb-1.8-<timestamp>/.
-- 3. Migrate and re-write the database into modern HSQLDB 2.x format.
-- =============================================================================
