# STARX 星选商城

面向零售与品牌演示场景的全渠道电子商城系统。覆盖 **PC 商城**、**手机 H5**、**商家工作台** 与 **平台运营后台**，提供从商品展示、交易履约到运营管理的完整闭环。

## 产品能力

| 端 | 能力概览 |
|----|----------|
| **PC 消费者端** | 商品浏览与搜索、热销榜、详情多图/规格、收藏、购物车、地址、下单、模拟支付、订单与评价、个人中心 |
| **手机 H5** | Vue 3 + Vant 触屏体验：登录、商品、购物车、地址、下单支付、订单与个人中心 |
| **商家工作台** | 看板、分类、商品/SKU 与库存、评价处理、订单发货、操作日志 |
| **平台管理端** | 买家/卖家账号治理、运营与权限管理 |
| **后端服务** | 登录鉴权与角色、分类/商品/SKU、购物车与地址、订单与模拟支付、收藏评价、热销排行、库存预警、统计与审计日志 |

统一视觉：深蓝玻璃卡片风格入口，PC / H5 / 商家 / 平台 / 登录注册一致体验。

## 技术架构

| 分层 | 选型 |
|------|------|
| 前端 | Vue 3 · Vite · Vue Router · Pinia；PC/管理端 Element Plus，H5 Vant |
| 后端 | Java 17 · Spring Boot 3 · Spring Security · JWT · MyBatis-Plus · Flyway · Actuator |
| 数据 | MySQL 8.4（生产编排）；本地预览可用 H2 文件库 |
| 交付 | Docker Compose · Nginx 反向代理 |
| 原生扩展 | Android 客户端（`android/`，与共享后端联调） |

## 仓库结构

```
├── README.md
├── docs/                 # 产品范围、需求、架构、接口、测试与交付总结
├── frontend/             # PC 商城 / 平台与商家 / 手机 H5
├── backend/              # Spring Boot API 与 Flyway 迁移
├── android/              # 原生 Android 客户端（增强）
├── scripts/              # 启动、停止与 API 冒烟
├── reports/              # 可选：工程日志与迭代纪要
├── docker-compose.yml
└── .env.example
```

## 快速开始

### 环境变量

```powershell
Copy-Item .env.example .env
```

`.env.example` 仅用于本地开发，上线前请更换密钥与管理员口令。

### 一键启动（Docker）

```powershell
docker compose up -d --build
docker compose ps
```

健康后访问：

| 入口 | 地址 |
|------|------|
| 统一门户 | http://localhost:8088/ |
| PC 商城 | http://localhost:8088/pc/products |
| 平台管理 | http://localhost:8088/admin/login |
| 商家工作台 | http://localhost:8088/seller/login |
| 手机 H5 | http://localhost:8088/m |
| 健康检查 | http://localhost:8088/api/actuator/health |

本地管理员账号见 `.env` 中 `BOOTSTRAP_ADMIN_*`。

### 开发模式：PC / Android 共用后端

```powershell
.\scripts\start-shared-backend.ps1
```

启动 `http://127.0.0.1:8080/api/`，数据持久化在 `data/shared-backend/`。Vite 代理、Android 模拟器 `10.0.2.2:8080` 与 `adb reverse` 真机均可共用同一库存与订单数据。停止：

```powershell
.\scripts\stop-shared-backend.ps1
```

### 接口冒烟

```powershell
.\scripts\api-smoke-test.ps1
```

覆盖注册登录、权限、商品 SKU、购物车、下单支付、发货收货、评价与热销等主路径，结束后自动清理测试数据。

### 停止 Docker 编排

```powershell
docker compose down
```

## 协作建议

- 主分支保持可运行、可演示状态。
- 功能开发使用特性分支，合并前完成自测与必要审查。
- 提交信息建议：`feat` / `fix` / `refactor` / `docs` / `test` / `chore`。

## 许可与声明

本仓库用于产品演示与商业化交付展示。演示账号、样例商品与图示仅供预览，请勿用于未授权的生产环境。
