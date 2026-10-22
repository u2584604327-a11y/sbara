# BaraPause

BaraPause 是一个 Android 本地数字健康工具：当用户选择的 App 达到本轮使用上限时，水豚会以底部卡片弹窗提醒休息。

## V1 设计

- Kotlin + Android 原生 View
- UsageStatsManager 判断前台 App
- Foreground Service 做本地计时
- TYPE_APPLICATION_OVERLAY 显示非全屏底部提醒卡片
- 每 App 独立上限
- 强制休息倒计时
- 每日有限延期
- 学习模式：上限减半并禁止延期
- 今日统计
- 开机恢复监控
- 原创水豚图标与 1.25 秒启动动画
- 不联网、不登录、不上传数据
- 不使用 AccessibilityService

## 权限边界

BaraPause 需要两项核心授权：

1. `PACKAGE_USAGE_STATS`：判断当前正在使用哪个 App。
2. `SYSTEM_ALERT_WINDOW`：在其他 App 上方显示底部提醒卡片。

“弹窗”并不能取消 overlay 权限；跨 App 主动显示窗口仍需要系统的“显示在其他 App 上层”授权。BaraPause 不申请无障碍服务，也不读取通知、键盘输入或屏幕内容。

## 构建

GitHub Actions 工作流：`.github/workflows/android-build.yml`

本地有 Android SDK 时：

```bash
gradle :app:assembleDebug
```

输出：`app/build/outputs/apk/debug/app-debug.apk`
