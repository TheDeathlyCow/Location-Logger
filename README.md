# Location Logger

A Spigot/Paper plugin that logs players locations to an SQLite database periodically.

Logs are saved to `player_locations.db` by default

Requires SQLite JDBC `3.41.2.1+20230506`: https://modrinth.com/plugin/sqlite-jdbc

## Default Config

```yaml
database-path: player_locations.db
log-interval-seconds: 5
```