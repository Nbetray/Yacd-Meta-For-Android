# Yacd Meta For Android

[English](README_EN.md)

Clash/Clash.Meta 内核的 Android 控制面板，[yacd-meta](https://github.com/MetaCubeX/yacd) 的原生移植版本。

## 截图

TODO

## 功能

- 流量监控和统计
- 代理组管理和切换
- 延迟测试
- 规则列表查看
- 连接管理
- 实时日志
- 内核配置修改
- 多后端支持

## 下载

前往 [Releases](../../releases) 下载最新版本。

## 使用

1. 确保 Clash/Clash.Meta 内核已在运行，并开启了 External Controller
2. 打开应用，配置后端地址（默认 `http://127.0.0.1:9090`）
3. 如果设置了 secret，填入密钥
4. 点击连接

## 构建

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

Release 构建需要配置签名，参考 `app/build.gradle.kts` 中的 `signingConfigs`。

## 技术栈

- Kotlin
- MVVM + Hilt
- Retrofit + OkHttp
- Coroutines + Flow
- Navigation Component
- DataStore
- MPAndroidChart

## 致谢

- [yacd](https://github.com/haishanh/yacd)
- [yacd-meta](https://github.com/MetaCubeX/yacd)
- [Clash.Meta](https://github.com/MetaCubeX/mihomo)

## License

MIT
