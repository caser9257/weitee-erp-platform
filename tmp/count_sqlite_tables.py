import sqlite3
path=r'C:\Users\Administrator\.tester\logs_2.sqlite'
conn=sqlite3.connect(path)
cur=conn.cursor()
print(cur.execute("SELECT count(*) FROM sqlite_master WHERE type='table'").fetchone()[0])
