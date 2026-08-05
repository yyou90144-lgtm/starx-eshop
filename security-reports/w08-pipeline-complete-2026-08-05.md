# W08 完整流水线报告 · 2026-08-05

## 工具

| 步骤 | 工具 | 结果 |
|------|------|------|
| 1 源码审计 | DeepSec Shield | backend 1H+6M；frontend 0 |
| 2 分诊/注释 | 人工 + SecurityConfig 注释 | CSRF 有意关闭；公开面表已确认 |
| 3 供应链 | deepsec shield supply-chain check | FE 0；BE pom 未扫到依赖 |
| 4 运行时 | VulnClaw → 失败；本地授权冒烟 | 公开/鉴权边界符合预期 |

## 报告目录 security-reports/

- shield-backend.md / shield-frontend.md / shield-summary-2026-08-05.md
- supply-chain-frontend.md / supply-chain-backend.md
- local-auth-smoke-2026-08-05.md
- hardening-auth-surface-2026-08-05.md
- 本文件：w08-pipeline-complete-2026-08-05.md

## 风险结论（课程发布视角）

- **无 critical**
- **无密钥类 finding（src 范围）**
- HIGH CSRF：JWT 架构可接受，已文档化
- MEDIUM permitAll：与 B2C 公开逛店一致；admin 需登录
- 运行时冒烟：匿名不能进 admin/cart；admin token 可进用户管理

## 阻塞项（工具）

- VulnClaw CLI 无法在本机完成 scan（包内 TypeError / Bad file descriptor）→ 记入工具债，不阻塞课程交付分诊
