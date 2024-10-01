GRANT SELECT, INSERT ON awesome_hackathon.event_journal TO "awesome_hackathon";
GRANT SELECT, INSERT, UPDATE, DELETE ON awesome_hackathon.event_tag TO "awesome_hackathon";
GRANT SELECT, INSERT, UPDATE, DELETE ON awesome_hackathon.snapshot TO "awesome_hackathon";
GRANT SELECT, INSERT, UPDATE, DELETE ON awesome_hackathon.durable_state TO "awesome_hackathon";

GRANT SELECT, INSERT, UPDATE, DELETE ON awesome_hackathon.pekko_projection_offset_store TO "awesome_hackathon";
GRANT SELECT, INSERT, UPDATE, DELETE ON awesome_hackathon.pekko_projection_management TO "awesome_hackathon";

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA awesome_hackathon TO "awesome_hackathon";

--
