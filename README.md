# 全能助手（Life Helper）

一款 Android 平台的多功能个人助手应用，包含五大核心模块：**学习目标管理台、课程表、记账本、语言翻译、我的**。全部数据本地存储，不依赖自建服务器。

- **语言**：Kotlin
- **架构**：MVVM
- **UI**：Jetpack Compose + Material 3
- **数据库**：Room
- **异步**：Coroutines / Flow
- **通知**：WorkManager + NotificationCompat
- **网络**：Retrofit + OkHttp（仅翻译功能）
- **包名**：`com.example.lifehelper`
- **minSdk**：24（Android 7.0），**targetSdk**：34（Android 14）

> 说明：本项目的依赖注入采用**手动 DI（`AppContainer`）**，未使用 Hilt。根据需求 Hilt 为可选项，手动注入可减少构建配置复杂度、更易上手。如需迁移 Hilt，可参考文末「迁移 Hilt」一节。

---

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 学习目标 | 创建/编辑/删除目标，标题、描述、截止日期、进度、优先级；按状态筛选；今日关注（临近截止/逾期标红） |
| 课程表 | 周视图，7 天多节课；名称、地点、教师、起止时间、周次（单/双周）；冲突检测；课前提醒 |
| 记账本 | 收/支录入，分类、日期、备注；月度汇总、分类饼图；月预算与超支提醒 |
| 语言翻译 | 多语言互译（MyMemory 免费 API）；源/目标语言选择；历史记录本地保存 |
| 我的 | 头像/昵称/签名编辑；通知开关、深色/浅色/跟随系统、界面语言；JSON 备份/恢复、CSV 导出；清除数据（二次确认） |

---

## 编译与运行

### 环境要求

- Android Studio（建议最新稳定版，如 Hedgehog 2023.1.1 及以上）
- JDK 17
- Android SDK Platform 34
- Gradle 8.9（首次打开 Android Studio 会自动下载）

### 步骤

1. 用 Android Studio 打开本项目根目录（`lifehelper/`）。
2. 等待 Gradle Sync 完成（首次会下载依赖）。
3. 连接设备或启动模拟器（Android 7.0+）。
4. 点击 **Run ▶** 编译并安装。

也可以在命令行构建 APK：

```bash
# 首次需先配置 local.properties（指向你的 Android SDK）
# 例如 Windows：sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
./gradlew assembleDebug
# 产物位于 app/build/outputs/apk/debug/app-debug.apk
```

> 若项目目录下没有 `gradlew`（未提交 wrapper jar），直接以 Android Studio 打开即可；或执行 `gradle wrapper --gradle-version 8.9` 生成。

---

## 配置翻译 API

翻译功能默认使用 **MyMemory 免费 API**，**无需密钥**，开箱即用：

- 接口：`https://api.mymemory.translated.net/get?q={text}&langpair={源}|{目标}`
- 代码位置：`app/src/main/java/com/example/lifehelper/network/TranslationService.kt`

如需替换为需要密钥的服务（如 Google、DeepL、百度翻译等），请：

1. 在 `TranslationService.kt` 的 `TODO` 处填入密钥常量（**切勿提交真实密钥到仓库**）。
2. 修改 `TranslationApi` 接口与 `TranslationService` 的 `baseUrl`、请求参数以匹配目标服务。
3. 相应调整 `TranslationResponse` / `ResponseData` 的字段映射。

---

## 数据与备份

- 所有业务数据存于 Room 数据库（`lifehelper.db`）。
- 「我的 → 数据备份与恢复」支持：
  - **导出备份**：全量数据导出为 JSON 文件（通过系统文件选择器选择保存位置）。
  - **导入恢复**：从 JSON 文件恢复（导入前会清空现有数据）。
  - **导出 CSV**：导出账目明细为 CSV。
- 头像、昵称、主题、语言等偏好存于 SharedPreferences。
- 备份/恢复使用 **SAF（Storage Access Framework）**，无需存储运行时权限。

---

## 权限说明

| 权限 | 用途 | 类型 |
| --- | --- | --- |
| `INTERNET` | 翻译功能网络请求 | 普通权限（自动授予） |
| `POST_NOTIFICATIONS` | Android 13+ 通知（课程/目标提醒） | 运行时权限（首次启动申请） |
| `SCHEDULE_EXACT_ALARM` | 课程提醒精确调度（Android 12 及以下） | 普通权限 |

---

## 项目结构

```
app/src/main/java/com/example/lifehelper/
├── LifeHelperApp.kt          # Application，初始化容器、通知渠道、示例数据
├── AppContainer.kt           # 手动依赖注入容器
├── MainActivity.kt           # 主界面 + 底部导航 + 主题/语言
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   ├── entity/           # Goal / Course / Transaction / TranslationRecord
│   │   └── dao/              # 各表 DAO
│   ├── repository/           # 仓库层（含 ProfileRepository 偏好存储）
│   └── SampleDataSeeder.kt   # 首次启动示例数据
├── network/                  # Retrofit 翻译 API
├── notification/             # 通知渠道与发送工具
├── work/                     # 课程/目标提醒 Worker 与调度器
├── util/                     # 备份恢复、头像压缩、Locale 切换
└── ui/
    ├── theme/                # 主题（颜色/字体/深色）
    ├── navigation/           # 底部导航目的地
    ├── common/               # 日期格式化等公共工具
    ├── goal/                 # 学习目标
    ├── course/               # 课程表
    ├── transaction/          # 记账本
    ├── translate/            # 翻译
    └── profile/              # 我的
```

---

## 迁移 Hilt（可选）

如希望改用 Hilt 依赖注入：

1. 根 `build.gradle.kts` 增加插件：`id("com.google.dagger.hilt.android") version "2.51.1" apply false`，并声明 `ksp` 插件（已存在）。
2. `app/build.gradle.kts` 应用 Hilt 插件，加入 `implementation("com.google.dagger:hilt-android:2.51.1")` 与 `ksp("com.google.dagger:hilt-compiler:2.51.1")`。
3. 为 `LifeHelperApp` 加 `@HiltAndroidApp`，`MainActivity` 加 `@AndroidEntryPoint`。
4. 用 `@Module @InstallIn` 提供 `AppDatabase`、各 Repository，`ViewModel` 加 `@HiltViewModel`，并将现有 `Factory` 移除。

---

## 开源许可

- 本项目代码采用 MIT 许可。
- 仓库地址：https://github.com/nanbujiwanfeng/Life-Helper
- 依赖库版权归各自所有者所有。
