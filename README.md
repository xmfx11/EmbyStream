# EmbyStream

一个基于 ExoPlayer 的轻量级 Android Emby 客户端，使用 Jetpack Compose + Material 3 构建。

## 功能特性

- 🔐 **服务器连接**：输入服务器地址、用户名、密码登录
- 📺 **媒体库浏览**：查看所有媒体库及其内容
- 🎬 **详情页面**：显示标题、年份、评分、简介、标签、演员
- 🏷️ **标签筛选**：点击标签查看同类影片
- 👤 **演员作品**：点击演员查看其参演作品
- ▶️ **视频播放**：ExoPlayer + 302 直链播放
- ⚙️ **设置管理**：查看服务器信息、退出登录

## 技术栈

| 技术 | 版本 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 播放引擎 | ExoPlayer 2.19.1 |
| 网络 | Retrofit 2.9.0 + OkHttp 4.12.0 |
| 图片加载 | Coil-compose 2.6.0 |
| 存储 | DataStore 1.0.0 |
| 最低 SDK | 24 |
| 目标 SDK | 34 |

## 项目结构

```
app/src/main/java/com/embystream/
├── EmbyApplication.kt           # Application 入口
├── data/
│   ├── api/
│   │   ├── EmbyApiService.kt    # API 接口定义
│   │   └── AuthInterceptor.kt   # Token 拦截器
│   ├── model/                   # 数据模型
│   ├── repository/
│   │   └── EmbyRepository.kt    # 数据仓库
│   └── local/
│       └── TokenManager.kt      # Token 管理
├── presentation/
│   ├── ui/
│   │   ├── login/               # 登录页
│   │   ├── home/                # 首页
│   │   ├── detail/              # 详情页
│   │   ├── player/              # 播放器
│   │   └── settings/            # 设置页
│   ├── navigation/              # 导航
│   └── theme/                   # 主题
└── utils/
    └── Extensions.kt            # 工具类
```

## 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1) 或更高版本
- JDK 17+
- Android SDK 34

### 构建

```bash
# 克隆项目
git clone https://github.com/xmfx11/EmbyStream.git
cd EmbyStream

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

### 使用说明

1. 安装并打开应用
2. 输入 Emby 服务器地址（如 `http://192.168.1.100:8096`）
3. 输入用户名和密码
4. 点击登录，开始浏览媒体库

## API 说明

应用调用以下 Emby Server API：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/Users/AuthenticateByName` | POST | 用户登录 |
| `/Users/{userId}/Views` | GET | 获取媒体库列表 |
| `/Users/{userId}/Items` | GET | 获取媒体内容列表 |
| `/Videos/{itemId}/PlaybackInfo` | GET | 获取播放信息 |

## License

MIT License
