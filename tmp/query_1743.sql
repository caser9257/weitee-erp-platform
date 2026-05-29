SELECT datetime(timestamp/1000,'localtime') AS t, message
FROM events
WHERE message LIKE '%17:43%'
ORDER BY timestamp
LIMIT 80;
