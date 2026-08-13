# STARX 星选商城 · Android 原生客户端

> 与共享后端联调的原生购物客户端：登录/注册、分类搜索、商品列表与详情、加购。  
> 设计说明：`../docs/02-设计文档/Android原生客户端设计-P2.md`

## 技术栈

| 项 | 选型 |
|----|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 网络 | Retrofit + OkHttp + Kotlinx Serialization |
| 鉴权 | JWT → `EncryptedSharedPreferences` |
| 最低系统 | Android 8.0（API 26） |

## 环境要求

1. **Android Studio** Ladybug / Koala 或更新（自带 SDK、模拟器）
2. JDK 17（Studio 自带即可）
3. 共享后端已启动（项目根目录执行 `scripts/start-shared-backend.ps1`）

当前若本机尚未安装 Android Studio / SDK，请先安装后再编译运行。

## 第一次打开

1. 安装 [Android Studio](https://developer.android.com/studio)
2. 用 Android Studio 打开目录：

   ```
   eshop-release-v1.0/android
   ```

3. 等待 Gradle Sync 完成（会下载依赖与 Gradle 8.9）
4. 复制 API 配置（可选）：

   ```powershell
   Copy-Item local.properties.example local.properties
   ```

   用 Studio 打开时通常会**自动生成** `local.properties` 的 `sdk.dir`。请再补上或修改：

   ```properties
   api.base.url=http\://10.0.2.2\:8080/api/
   ```

   | 场景 | `api.base.url` |
   |------|----------------|
   | 模拟器 → 共享后端 | `http://10.0.2.2:8080/api/` |
   | USB 真机 → 共享后端 | `http://127.0.0.1:8080/api/`（脚本会执行 `adb reverse`） |

5. 选择模拟器或真机，点击 **Run**。

命令行（需已配置 `ANDROID_HOME` 与 wrapper）：

```powershell
cd eshop-release-v1.0\android
.\gradlew.bat assembleDebug
```

## 已包含

### S1
- 工程结构：`core/` + `feature/` + `data/`
- 品牌浅色主题（主色 `#E5484D`）
- Retrofit、`ApiResponse`、JWT 拦截器、Token 加密存储
- 登录 / 注册（注册后自动登录）
- 底部 Tab：首页 / 购物车 / 订单 / 我的
- Debug「开发预览主页（不登录）」

### S3 商品
- 首页：分类 Chip、关键词搜索、双列商品网格、分页加载更多
- 热销榜入口（接口失败则隐藏）
- 商品详情：SKU 选择、数量、加入购物车（未登录会提示并跳转登录）

### 商品图片与列表性能优化（2026-08-11）

- 内置 6 张 512px WebP 商品图，位于 `app/src/main/res/drawable-nodpi/catalog_*.webp`，仅作为图片加载失败或滚动缓存未命中时的本地兜底。
- 商品列表与详情优先读取同一个后端 `mainImage`，因此电脑后台更新商品主图后 Android 会读取同一份数据；列表图固定为 200px、`RGB_565`、无淡入，滑动中只读取内存缓存，避免网络或解码阻塞 UI 线程。
- 首页进入后会在后台预热全部 6 张缩略图；应用不限制系统高刷新率，日常显示由设备按高刷策略调度。
- 本地映射 `catalogImageResource(name, categoryId, productId)` 仍由列表和详情共用，作为后端图片异常时的统一回退；当前只有 6 张本地素材，因此不能宣称每个 SKU 都有独立实拍图。
- 真机验证：vivo V2307A 在系统 `120Hz` 下，以 `com.eshop.starx.performance` 运行商品首页；连续两轮各 8 轮上滑/下滑（每轮共 16 次手势）采样。首轮为 `p50=5ms`、`p90=8ms`、`p95=8ms`、`p99=14ms`、掉帧 `0.62%`、慢位图上传 `0`；复测为 `p50=5ms`、`p90=6ms`、`p95=7ms`、`p99=8ms`、掉帧 `0%`，慢 UI 线程、慢位图上传和慢绘制提交均为 `0`。两轮数据均满足本机 120Hz（`8.33ms` 帧间隔）的高刷滚动目标；后续新增复杂组件时应以同口径复测。
- 首页视觉与筛选修复后再次用 performance 包回归：`883` 帧，`p50=5ms`、`p90=7ms`、`p95=7ms`、`p99=9ms`，新版 Janky frames 为 `0%`；慢 UI 线程、慢位图上传和慢绘制提交均为 `0`。

### 首页视觉、分类状态与图片一致性（2026-08-11）

- 首页改为 Material 3 原生商城结构：逐字彩虹品牌标题、原生搜索框、紧凑分类 Tab、热销精选、商品标题区、双列商品卡，以及匹配最终卡片尺寸的加载骨架；底色改为冷中性浅灰，红色只承担选中、价格和主要操作。
- 分类和搜索请求使用可取消 `Job`，并在响应落地前校验当前筛选条件，防止较慢的旧请求覆盖用户刚选择的“全部”。切换分类或搜索条件时会清空旧列表、显示加载状态并滚回顶部。
- 真机从“手机数码”切回“全部”已验证：数量由 `6` 恢复为 `18`，“全部”重新选中，热销精选恢复，列表标题回到“全部商品”。
- vivo 开发者选项的刷新率叠加层会在左上角显示 `120`；它可能与首页标题重叠，看起来像一小段竖线，不属于 App UI。

## 共享后端与统一保存

电脑网页、Android 模拟器和 USB 真机只使用一份后端与一份数据库，不再启动 H2 内存预览库，也不需要分别修改两个后端地址。

```powershell
cd eshop-release-v1.0
.\scripts\start-shared-backend.ps1
```

- 后端地址固定为 `http://127.0.0.1:8080/api/`，PC Vite 的 `/api` 代理已经指向它。
- 共享数据文件为 `data/shared-backend/eshop.mv.db`，关闭并重启后仍保留商品、库存、用户、购物车和订单。
- USB 真机保持 `android/local.properties` 中的 `http://127.0.0.1:8080/api/`；脚本会自动执行 `adb reverse tcp:8080 tcp:8080`。
- 模拟器使用 `http://10.0.2.2:8080/api/`。两种地址只是到同一台电脑 `8080` 的不同网络入口，不是两套后端。
- 停止服务执行 `.\scripts\stop-shared-backend.ps1`；不会删除共享数据库文件。只有手动删除 `data/shared-backend/` 才会重置本地数据。

## 如何在 Studio 运行

1. 打开 `eshop-release-v1.0/android`
2. 在项目根执行 `scripts/start-shared-backend.ps1`，再按当前设备使用上方固定的 `api.base.url`
3. 创建/启动模拟器 → Run
4. 可先「开发预览主页」看商品列表；加购需登录

## 下一步（S4）

- 购物车列表 / 改数量 / 删除
- 地址管理

## 包名

`com.eshop.starx`

### 2026-08-12 - Product detail JSON rendering fix

- Fixed the Android detail page rendering the complete `detail` JSON as visible text. The versioned payload is now parsed with the shared Kotlinx `Json` instance and rendered as highlights, paragraphs, and labeled specs, matching the PC detail content model.
- Plain-text legacy details still use a text fallback. `:app:assemblePerformance --no-daemon` passed; the phone install was blocked by vivo's USB-install confirmation and needs one device-side approval before the updated APK can be visually rechecked.

### 2026-08-12 - Android image parity and storefront visual alignment

- Fixed the Android image path that could fall back to one bundled thumbnail when the preview backend returned `401` for Coil requests. The shared backend now explicitly permits `GET /demo/**`, and Coil uses the same `AuthInterceptor` as Retrofit for compatibility with older preview jars.
- Verified on vivo V2307A after `:app:assemblePerformance`: `智能手机 A1`, `摄影手机 Pro`, and subsequent products render their distinct backend `mainImage` files. Product detail uses the same image source as the list.
- Aligned the native storefront with PC tokens: warm paper page `#F7F4F1`, white surfaces, `#FF4D4F` retail red, warm borders, and restrained 10dp card radii. Kept fixed image/card bounds, RGB_565 list decoding, preload, and no network work during scrolling.
- Verification: shared backend restarted with the persistent H2 database; image endpoint returned `200` through USB reverse; performance APK built and installed successfully. A short device swipe sample recorded `50th=8ms`, `90th=17ms`, `95th=25ms`, with `0` slow bitmap uploads; detail-page interaction was included in this mixed sample.

### 2026-08-12 - Product image quality normalization

- Normalized the 18 catalog main images used by the shared backend and PC static directory to a consistent `1200x900` catalog canvas with a neutral `#F7F4F1` background. The product subject remains centered and is never stretched. The former ranking poster, price-promotion page, and multi-device collage are no longer used as phone list thumbnails.
- Android list cards now use fixed bounds with `ContentScale.Crop`, a neutral media surface, and a 512px `ARGB_8888` decode. This removes exposed pink background strips and avoids the color banding or softness caused by 200px `RGB_565` thumbnails. There is no layout-size change during scrolling.
- Product static URLs include revision `20260812` on Android and PC only for `/api/demo/products/*`, so devices refresh replaced images without clearing all application data. Shared backend and PC image files were verified byte-for-byte identical for all 18 primary assets.
- Verification: shared backend health endpoint and `GET /api/demo/products/04-phone-ultra.png?rev=20260812` both returned `200`; `:app:assemblePerformance --no-daemon` passed; `com.eshop.starx.performance` was installed over USB on vivo V2307A and a real-device catalog screenshot confirmed the updated image path.

### 2026-08-13 - Final retail cover system

- Supersedes the earlier `1200x900` experiment. PC and native Android now use the same 18 square product covers and the same 1:1 card media ratio, so shoppers can see the complete product form before opening details.
- Cover composition follows mainstream retail catalog practice: scene photography fills the square; isolated products are enlarged against a seamless solid background with a small safety margin. No blurred-image extension, promotional poster, price text, or multi-product collage is used.
- Eight full-bleed scene covers receive a pre-rendered 48px outer-edge feather into a matching solid color. The center and product remain sharp. Ten isolated-product covers are not feathered, preserving clean silhouettes.
- Android retains fixed image bounds, 512px `ARGB_8888` decoding, memory caching, and no runtime filters. PC removed the hover image zoom that could crop product edges. Static cache revision is `20260812-feather`.
- Verification: frontend production build and `:app:assemblePerformance --no-daemon` passed; the Impeccable detector returned no findings. PC `/pc/products` rendered 12 cards in the first page and vivo V2307A rendered the same square covers after USB installation.
- Final vivo V2307A scroll regression: 757 rendered frames, 5 janky frames (`0.66%`), `p50=5ms`, `p90=7ms`, `p95=8ms`, `p99=13ms`, and zero slow bitmap uploads.

### 2026-08-13 - Product cover edge and fill correction

- Replaced the blanket feathering rule with asset-aware treatment. Natural full-frame scenes keep their original sharp edges; feathering is no longer applied just because a cover reaches the card boundary.
- Restored the unfeathered retail versions of the eight scene covers, including the desk lamp and air purifier shown in the device feedback.
- Rebuilt `17-kettle.png`: removed the opaque 50px outer frame and transparent inner rectangle, enlarged the complete kettle, and composited it onto a seamless warm-grey studio surface with a restrained contact shadow. No blurred image extension is used.
- Backend and PC static assets remain byte-identical. Android and PC cache revision is `20260813-coverfix`; the fixed 1:1 image bounds, `ContentScale.Crop`, 512px `ARGB_8888` decode, and no-runtime-filter performance rules remain unchanged.
- Verification: the frontend production build and `:app:assemblePerformance --no-daemon` passed. The running backend returned the revised lamp and kettle assets, and the PC home-appliance category screenshot confirmed the real 1:1 card crops. ADB was restarted, but no authorized USB device was listed, so this revision could not be installed or re-profiled on the phone in this session.
