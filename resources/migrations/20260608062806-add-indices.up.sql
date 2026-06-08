CREATE INDEX idx_message_channel_ts ON message(channel_id, ts);
--;;
CREATE INDEX idx_channel_slack_id ON channel(slack_id);
--;;
CREATE INDEX idx_message_staging_ts_channel ON message_staging(ts, channel_id);
