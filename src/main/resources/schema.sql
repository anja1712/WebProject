-- Sql to be executed from favorite db client (pgadmin for example)
-- Schema creation
-- Create schema
CREATE SCHEMA IF NOT EXISTS bank AUTHORIZATION bank007;
GRANT ALL ON SCHEMA bank TO bank007;

-- Drop tables in correct order to avoid FK conflicts
DROP TABLE IF EXISTS bank.transaction CASCADE;
DROP TABLE IF EXISTS bank.category CASCADE;
DROP TABLE IF EXISTS bank.wallet CASCADE;
DROP TABLE IF EXISTS bank.currency CASCADE;
DROP TABLE IF EXISTS bank.configuration CASCADE;
DROP TABLE IF EXISTS bank.customer CASCADE;

-- Drop sequences
DROP SEQUENCE IF EXISTS bank.seq_transaction CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_category CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_wallet CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_currency CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_configuration CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_customer CASCADE;

-- Create sequences first (so defaults can use them)
CREATE SEQUENCE bank.seq_customer
    START 1000
    INCREMENT 1;
ALTER SEQUENCE bank.seq_customer OWNER TO bank007;

CREATE SEQUENCE bank.seq_configuration START 1000;
ALTER SEQUENCE bank.seq_configuration OWNER TO bank007;

CREATE SEQUENCE bank.seq_currency START 1000;
ALTER SEQUENCE bank.seq_currency OWNER TO bank007;

CREATE SEQUENCE bank.seq_wallet START 1000;
ALTER SEQUENCE bank.seq_wallet OWNER TO bank007;

CREATE SEQUENCE bank.seq_category START 1000;
ALTER SEQUENCE bank.seq_category OWNER TO bank007;

CREATE SEQUENCE bank.seq_transaction START 1000;
ALTER SEQUENCE bank.seq_transaction OWNER TO bank007;

-- Create customer table
CREATE TABLE bank.customer (
   id bigint NOT NULL DEFAULT nextval('bank.seq_customer'::regclass),
   version integer NOT NULL DEFAULT 0,
   creation_date timestamp NOT NULL,
   modification_date timestamp,
   currency_id bigint,
   birth_date timestamp NOT NULL,
   blocked boolean NOT NULL DEFAULT false,
   email varchar(50) NOT NULL UNIQUE,
   role varchar(10) NOT NULL,
   user_name varchar(50) NOT NULL UNIQUE,
   password varchar(50) NOT NULL,
   profile_picture varchar(100) NOT NULL,
   first_name varchar(50) NOT NULL,
   last_name varchar(50) NOT NULL,
   CONSTRAINT customer_pkey PRIMARY KEY (id)
);
ALTER TABLE bank.customer OWNER TO bank007;
GRANT ALL ON TABLE bank.customer TO bank007;

-- Create configuration table
CREATE TABLE bank.configuration (
    id bigint NOT NULL DEFAULT nextval('bank.seq_configuration'::regclass),
    version integer NOT NULL DEFAULT 0,
    creation_date timestamp NOT NULL,
    modification_date timestamp,
    name varchar(100) NOT NULL,
    config_value text NOT NULL,
    customer_id bigint,
    CONSTRAINT configuration_pkey PRIMARY KEY (id),
    CONSTRAINT configuration_customer_fkey FOREIGN KEY (customer_id) REFERENCES bank.customer (id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.configuration OWNER TO bank007;
GRANT ALL ON TABLE bank.configuration TO bank007;

-- Create currency table
CREATE TABLE bank.currency (
   id bigint NOT NULL DEFAULT nextval('bank.seq_currency'::regclass),
   version integer NOT NULL DEFAULT 0,
   creation_date timestamp NOT NULL,
   modification_date timestamp,
   name varchar(100) NOT NULL,
   currency_value numeric(10,2) NOT NULL,
   CONSTRAINT currency_pkey PRIMARY KEY (id)
);
ALTER TABLE bank.currency OWNER TO bank007;
GRANT ALL ON TABLE bank.currency TO bank007;

-- Seed currency
INSERT INTO bank.currency (name, currency_value, version, creation_date)
VALUES
    ('USD', 1.00, 0, NOW()),
    ('EUR', 0.90, 0, NOW()),
    ('GBP', 0.78, 0, NOW()),
    ('RSD', 105.00, 0, NOW()),
    ('JPY', 130.00, 0, NOW());

-- Create wallet table
CREATE TABLE bank.wallet (
     id bigint NOT NULL DEFAULT nextval('bank.seq_wallet'::regclass),
     version integer NOT NULL DEFAULT 0,
     creation_date timestamp NOT NULL,
     modification_date timestamp,
     name varchar(100) NOT NULL,
     init_value numeric(10,2) NOT NULL,
     current_value numeric(10,2) NOT NULL,
     customer_id bigint,
     currency_id bigint,
     savings boolean NOT NULL,
     archive boolean NOT NULL,
     CONSTRAINT wallet_pkey PRIMARY KEY (id),
     CONSTRAINT wallet_customer_fkey FOREIGN KEY (customer_id) REFERENCES bank.customer(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
     CONSTRAINT wallet_currency_fkey FOREIGN KEY (currency_id) REFERENCES bank.currency(id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.wallet OWNER TO bank007;
GRANT ALL ON TABLE bank.wallet TO bank007;

-- Create category table
CREATE TABLE bank.category (
   id bigint NOT NULL DEFAULT nextval('bank.seq_category'::regclass),
   version integer NOT NULL DEFAULT 0,
   creation_date timestamp NOT NULL,
   modification_date timestamp,
   customer_id bigint,
   account_type varchar(10) NOT NULL,
   predefined boolean NOT NULL DEFAULT false,
   name varchar(50) NOT NULL,
   CONSTRAINT category_pkey PRIMARY KEY (id),
   CONSTRAINT category_customer_fkey FOREIGN KEY (customer_id) REFERENCES bank.customer(id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.category OWNER TO bank007;
GRANT ALL ON TABLE bank.category TO bank007;

-- Seed category
INSERT INTO bank.category (account_type, predefined, name, version, creation_date)
VALUES
    ('OUTCOME', true, 'Food', 0, NOW()),
    ('OUTCOME', true, 'Home Care', 0, NOW()),
    ('OUTCOME', true, 'Leisure', 0, NOW()),
    ('OUTCOME', true, 'Medical', 0, NOW()),
    ('OUTCOME', true, 'Transportation', 0, NOW()),
    ('OUTCOME', true, 'Utilities', 0, NOW()),
    ('OUTCOME', true, 'Education', 0, NOW()),
    ('OUTCOME', true, 'Entertainment', 0, NOW()),
    ('OUTCOME', true, 'Shopping', 0, NOW()),
    ('OUTCOME', true, 'Insurance', 0, NOW()),
    ('INCOME', true, 'Investments', 0, NOW()),
    ('INCOME', true, 'Savings', 0, NOW());

-- Create transaction table
CREATE TABLE bank.transaction (
      id bigint NOT NULL DEFAULT nextval('bank.seq_transaction'::regclass),
      version integer NOT NULL DEFAULT 0,
      creation_date timestamp NOT NULL,
      modification_date timestamp,
      transaction_date timestamp,
      customer_id bigint NOT NULL,
      category_id bigint NOT NULL,
      wallet_id bigint NOT NULL,
      name varchar(50) NOT NULL,
      recurring boolean NOT NULL DEFAULT false,
      amount numeric(10,2) NOT NULL,
      frequency varchar(10),
      account_type varchar(10) NOT NULL,
      source_wallet_id bigint,
      destination_wallet_id bigint,
      CONSTRAINT transaction_pkey PRIMARY KEY (id),
      CONSTRAINT transaction_customer_fkey FOREIGN KEY (customer_id) REFERENCES bank.customer(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT transaction_category_fkey FOREIGN KEY (category_id) REFERENCES bank.category(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
      CONSTRAINT transaction_wallet_fkey FOREIGN KEY (wallet_id) REFERENCES bank.wallet(id) ON UPDATE NO ACTION ON DELETE NO ACTION
);
ALTER TABLE bank.transaction OWNER TO bank007;
GRANT ALL ON TABLE bank.transaction TO bank007;
