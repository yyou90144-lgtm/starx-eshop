# DeepSec Shield 扫描摘要 · eshop-release-v1.01

> 工作流：W08 安全优先写代码  
> 工具：DeepSec Shield（A · sec.deepsec.shield）  
> 时间：2026-08-05  
> 范围：`backend/src` + `frontend/src`（避开 node_modules / target）  
> 命令：`deepsec shield scan <path> --format markdown`

## 汇总

| 目标 | 文件数 | findings | critical | high | medium | exit |
|------|--------|----------|----------|------|--------|------|
| backend/src | 145 | 7 | 0 | 1 | 6 | 2 |
| frontend/src | — | 0 | 0 | 0 | 0 | 0 |
| resources / docker-compose | — | 0 | 0 | 0 | 0 | 0 |

**未检出：** 硬编码密钥、幻觉依赖、危险 DOM/XSS 规则命中（本轮 L1 报告范围内）。

## Findings 分诊

### HIGH（1）

| 规则 | 位置 | 说明 | 建议 |
|------|------|------|------|
| `ai_pattern_spring_csrf_disabled` | `SecurityConfig.java:49` `.csrf(csrf -> csrf.disable())` | 前后端分离 + JWT 常见做法；Cookie Session 场景风险高 | **课程/JWT API 可接受**。若以后 cookie 会话登录，必须开 CSRF 或 SameSite+双令牌。文档注明「仅 Bearer JWT，无 cookie 会话」 |

### MEDIUM（6）— 同源 `permitAll`

均在 `SecurityConfig.java:56–62`，规则 `insecure_config_spring_permit_all`。

需人工确认是否**故意公开**（登录/注册、商品浏览、静态资源、actuator health 等）。典型 B2C 会公开：

- 认证入口（login/register）
- 商品/分类只读列表
- 健康检查（建议仅 health，勿暴露全部 actuator）

**处置：** 不是自动修代码项；对照 `requestMatchers(...).permitAll()` 清单做一次「公开面审计」表格即可。非公开管理接口若误放 permitAll → 立即收紧。

## W08 流水线状态

1. [x] deepsec shield scan  
2. [ ] 修 critical/high — 本轮 **0 critical**；1 high 为 CSRF disable，需产品决策非盲目开启  
3. [ ] 可选 supply-chain check  
4. [ ] 可选 VulnClaw 只打 127.0.0.1（需服务已起且用户明确要做）  
5. [ ] commit/发布前再扫一眼  

## 报告文件

- `security-reports/shield-backend.md`
- `security-reports/shield-frontend.md`
- 本摘要：`security-reports/shield-summary-2026-08-05.md`

## 后续（未自动执行）

- 若要 **supply-chain**：`deepsec shield supply-chain ...`  
- 若要 **VulnClaw**：仅对本机已启动服务 `http://127.0.0.1:8080` / `5173`，禁止外扫  