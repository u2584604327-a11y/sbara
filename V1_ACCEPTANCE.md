# BaraPause V1 Acceptance

## UI / Branding
- [x] 原创水豚应用图标
- [x] Adaptive Icon
- [x] 单色前台服务通知图标
- [x] 1.25 秒启动动画
- [x] 底部卡片式提醒，不再全屏覆盖

## Core behavior
- [x] 受限 App 选择
- [x] 每 App 本轮使用上限
- [x] 前台 App 事件增量跟踪，修复固定 15 秒窗口失效问题
- [x] 本地累计使用时长
- [x] 休息倒计时
- [x] 每日有限延期
- [x] 学习模式减半上限并禁用延期
- [x] 今日统计
- [x] 开机恢复

## Privacy
- [x] No INTERNET permission
- [x] No AccessibilityService
- [x] No notification content access
- [x] No keyboard/screen capture
- [x] No account/cloud sync

## Build verification
- [x] Core rule tests PASS 9/9
- [x] Static privacy/XML audit PASS
- [x] GitHub Actions Android build PASS
- [x] APK artifact generated and downloaded
- [x] APK ZIP integrity PASS
- [x] APK SHA-256 recorded

## Device validation still required
- [ ] Android 16 install PASS
- [ ] Usage access runtime PASS
- [ ] Overlay runtime PASS
- [ ] 1-minute test rule triggers popup
- [ ] Popup remains compact/non-fullscreen
- [ ] Extension counter correct
- [ ] Study mode behavior correct
- [ ] Reboot recovery verified
