-- Sql to be executed from favorite db client (pgadmin for example)
-- Schema creation
-- Create schema
CREATE SCHEMA IF NOT EXISTS bank AUTHORIZATION bank007;
GRANT ALL ON SCHEMA bank TO bank007;

-- -- Drop tables in correct order to avoid FK conflicts
-- DROP TABLE IF EXISTS bank.transaction CASCADE;
-- DROP TABLE IF EXISTS bank.category CASCADE;
-- DROP TABLE IF EXISTS bank.wallet CASCADE;
-- DROP TABLE IF EXISTS bank.currency CASCADE;
-- DROP TABLE IF EXISTS bank.configuration CASCADE;
-- DROP TABLE IF EXISTS bank.user CASCADE;
--
-- Drop sequences
-- DROP SEQUENCE IF EXISTS bank.seq_transaction CASCADE;
-- DROP SEQUENCE IF EXISTS bank.seq_category CASCADE;
-- DROP SEQUENCE IF EXISTS bank.seq_wallet CASCADE;
-- DROP SEQUENCE IF EXISTS bank.seq_currency CASCADE;
-- DROP SEQUENCE IF EXISTS bank.seq_configuration CASCADE;
-- DROP SEQUENCE IF EXISTS bank.seq_user CASCADE;

-- Create sequences first (so defaults can use them)
CREATE SEQUENCE IF NOT EXISTS bank.seq_user
    START 1000
    INCREMENT 1;
ALTER SEQUENCE bank.seq_user OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_configuration START 1000;
ALTER SEQUENCE bank.seq_configuration OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_currency START 1000;
ALTER SEQUENCE bank.seq_currency OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_wallet START 1000;
ALTER SEQUENCE bank.seq_wallet OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_category START 1000;
ALTER SEQUENCE bank.seq_category OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_transaction START 1000;
ALTER SEQUENCE bank.seq_transaction OWNER TO bank007;

CREATE SEQUENCE IF NOT EXISTS bank.seq_goal START 1000;
ALTER SEQUENCE bank.seq_goal OWNER TO bank007;

-- Create user table
CREATE TABLE IF NOT EXISTS bank.user (
   id bigint NOT NULL DEFAULT nextval('bank.seq_user'::regclass),
   version integer NOT NULL DEFAULT 0,
   birth_date timestamptz NOT NULL,
   blocked boolean NOT NULL DEFAULT false,
   email varchar(50) NOT NULL UNIQUE,
   role varchar(10) NOT NULL,
   user_name varchar(50) NOT NULL UNIQUE,
   password varchar(250) NOT NULL,
   profile_picture varchar(100) NOT NULL,
   first_name varchar(50) NOT NULL,
   last_name varchar(50) NOT NULL,
   created timestamptz NOT NULL,
   modified timestamptz NOT NULL,
   admin_note varchar(300),
   CONSTRAINT user_pkey PRIMARY KEY (id)
);
ALTER TABLE bank.user OWNER TO bank007;
GRANT ALL ON TABLE bank.user TO bank007;

-- Create configuration table
CREATE TABLE IF NOT EXISTS bank.configuration (
    id bigint NOT NULL DEFAULT nextval('bank.seq_configuration'::regclass),
    version integer NOT NULL DEFAULT 0,
    created timestamptz NOT NULL,
    modified timestamptz,
    name varchar(100) NOT NULL,
    config_value text NOT NULL,
    user_id bigint,
    CONSTRAINT configuration_pkey PRIMARY KEY (id),
    CONSTRAINT configuration_user_fkey FOREIGN KEY (user_id) REFERENCES bank.user (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.configuration OWNER TO bank007;
GRANT ALL ON TABLE bank.configuration TO bank007;

-- Create currency table
CREATE TABLE IF NOT EXISTS bank.currency (
   id bigint NOT NULL DEFAULT nextval('bank.seq_currency'::regclass),
   version integer NOT NULL DEFAULT 0,
   created timestamptz NOT NULL,
   modified timestamptz,
   name varchar(100) NOT NULL,
   currency_value numeric(10,2) NOT NULL,
   CONSTRAINT currency_pkey PRIMARY KEY (id)
);
ALTER TABLE bank.currency OWNER TO bank007;
GRANT ALL ON TABLE bank.currency TO bank007;

ALTER TABLE bank.currency DROP CONSTRAINT IF EXISTS uq_currency_name;

ALTER TABLE bank.currency
    ADD CONSTRAINT uq_currency_name UNIQUE(name);

-- Seed currency
INSERT INTO bank.currency (name, currency_value, version, created)
VALUES
    ('USD', 1.00, 0, NOW()),
    ('EUR', 0.90, 0, NOW()),
    ('GBP', 0.78, 0, NOW()),
    ('RSD', 105.00, 0, NOW()),
    ('JPY', 130.00, 0, NOW())
ON CONFLICT (name) DO NOTHING;

-- Create wallet table
CREATE TABLE IF NOT EXISTS bank.wallet (
     id bigint NOT NULL DEFAULT nextval('bank.seq_wallet'::regclass),
     version integer NOT NULL DEFAULT 0,
     created timestamptz NOT NULL,
     modified timestamptz,
     name varchar(100) NOT NULL,
     init_amount numeric(10,2) NOT NULL,
     current_amount numeric(10,2) NOT NULL,
     user_id bigint,
     currency_id bigint,
     savings boolean NOT NULL,
     archived boolean NOT NULL,
     CONSTRAINT wallet_pkey PRIMARY KEY (id),
     CONSTRAINT wallet_user_fkey FOREIGN KEY (user_id) REFERENCES bank.user(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
     CONSTRAINT wallet_currency_fkey FOREIGN KEY (currency_id) REFERENCES bank.currency(id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.wallet OWNER TO bank007;
GRANT ALL ON TABLE bank.wallet TO bank007;

-- Create category table
CREATE TABLE IF NOT EXISTS bank.category (
   id bigint NOT NULL DEFAULT nextval('bank.seq_category'::regclass),
   version integer NOT NULL DEFAULT 0,
   transaction_type varchar (10) NOT NULL,
   created timestamptz NOT NULL,
   modified timestamptz,
   user_id bigint,
   role varchar(10) NOT NULL,
   predefined boolean NOT NULL DEFAULT false,
   name varchar(50) NOT NULL,
   CONSTRAINT category_pkey PRIMARY KEY (id)
);
ALTER TABLE bank.category OWNER TO bank007;
GRANT ALL ON TABLE bank.category TO bank007;

-- Drop old constraint if exists
ALTER TABLE bank.category DROP CONSTRAINT IF EXISTS uq_category_name;

-- For user-created categories: unique per user+name+type
CREATE UNIQUE INDEX IF NOT EXISTS idx_category_user_name_type
    ON bank.category(user_id, name, transaction_type)
    WHERE user_id IS NOT NULL;

-- For predefined categories: unique per name+type
CREATE UNIQUE INDEX IF NOT EXISTS idx_category_predefined_name_type
    ON bank.category(name, transaction_type)
    WHERE predefined = true;

-- Seed category
INSERT INTO bank.category (role, transaction_type, predefined, name, version, created)
VALUES
    ('ADMIN', 'EXPENSE', true, 'Food', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Home Care', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Leisure', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Medical', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Transportation', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Utilities', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Education', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Entertainment', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Shopping', 0, NOW()),
    ('ADMIN', 'EXPENSE',true, 'Insurance', 0, NOW()),
    ('ADMIN', 'INCOME',true, 'Investments', 0, NOW()),
    ('ADMIN', 'INCOME',true, 'Savings', 0, NOW()),
    ('ADMIN', 'TRANSFER',true, 'Transfer', 0, NOW())
ON CONFLICT (name, transaction_type) WHERE predefined = true DO NOTHING;;

-- Create transaction table
CREATE TABLE IF NOT EXISTS bank.transaction (
      id bigint NOT NULL DEFAULT nextval('bank.seq_transaction'::regclass),
      version integer NOT NULL DEFAULT 0,
      created timestamptz NOT NULL,
      modified timestamptz,
      transaction_date timestamptz,
      user_id bigint NOT NULL,
      category varchar(50) NOT NULL,
      wallet_id bigint NOT NULL,
      name varchar(100) NOT NULL,
      recurring boolean NOT NULL DEFAULT false,
      amount numeric(10,2) NOT NULL,
      currency varchar(10) NOT NULL,
      frequency varchar(10),
      transaction_type varchar(10) NOT NULL,
      source_wallet_id bigint,
      destination_wallet_id bigint,
      CONSTRAINT transaction_pkey PRIMARY KEY (id),
      CONSTRAINT transaction_user_fkey FOREIGN KEY (user_id) REFERENCES bank.user(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT transaction_wallet_fkey FOREIGN KEY (wallet_id) REFERENCES bank.wallet(id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.transaction OWNER TO bank007;
GRANT ALL ON TABLE bank.transaction TO bank007;

CREATE TABLE IF NOT EXISTS bank.goal (
    id bigint NOT NULL DEFAULT nextval('bank.seq_goal'::regclass),
    version integer NOT NULL DEFAULT 0,
    created timestamptz NOT NULL,
    modified timestamptz,
    name VARCHAR(255) NOT NULL,
    user_id bigint NOT NULL,
    current_amount NUMERIC(19, 2) NOT NULL,
    goal_amount NUMERIC(19, 2) NOT NULL,
    start_date timestamptz NOT NULL DEFAULT NOW(),
    target_date timestamptz NOT NULL,
    wallet_id bigint NOT NULL,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES bank.user(id),
    CONSTRAINT fk_goal_wallet FOREIGN KEY (wallet_id) REFERENCES bank.wallet(id)
);
