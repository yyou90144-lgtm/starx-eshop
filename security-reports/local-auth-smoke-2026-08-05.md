# Authorized local smoke (W08 alternative to VulnClaw)

> Target: only http://127.0.0.1:8080/api
> Time: 2026-08-05
> VulnClaw CLI failed (TypeError/Bad file descriptor); used PowerShell authorized probes instead.

| Check | HTTP | Note |
|-------|------|------|
| health | ERR | 方法调用失败，因为 [System.Byte] 不包含名为“Substring”的方法。 |
| public_products | 200 | {"code":0,"message":"success","data":{"current":1,"size":1,"total":18,"records":[{"id":1,"categoryId":1,"name":"æºè½æ |
| public_categories | 200 | {"code":0,"message":"success","data":[{"id":1,"name":"ææºæ°ç ","sortOrder":10,"status":"ENABLED","createdAt":"2026- |
| admin_no_auth | 401 |  |
| cart_no_auth | 401 |  |
| login_bad | 401 | {"code":40102,"message":"用户名或密码错误"} |
| login_admin | 200 | {"code":0,"message":"success","data":{"userId":2,"username":"admin","nickname":"ç³»ç»ç®¡çå","role":"ADMIN","token": |
| admin_with_token | SKIP | no token extracted from login |

## Token follow-up

| Check | Result |
|-------|--------|
| login_admin token | obtained (len=165, role=ADMIN) |
| admin_with_token | see console / re-run result below |

