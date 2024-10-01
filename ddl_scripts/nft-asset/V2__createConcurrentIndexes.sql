CREATE INDEX CONCURRENTLY state_tag_idx on awesome_hackathon.durable_state (tag);
CREATE INDEX CONCURRENTLY state_global_offset_idx on awesome_hackathon.durable_state (global_offset);
