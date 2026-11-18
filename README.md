# STARX 星选商城

**覆盖 PC、手机 H5、商家工作台与平台后台的多端电商系统，附 Kotlin 原生 Android 客户端源码。**

星选商城围绕“用户选购 → 下单支付 → 商家履约 → 平台管理”组织业务，提供商品分类与搜索、多规格 SKU、购物车、收货地址、订单、评价和运营管理能力。前后端分离设计，让 Web 与 Android 客户端共用商品、账号和购物车等后端接口。

## 功能概览

| 使用场景 | 主要功能 |
| --- | --- |
| PC 商城 | 商城首页、分类与关键词搜索、商品详情与多图、SKU 选择、热销排行、收藏、浏览历史 |
| 手机 H5 | 适配触屏的商品浏览、购物车、地址、订单、评价与个人中心 |
| 购物与订单 | 注册登录、购物车管理、地址管理、下单、模拟支付、取消订单、发货、确认收货与评价 |
| 商家工作台 | 经营看板、分类管理、商品与 SKU、库存预警、订单发货、评价管理、操作日志 |
| 平台管理 | 买家与卖家账号管理、角色与访问权限控制 |
| Android 原生客户端 | 登录注册、分类搜索、商品详情与规格、购物车、地址和个人资料 |

支付流程采用**模拟支付**。Android 的订单页面仍处于占位阶段，完整交易流程可通过 PC / H5 体验。

## 工程特点

- **多端共用 API**：PC、H5 与 Android 连接同一套 Spring Boot 服务。
- **角色分工明确**：买家、商家、平台管理员拥有对应入口与权限。
- **商品展示与管理联动**：分类、SKU、库存和商品状态由商家工作台维护。
- **交易流程可追踪**：包含订单状态、超时取消、销量排行与操作审计。
- **兼顾加载效率**：前端包含请求缓存、请求合并、购物车角标状态管理及静态资源缓存配置。
- **配套工程资料**：提供数据库迁移、接口与架构文档、后端集成测试和 API 冒烟脚本。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| Web 前端 | Vue 3、Vite 6、Vue Router、Pinia、Axios |
| UI 组件 | Element Plus（PC / 管理端）、Vant（手机 H5） |
| 后端 | Java 17、Spring Boot 3.5、Spring Security、JWT、MyBatis-Plus |
| 数据库 | MySQL 8.4、Flyway 数据库迁移；本地预览使用 H2 |
| Android | Kotlin、Jetpack Compose、Material 3、Retrofit、OkHttp、Coil |
| 部署 | Docker Compose、Nginx |

## 仓库结构

主要业务代码位于 `eshop-release-v1.0/`。目录名沿用历史版本名称。

```text
starx-eshop/
├── README.md                     # GitHub 首页介绍
├── eshop-release-v1.0/
│   ├── frontend/                 # PC 商城、H5、商家与平台后台
│   ├── backend/                  # Spring Boot API、迁移与测试
│   ├── android/                  # Kotlin 原生 Android 客户端
│   ├── docs/                     # 需求、设计、接口、测试与项目总结
│   ├── scripts/                  # 本地联调与接口冒烟脚本
│   ├── .env.example              # 本地开发环境变量模板
│   └── docker-compose.yml        # MySQL + API + Nginx 编排
└── security-reports/             # 已归档的工程检查记录
```

## 本地启动

准备 Git、Node.js 22 和 Docker Compose。Docker 构建阶段会准备 Java / Maven 环境。

以下为 PowerShell 命令：

```powershell
git clone https://github.com/yyou90144-lgtm/starx-eshop.git
cd starx-eshop/eshop-release-v1.0
Copy-Item .env.example .env

# 前端 Dockerfile 使用 npm ci，首次克隆先生成本地 npm 锁文件
Push-Location frontend
npm install --package-lock-only --ignore-scripts
Pop-Location

docker compose up -d --build
docker compose ps
```

启动配置见 `.env`，包含数据库连接、JWT 密钥、端口及管理员初始化设置。模板中的演示配置用于本地开发，部署时应设置自己的值。

| 入口 | 默认地址 |
| --- | --- |
| PC 商城首页 | <http://localhost:8088/pc> |
| 全部商品 | <http://localhost:8088/pc/products> |
| 手机 H5 | <http://localhost:8088/m> |
| 商家登录 | <http://localhost:8088/seller/login> |
| 平台管理员登录 | <http://localhost:8088/admin/login> |
| 后端健康检查 | <http://localhost:8088/api/actuator/health> |

商品图片、首页素材及部分 Android 本地资源按现有仓库规则保留在本地。新克隆项目需要补充商品素材；构建 Android 时还需补齐 Gradle wrapper JAR、引用的图片和本地源码文件。Android 环境与 API 地址配置见 [Android 说明](eshop-release-v1.0/android/README.md)。

## 开发与验证

在 `eshop-release-v1.0/frontend/` 中执行：

```powershell
npm install
npm run dev      # http://localhost:5173，/api 代理到本机 8080
npm run build
```

后端开发需要 Java 17 与 Maven，在 `eshop-release-v1.0/backend/` 中运行 `mvn test` 执行集成测试。完整 API 联调可使用 [接口冒烟脚本](eshop-release-v1.0/scripts/api-smoke-test.ps1)。

## 项目文档

- [需求规格说明书](eshop-release-v1.0/docs/01-需求文档/需求规格说明书.md)
- [系统架构设计](eshop-release-v1.0/docs/02-设计文档/系统架构设计.md)
- [数据库设计](eshop-release-v1.0/docs/02-设计文档/数据库设计.md)
- [接口设计](eshop-release-v1.0/docs/02-设计文档/接口设计.md)
- [平台账号与商家工作台设计](eshop-release-v1.0/docs/02-设计文档/平台账号与商家工作台设计.md)
- [功能完成度与验收对照](eshop-release-v1.0/docs/03-测试文档/功能完成度对照表-验收用.md)
- [项目总结报告](eshop-release-v1.0/docs/05-总结答辩/项目总结报告.md)
