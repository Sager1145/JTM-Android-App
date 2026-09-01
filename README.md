# JTM Android App

JTM Android App 是 Japan Train Map 的原生 Android 起步版本。它使用 Kotlin、Jetpack Compose、Material 3 和 Navigation 3，提供一个能直接运行和继续扩展的铁路旅行体验。

## 当前可用功能

- 可拖动、双指缩放和重置的东京铁路示意图
- 按行程切换地图关注点
- 行程搜索、选择、添加，以及“计划中 / 已乘坐”状态切换
- 铁路护照：完成度、里程、时间、地区与已盖章行程
- 手机底部导航与大屏导航栏自适应
- 系统动态配色、深色模式和 edge-to-edge
- 独立 `core` 模块，负责行程账本、搜索与统计，不依赖 Compose UI

> 当前数据保存在内存中，应用重启后会恢复示例数据。持久化、完整线路数据和 iOS 数据格式兼容是下一阶段工作。

## 项目结构

```text
app/   Android 应用、Compose UI、Navigation 3、ViewModel
core/  平台无关的铁路行程模型、账本和统计逻辑
```

## 运行

需要 Android Studio 2026.1 或兼容环境、JDK 17+ 与 Android SDK 36。

```bash
./gradlew testDebugUnitTest assembleDebug
./gradlew installDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 测试

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```
