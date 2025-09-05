-- Sql to be executed from favorite db client (pgadmin for example)
-- Schema creation
CREATE SCHEMA IF NOT EXISTS bank_schema AUTHORIZATION bank007;
GRANT ALL ON SCHEMA bank_schema TO bank007;


--reset everything
--recreate dbs
DROP TABLE IF EXISTS bank.customer CASCADE;
DROP TABLE IF EXISTS bank.configuration CASCADE;
DROP TABLE IF EXISTS bank.category CASCADE;
DROP TABLE IF EXISTS bank.currency CASCADE;
DROP TABLE IF EXISTS bank.transaction CASCADE;
DROP TABLE IF EXISTS bank.wallet CASCADE;


--recreate sequences
DROP SEQUENCE IF EXISTS bank.seq_customer CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_configuration CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_category CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_currency CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_transaction CASCADE;
DROP SEQUENCE IF EXISTS bank.seq_wallet CASCADE;


--Create table customer
CREATE TABLE bank.customer (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
currency_id bigint,
birth_date timestamp without time zone,
blocked boolean NOT NULL DEFAULT false,
email character varying(50) NOT NULL UNIQUE,
role character varying(10) NOT NULL,
user_name character varying(50) NOT NULL UNIQUE,
password character varying(50) NOT NULL,
profile_picture character varying(100) NOT NULL,
first_name character varying(50) NOT NULL,
last_name character varying(50) NOT NULL,
CONSTRAINT customer_pkey PRIMARY KEY (id),
--REFERENCES bank.currency (id) MATCH SIMPLE
--ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.customer OWNER TO bank007;
GRANT ALL ON TABLE bank.customer TO bank007;

CREATE SEQUENCE bank.seq_customer START 1000;
ALTER TABLE bank.seq_customer OWNER TO bank007;


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
REFERENCES bank.customer (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.configuration OWNER TO bank007;
GRANT ALL ON TABLE bank.configuration TO bank007;

CREATE SEQUENCE bank.seq_configuration START 1000;
ALTER TABLE bank.seq_configuration OWNER TO bank007;



--Create table currency
CREATE TABLE bank.currency (
id bigint NOT NULL,
version integer NOT NULL DEFAULT 0,
creation_date timestamp without time zone NOT NULL,
modification_date timestamp without time zone,
name character varying(100) NOT NULL,
currency_value numeric(10, 2) NOT NULL,
CONSTRAINT configuration_pkey PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.currency OWNER TO bank007;
GRANT ALL ON TABLE bank.currency TO bank007;

CREATE SEQUENCE bank.seq_currency START 1000;
ALTER TABLE bank.seq_currency OWNER TO bank007;



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
accumulation boolean NOT NULL,
archive boolean NOT NULL,
CONSTRAINT wallet_pkey PRIMARY KEY (id),
CONSTRAINT wallet_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT wallet_currency_fkey FOREIGN KEY (currency_id)
REFERENCES bank.currency (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION,

)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.wallet OWNER TO bank007;
GRANT ALL ON TABLE bank.wallet TO bank007;

CREATE SEQUENCE bank.seq_wallet START 1000;
ALTER TABLE bank.seq_wallet OWNER TO bank007;



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
REFERENCES bank.customer (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.category OWNER TO bank007;
GRANT ALL ON TABLE bank.category TO bank007;

CREATE SEQUENCE bank.seq_category START 1000;
ALTER TABLE bank.seq_category OWNER TO bank007;



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
recurring boolean NOT NULL DEFAULT false, --promenjeno u boolean
amount numeric(10, 2) NOT NULL,
frequency character varying(10) NOT NULL,
account_type character varying(10) NOT NULL,
CONSTRAINT transaction_pkey PRIMARY KEY (id),
CONSTRAINT transaction_customer_fkey FOREIGN KEY (customer_id)
REFERENCES bank.customer (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT transaction_category_fkey FOREIGN KEY (category_id)
REFERENCES bank.category (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION,
CONSTRAINT transaction_wallet_fkey FOREIGN KEY (wallet_id)
REFERENCES bank.wallet (id) MATCH SIMPLE
ON UPDATE NO ACTION ON DELETE NO ACTION

)
WITH (
OIDS=FALSE
);
ALTER TABLE bank.transaction OWNER TO bank007;
GRANT ALL ON TABLE bank.transaction TO bank007;

CREATE SEQUENCE bank.seq_transaction START 1000;
ALTER TABLE bank.seq_transaction OWNER TO bank007;
