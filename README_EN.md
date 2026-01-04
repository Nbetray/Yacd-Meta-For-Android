# Yacd Meta For Android

[中文](README.md)

A native Android client for Clash/Clash.Meta, ported from [yacd-meta](https://github.com/MetaCubeX/yacd).

## Screenshots

TODO

## Features

- Traffic monitoring and statistics
- Proxy group management
- Latency testing
- Rules viewer
- Connection management
- Real-time logs
- Core configuration
- Multiple backends support

## Download

Go to [Releases](../../releases) to download the latest version.

## Usage

1. Make sure Clash/Clash.Meta is running with External Controller enabled
2. Open the app, configure the backend URL (default: `http://127.0.0.1:9090`)
3. Enter secret if configured
4. Connect

## Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

For release builds, you need to configure signing in `app/build.gradle.kts`.

## Tech Stack

- Kotlin
- MVVM + Hilt
- Retrofit + OkHttp
- Coroutines + Flow
- Navigation Component
- DataStore
- MPAndroidChart

## Credits

- [yacd](https://github.com/haishanh/yacd)
- [yacd-meta](https://github.com/MetaCubeX/yacd)
- [Clash.Meta](https://github.com/MetaCubeX/mihomo)

## License

MIT
