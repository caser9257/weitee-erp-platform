#!/usr/bin/env python3
import json
import time
import urllib.error
import urllib.request


BASE_URL = "http://localhost:18080/admin-api"


def request_json(method, path, token=None, payload=None):
    data = None
    headers = {}
    if token:
        headers["Authorization"] = "Bearer " + token
    if payload is not None:
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json"
    req = urllib.request.Request(BASE_URL + path, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {path} failed: HTTP {exc.code} {body}") from exc


def assert_code0(step, body):
    if body.get("code") != 0:
        raise RuntimeError(f"{step} failed: {json.dumps(body, ensure_ascii=False)}")


def main():
    marker = "TESTER-SMOKE-STOCK-OUT-" + time.strftime("%Y%m%d%H%M%S")
    created_id = None
    login = request_json("POST", "/system/auth/login", payload={
        "username": "superadmin",
        "password": "123456",
        "captchaVerification": "",
    })
    assert_code0("login", login)
    token = login["data"]["accessToken"]

    customers = request_json("GET", "/erp/customer/simple-list", token=token)
    assert_code0("customer simple-list", customers)
    customer_list = customers.get("data") or []
    if not customer_list:
        raise RuntimeError("no enabled customer found for stock-out draft smoke")
    customer_id = customer_list[0]["id"]

    try:
        create = request_json("POST", "/erp/stock-out/create", token=token, payload={
            "customerId": customer_id,
            "outTime": int(time.time() * 1000),
            "remark": marker,
            "fileUrl": "",
            "items": [{
                "warehouseId": 3,
                "productId": 3,
                "productPrice": 1,
                "count": 1,
                "remark": marker,
            }],
        })
        assert_code0("create stock-out", create)
        created_id = create["data"]

        after_create = request_json("GET", f"/erp/stock-out/get?id={created_id}", token=token)
        assert_code0("get stock-out after create", after_create)

        delete = request_json("DELETE", f"/erp/stock-out/delete?ids={created_id}", token=token)
        assert_code0("delete stock-out", delete)
        deleted_id = created_id
        created_id = None

        final_get = request_json("GET", f"/erp/stock-out/get?id={deleted_id}", token=token)
        print(json.dumps({
            "marker": marker,
            "customerId": customer_id,
            "createdId": deleted_id,
            "createdNo": after_create["data"].get("no"),
            "createStatus": after_create["data"].get("status"),
            "deleteCode": delete.get("code"),
            "finalGetCode": final_get.get("code"),
            "dataExists": final_get.get("code") == 0 and final_get.get("data") is not None,
        }, ensure_ascii=False, indent=2))
    finally:
        if created_id is not None:
            try:
                request_json("DELETE", f"/erp/stock-out/delete?ids={created_id}", token=token)
            except Exception as exc:
                print(f"cleanup delete failed for stock-out id={created_id}: {exc}")


if __name__ == "__main__":
    main()
