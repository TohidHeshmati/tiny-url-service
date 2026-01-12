-- this is where services get their block of ids
CREATE TABLE IF NOT EXISTS global_id_sequence
(
    next_block_start BIGINT NOT NULL
);

-- starting from 1
INSERT INTO global_id_sequence (next_block_start)
VALUES (1);