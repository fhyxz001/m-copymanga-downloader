# 拷贝漫画下载器 Android 版

桌面端 [copymanga-downloader](https://github.com/misaka10843/copymanga-downloader) 的全功能原生 Android 移植，使用 Kotlin + Jetpack Compose 开发。

## 功能

- 漫画搜索、收藏、本地库存浏览
- 章节多选下载（支持长按进入选择模式）
- CBZ / PDF 导出，可选合并 PDF
- 账号池自动注册/登录，反 60 秒风控限流
- 应用内日志与设置

## 技术栈

- UI：Jetpack Compose + Material 3
- 网络：Retrofit 2.11 + OkHttp 4.12 + kotlinx.serialization 1.7.1
- 图片：Coil 2.7.0
- PDF：PdfBox-Android 1.8.10.1
- 依赖注入：手动 service-locator（AppContainer）
- 协程：kotlinx.coroutines 1.8.1
- minSdk 26 / targetSdk 34 / Kotlin 2.0.20 / AGP 8.5.2

## 构建步骤

1. 安装 Android Studio（推荐最新稳定版）。
2. 打开 `m-copymanga-downloader/` 目录。
3. 首次同步时 Gradle 会自动下载依赖并生成 `gradle/wrapper/gradle-wrapper.jar`。
4. 连接设备或启动模拟器（API 26+）。
5. 点击 **Run** 编译并安装。

> 说明：本仓库未提交 `gradle-wrapper.jar` 二进制文件，首次 Android Studio 同步时会自动生成。

## 首次配置

1. 进入应用后打开右上角菜单 → **配置**。
2. 确认下载目录与导出目录（默认位于应用外部私有目录）。
3. 可选：登录拷贝漫画账号以访问收藏；未登录时仍可使用搜索与下载，应用会自动维护账号池。

## 项目结构

```
app/src/main/java/com/copymanga/downloader/
├── data/
│   ├── model/          # 领域模型
│   ├── remote/         # API 接口、DTO、CopyClient
│   └── store/          # 配置/账号/元数据/文件/日志存储
├── domain/
│   ├── AccountPool.kt  # 账号池反限流
│   ├── download/       # 下载引擎
│   ├── export/         # CBZ/PDF 导出
│   └── repository/     # Repository 封装
├── ui/
│   ├── components/     # 通用组件（TopAppBar 等）
│   ├── dialog/         # 登录/设置/日志/关于对话框
│   ├── nav/            # 导航
│   ├── screens/        # 5 个主屏
│   └── theme/          # 主题
├── di/
│   └── AppContainer.kt # 手动依赖注入容器
└── util/               # 工具类
```

## 已知限制

- 下载/导出错误提示中的动作名称目前保留中文，便于中文用户排查问题。
- 本机仅产出源码，构建验证需在 Android Studio 中完成。
