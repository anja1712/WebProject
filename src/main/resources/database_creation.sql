-- database and user creation, and granting
sudo su - postgres
psql template1
template1=# create user bak007 with password '34s54edr6ft7gyhui';
template1=# create database bank_db;
template1=# grant all privileges on database bank_db to bank007;
template1=# grant all on database bank_db to bank007;
\q
exit
-- exit psql and logout as user postgres