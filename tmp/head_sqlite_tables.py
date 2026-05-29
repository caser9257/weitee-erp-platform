import sqlite3
path=r'C:\Users\Administrator\.tester\logs_2.sqlite'
conn=sqlite3.connect(path)
cur=conn.cursor()
for row in cur.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name LIMIT 10"):
    print(row[0])
