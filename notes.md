Bugs/missing
- [ ] Message count for each date is not correct, doesn't take thread_ts into account
- [ ] No message count on channels
- [ ] blockquote rendering
- [ ] User id->name mapping in search results
- [ ] Nicer date format on messages

Data import
- [ ] slack-event-sink -> use event ts not message ts to determine file name
- [ ] Systemd timer for import

Traefik
- [ ] Clean up accesslog unneeded fields https://doc.traefik.io/traefik/reference/install-configuration/observability/logs-and-accesslogs/
- [ ] Maintenance mode page

Observability
- [ ] Machine CPU/RAM/Disk stats
- [ ] Sentry or similar
- [ ] Alerts on free disk space
- [ ] Improve logging -> startup sequence, running migrations

Bots/scrapers
- [ ] robots.txt
- [ ] Per-ip rate limits
- [ ] AI scraper denylist

Backup
- [ ] Backup to rsync.net
