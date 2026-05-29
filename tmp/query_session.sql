SELECT datetime(timestamp/1000,'localtime') AS t, message
FROM events
WHERE message LIKE '%019dfc5c-dbac-7d01-a2d0-1bfe2d0a6eda%'
ORDER BY timestamp
LIMIT 50;
