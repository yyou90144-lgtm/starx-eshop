# 鉴权公开面与 CSRF 加固说明 · eshop-release-v1.01

> DeepSec W08 续作 · 2026-08-05  
> 源码：`backend/.../security/SecurityConfig.java`（已加意图注释）

## 1. CSRF（Shield HIGH）

| 项 | 结论 |
|----|------|
| 现状 | `csrf.disable()` + `SessionCreationPolicy.STATELESS` + JWT Bearer |
| 评估 | **JWT SPA 可接受**；不是“漏配”而是有意设计 |
| 保持条件 | 浏览器**不**用 Cookie 携带会话；前端用 `Authorization: Bearer` |
| 必须改时 | 引入 cookie 会话 / 同站表单登录 → 开启 CSRF 或 double-submit cookie |

**本轮处置：** 不改行为，只在 `SecurityConfig` 写入 DeepSec W08 意图注释，避免后人“顺手打开 CSRF 搞挂前后端”或“误以为可删注释重开危险模式”。

## 2. permitAll 公开面（Shield MEDIUM ×6）

| Matcher | 意图 | 生产建议 |
|---------|------|----------|
| `OPTIONS /**` | CORS 预检 | 保留 |
| `POST /auth/register`, `/auth/login` | 注册登录 | 保留；注意限流/验证码（课程可后置） |
| `/actuator/health` | 探活 | **仅 health**；勿 `permitAll` 整个 actuator |
| `/uploads/**` | 商品图静态读 | 保留；上传写接口必须鉴权 |
| `GET /demo/**` | 演示资源 | 上线无用可关 |
| `GET /categories/**`, `/products/**` | 公开逛店 | 保留 |
| `/admin/**` | ADMIN/SELLER | 已约束；保持 |
| `anyRequest().authenticated()` | 默认拒绝匿名 | 保持 |

## 3. 供应链

| 目标 | findings |
|------|----------|
| frontend (package.json) | 0 |
| backend 目录 | 0（工具未解析 pom 依赖树，见下） |

说明：当前 DeepSec supply-chain check 对 **package.json** 有效；**Maven pom** 本轮 0 file，不能据此宣称 Java 依赖无 CVE。生产发布前建议另跑 `mvn dependency-check` 或 OSV 扫描（需定级/确认后再装）。

## 4. 本机授权冒烟（VulnClaw 替代）

VulnClaw CLI 本环境报错：`TypeError` / `OSError: Bad file descriptor`（工具安装问题，非目标站拒绝）。

改用 **仅 127.0.0.1** 的 PowerShell 探测（见 `local-auth-smoke-2026-08-05.md`）：

| 检查 | 期望 | 结果 |
|------|------|------|
| GET /products | 200 公开 | 通过 |
| GET /categories | 200 公开 | 通过 |
| GET /admin/users 无 token | 401 | 通过 |
| GET /cart 无 token | 401 | 通过 |
| POST /auth/login 错密 | 401 | 通过 |
| admin 登录 + Bearer 访问 /admin/users | 200 | 通过 |
| /actuator/health | UP | 通过 |

## 5. 可选后续加固（未改代码行为）

1. 生产关闭 `GET /demo/**`  
2. 注册/登录加限流  
3. JWT 登出黑名单（class2 笔记已记 P2：logout 后 JWT 仍可用）  
4. uploads 目录禁止可执行内容 / 路径穿越审计  
5. 修好 VulnClaw 后再做一次授权扫  

## 6. W08 流水线最终状态

1. [x] Shield 源码扫描  
2. [x] high/medium 分诊 + CSRF 意图注释（无行为破坏性修改）  
3. [x] supply-chain check（npm 0；pom 工具未覆盖）  
4. [x] 本机 127.0.0.1 授权冒烟（VulnClaw CLI 故障已记录）  
5. [ ] commit/发布：需你确认后再做  
