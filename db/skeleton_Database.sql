CREATE SEQUENCE multiverse_id_seq;

CREATE SEQUENCE universes_id_seq;

CREATE TABLE accounts
(
	id        SERIAL                NOT NULL
		CONSTRAINT accounts_pkey
		PRIMARY KEY,
	email     VARCHAR(100),
	password  VARCHAR(255),
	logged_in BOOLEAN DEFAULT FALSE NOT NULL
);

CREATE UNIQUE INDEX accounts_email_uindex
	ON accounts (email);

CREATE TABLE account_flags
(
	account_id INTEGER NOT NULL
		CONSTRAINT account_flags_accounts_id_fk
		REFERENCES accounts
);

CREATE UNIQUE INDEX account_flags_account_id_uindex
	ON account_flags (account_id);

CREATE TABLE items
(
	id      SERIAL   NOT NULL
		CONSTRAINT items_pkey
		PRIMARY KEY,
	type    SMALLINT NOT NULL,
	subtype SMALLINT
);

CREATE TABLE characters
(
	id         SERIAL      NOT NULL
		CONSTRAINT characters_pkey
		PRIMARY KEY,
	name       VARCHAR(20) NOT NULL,
	race       SMALLINT    NOT NULL,
	account_id INTEGER     NOT NULL
		CONSTRAINT characters_accounts_id_fk
		REFERENCES accounts
		ON UPDATE CASCADE ON DELETE CASCADE,
	world_id   INTEGER
);

CREATE TABLE inventory
(
	character_id INTEGER            NOT NULL
		CONSTRAINT inventory_characters_id_fk
		REFERENCES characters
		ON UPDATE CASCADE ON DELETE CASCADE,
	item_id      INTEGER            NOT NULL
		CONSTRAINT inventory_items_id_fk
		REFERENCES items
		ON UPDATE CASCADE ON DELETE CASCADE,
	amount       SMALLINT DEFAULT 1 NOT NULL,
	item_data    TEXT
);

CREATE TABLE equipment
(
	character_id   INTEGER  NOT NULL
		CONSTRAINT equipment_characters_id_fk
		REFERENCES characters
		ON UPDATE CASCADE ON DELETE CASCADE,
	item_id        INTEGER  NOT NULL
		CONSTRAINT equipment_items_id_fk
		REFERENCES items
		ON UPDATE CASCADE ON DELETE CASCADE,
	equipment_slot SMALLINT NOT NULL,
	item_data      TEXT
);

CREATE TABLE world_servers
(
	connection_string VARCHAR(120)         NOT NULL,
	clients_max       INTEGER DEFAULT 5000 NOT NULL,
	clients_current   INTEGER DEFAULT 0,
	world_id          INTEGER              NOT NULL
		CONSTRAINT world_servers_world_id_pk
		PRIMARY KEY
);

CREATE UNIQUE INDEX world_servers_connection_string_uindex
	ON world_servers (connection_string);

CREATE TABLE worlds
(
	id   SERIAL       NOT NULL
		CONSTRAINT worlds_id_pk
		PRIMARY KEY,
	name VARCHAR(100) NOT NULL
);

CREATE UNIQUE INDEX worlds_name_uindex
	ON worlds (name);

ALTER TABLE characters
	ADD CONSTRAINT characters_worlds_id_fk
FOREIGN KEY (world_id) REFERENCES worlds
ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE world_servers
	ADD CONSTRAINT world_servers_worlds_id_fk
FOREIGN KEY (world_id) REFERENCES worlds;

CREATE TABLE possible_world_servers
(
	name              TEXT,
	connection_string VARCHAR(120) NOT NULL
		CONSTRAINT possible_world_servers_connection_string_pk
		PRIMARY KEY
);

ALTER TABLE world_servers
	ADD CONSTRAINT world_servers_possible_world_servers_connection_string_fk
FOREIGN KEY (connection_string) REFERENCES possible_world_servers
ON UPDATE CASCADE ON DELETE CASCADE;

CREATE TABLE log_loginserver
(
	message   TEXT,
	time      BIGINT DEFAULT date_part('epoch' :: TEXT, now()) NOT NULL,
	id        SERIAL                                           NOT NULL
		CONSTRAINT log_loginserver_pkey
		PRIMARY KEY,
	throwable TEXT,
	logger    TEXT                                             NOT NULL,
	level     VARCHAR(8)
);

