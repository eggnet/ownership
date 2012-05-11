CREATE TABLE IF NOT EXISTS owners (
	commit_id character varying(255) NOT NULL,
	owner_id character varying(255) NOT NULL,
	file_id character varying(255) NOT NULL,
	char_start integer NOT NULL,
	char_end integer NOT NULL,
	is_delete character (1)
);
