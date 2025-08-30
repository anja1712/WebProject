-- Sql to be executed from favorite db client (pgadmin for example)
-- Schema creation
CREATE SCHEMA IF NOT EXISTS bank AUTHORIZATION bank006;
GRANT ALL ON SCHEMA bank TO bank006;


--reset everything
--recreate dbs
DROP TABLE IF EXISTS bank.customer CASCADE;
DROP TABLE IF EXISTS bank.configuration CASCADE;
DROP TABLE IF EXISTS bank.category CASCADE;
DROP TABLE IF EXISTS bank.currency CASCADE;
DROP TABLE IF EXISTS bank.transaction CASCADE;
DROP TABLE IF EXISTS bank.wallet CASCADE;


--recreate sequences
DROP SEQUENCE IF EXISTS bank.seq_customer;
DROP SEQUENCE IF EXISTS bank.seq_configuration;
DROP SEQUENCE IF EXISTS bank.seq_category;
DROP SEQUENCE IF EXISTS bank.seq_currency;
DROP SEQUENCE IF EXISTS bank.seq_transaction;
DROP SEQUENCE IF EXISTS bank.seq_wallet;


--Create table currency
CREATE TABLE bank.currency (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
name character varying(100) NOT NULL,
currency_value numeric(10, 2) NOT NULL,
CONSTRAINT currency_pkey PRIMARY KEY (id)
);
GRANT ALL ON TABLE bank.currency TO bank006;

CREATE SEQUENCE bank.seq_currency START WITH 1000;


--Create table customer
CREATE TABLE bank.customer (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
currency_id bigint,
birth_date timestamp without time zone,
blocked boolean NOT NULL DEFAULT false,
email character varying(50) NOT NULL,
role character varying(10) NOT NULL,
user_name character varying(50) NOT NULL,
password character varying(50) NOT NULL,
profile_picture character varying(100) NOT NULL,
first_name character varying(50) NOT NULL,
last_name character varying(50) NOT NULL,
CONSTRAINT customer_pkey PRIMARY KEY (id)
-- CONSTRAINT customer_currency_fkey FOREIGN KEY (currency_id)
-- REFERENCES bank.currency (id)
);
GRANT ALL ON TABLE bank.customer TO bank006;

CREATE SEQUENCE bank.seq_customer START WITH 1000;


--Create table configuration
CREATE TABLE bank.configuration (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
name character varying(100) NOT NULL,
config_value text NOT NULL,
customer_id bigint,
CONSTRAINT configuration_pkey PRIMARY KEY (id),
CONSTRAINT configuration_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id) 
);
GRANT ALL ON TABLE bank.configuration TO bank006;

CREATE SEQUENCE bank.seq_configuration START WITH 1000;




--Create table wallet
CREATE TABLE bank.wallet (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
name character varying(100) NOT NULL,
init_value numeric(10, 2) NOT NULL,
current_value numeric(10, 2) NOT NULL,
customer_id bigint,
currency_id bigint,
savings boolean NOT NULL,
archive boolean NOT NULL,
CONSTRAINT wallet_pkey PRIMARY KEY (id),
CONSTRAINT wallet_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id) ,
CONSTRAINT wallet_currency_fkey FOREIGN KEY (currency_id)
REFERENCES bank.currency (id) 
);
GRANT ALL ON TABLE bank.wallet TO bank006;

CREATE SEQUENCE bank.seq_wallet START WITH 1000;



--Create table category
CREATE TABLE bank.category (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
customer_id bigint NOT NULL,
account_type character varying(10) NOT NULL,
predefined boolean NOT NULL DEFAULT false,
name character varying(50) NOT NULL,
CONSTRAINT category_pkey PRIMARY KEY (id),
CONSTRAINT category_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id) 
);
GRANT ALL ON TABLE bank.category TO bank006;

CREATE SEQUENCE bank.seq_category START WITH 1000;



--Create table transaction
CREATE TABLE bank.transaction (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
transaction_date timestamp without time zone,
customer_id bigint NOT NULL,
category_id bigint NOT NULL,
wallet_id bigint NOT NULL,
name character varying(50) NOT NULL,
recurring character varying(10) NOT NULL,
amount numeric(10, 2) NOT NULL,
frequency character varying(10),
account_type character varying(10) NOT NULL,
CONSTRAINT transaction_pkey PRIMARY KEY (id),
CONSTRAINT transaction_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id),
CONSTRAINT transaction_category_fkey FOREIGN KEY (category_id)
REFERENCES bank.category (id),
CONSTRAINT transaction_wallet_fkey FOREIGN KEY (wallet_id)
REFERENCES bank.wallet (id)
);
GRANT ALL ON TABLE bank.transaction TO bank006;

CREATE SEQUENCE bank.seq_transaction START WITH 1000;