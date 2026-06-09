import json, sys

body = sys.stdin.read()
data = {"body": body}
print(json.dumps(data, ensure_ascii=False))
