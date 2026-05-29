import sqlite3

path = "/mnt/d/ruoyi-vue-pro/tmp/logs_2_snapshot.sqlite"
conn = sqlite3.connect(path)
cur = conn.cursor()

print("tables:")
for (name,) in cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"):
    print(name)

print("views:")
for (name,) in cur.execute("SELECT name FROM sqlite_master WHERE type='view' ORDER BY name"):
    print(name)

print("log columns:")
for row in cur.execute("PRAGMA table_info(logs)"):
    print(row)

print("sample rows:")
for row in cur.execute("SELECT * FROM logs LIMIT 3"):
    print(row)

thread_id = "019dfc5c-dbac-7d01-a2d0-1bfe2d0a6eda"
print("thread hits:")
for row in cur.execute(
    "SELECT ts, level, target, feedback_log_body FROM logs WHERE thread_id = ? ORDER BY ts LIMIT 200",
    (thread_id,),
):
    print(row)

print("time hits:")
for row in cur.execute(
    "SELECT ts, level, target, feedback_log_body FROM logs WHERE feedback_log_body LIKE '%17:43%' ORDER BY ts LIMIT 200"
):
    print(row)
