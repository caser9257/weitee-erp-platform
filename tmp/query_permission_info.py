import json
import sys
import urllib.request

token = sys.argv[1]
req = urllib.request.Request(
    "http://127.0.0.1:48080/admin-api/system/auth/get-permission-info",
    headers={"Authorization": f"Bearer {token}"},
)
with urllib.request.urlopen(req, timeout=20) as resp:
    data = json.load(resp)
print(data.get("code"))
menus = data.get("data", {}).get("menus", [])
print(len(menus))
print([m.get("path") for m in menus[:20]])
