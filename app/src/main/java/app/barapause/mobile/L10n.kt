package app.barapause.mobile

import android.content.Context
import java.util.Locale

/** Local-only UI translation; language codes are stable preference values. */
object L10n {
    fun language(context: Context): String {
        val choice = context.getSharedPreferences("barapause", Context.MODE_PRIVATE).getString("language", "system") ?: "system"
        if (choice != "system") return choice
        val l = Locale.getDefault()
        return when (l.language) {
            "zh" -> if (l.country.equals("TW", true) || l.country.equals("HK", true) || l.script.equals("Hant", true)) "zh-Hant" else "zh-Hans"
            "ja" -> "ja"
            else -> "en"
        }
    }

    private val en = mapOf(
        "app" to "BaraPause", "home_desc" to "A gentle break from endless scrolling.",
        "status" to "Status", "permissions" to "Permissions", "permission_detail" to "Usage access detects the active app. Display over other apps shows a small break reminder. No accessibility service is used.",
        "usage_permission" to "Grant usage access", "overlay_permission" to "Allow popup over apps",
        "notification_permission" to "Allow reminder notifications", "monitor" to "Enable BaraPause",
        "monitor_on" to "On", "monitor_off" to "Off", "authorize_first" to "Grant the first two permissions before starting.",
        "selected_apps" to "Limited apps", "settings" to "Rules & appearance", "stats" to "Today's activity",
        "battery" to "Battery optimization settings", "privacy" to "Privacy", "privacy_detail" to "Offline only. No accessibility, network, screen capture, keyboard or notification reading.",
        "apps_intro" to "Choose apps and set a limit for each.", "search" to "Search apps…", "selected" to "Selected", "all" to "All", "browser" to "Browsers",
        "social" to "Social", "video" to "Entertainment", "ai" to "AI tools", "office" to "Mail & office",
        "tools" to "Utilities", "other" to "Other", "list" to "List", "grid" to "Grid",
        "empty" to "No matching apps", "limit" to "Time limit", "custom" to "Custom minutes",
        "enter_minutes" to "Enter minutes (1–240)", "invalid_minutes" to "Enter a whole number from 1 to 240.",
        "minute" to "min", "seconds" to "sec", "save" to "Save", "cancel" to "Cancel",
        "study" to "Study mode", "study_desc" to "Halves limits and disables extensions during study time.",
        "break" to "Break length", "extension" to "Extension length", "extensions" to "Daily extensions",
        "start" to "Study starts", "end" to "Study ends", "window" to "Overnight periods supported. Identical times mean all day.",
        "appearance" to "Appearance", "theme" to "Theme", "system" to "Follow system", "light" to "Light",
        "dark" to "Dark", "eye" to "Eye comfort", "language" to "Language",
        "language_system" to "Follow system", "language_zh" to "简体中文", "language_tw" to "繁體中文",
        "language_en" to "English", "language_ja" to "日本語",
        "advanced" to "About & test", "version" to "Version 1.1 · tap 7× for test mode",
        "test_enabled" to "Test mode enabled", "test_disabled" to "Test mode disabled",
        "test_warning" to "TEST MODE: Short timers are for validation, not daily use.",
        "today_blocks" to "Break reminders", "today_extensions" to "Extensions used",
        "today_info" to "Only time observed while BaraPause monitors is counted, not Android's total screen time.",
        "none_selected" to "Choose an app to begin.", "break_title" to "Time for a little break!",
        "break_desc" to " reached its time limit. This popup doesn't lock your phone.",
        "rest" to "Break", "extend" to "Use more", "leave" to "Leave app",
        "overlay_note" to "You can still use the screen outside this card.",
        "service_title" to "BaraPause is gently keeping watch", "service_desc" to "Local monitoring of chosen apps only.",
        "summary_apps" to "Limited apps", "summary_blocks" to "Reminders"
    );
    private val zh = mapOf(
        "home_desc" to "温柔打断无意识刷屏", "status" to "运行状态", "permissions" to "必要权限",
        "permission_detail" to "使用情况权限用于识别当前 App；悬浮窗权限用于显示底部休息提醒，不使用无障碍服务。",
        "usage_permission" to "授予使用情况访问权限", "overlay_permission" to "允许在其他应用上层显示弹窗",
        "notification_permission" to "允许提醒通知", "monitor" to "启用 BaraPause", "monitor_on" to "运行中",
        "monitor_off" to "未启用", "authorize_first" to "请先完成前两项系统授权。",
        "selected_apps" to "受限 App", "settings" to "规则与外观", "stats" to "今日统计",
        "battery" to "电池优化设置", "privacy" to "隐私", "privacy_detail" to "完全本地运行；无联网、无无障碍、无截屏、无键盘或通知读取。",
        "apps_intro" to "选择需要提醒的应用，并为每个应用设置时长。", "search" to "搜索应用名称或包名…",
        "selected" to "已选择", "all" to "全部", "browser" to "浏览器", "social" to "社交",
        "video" to "视频娱乐", "ai" to "AI 工具", "office" to "邮件办公", "tools" to "工具", "other" to "其他",
        "list" to "列表", "grid" to "图标", "empty" to "没有符合条件的应用",
        "limit" to "时间上限", "custom" to "自定义分钟", "enter_minutes" to "输入分钟数（1–240）",
        "invalid_minutes" to "请输入 1–240 之间的整数。", "minute" to "分钟", "seconds" to "秒",
        "save" to "保存", "cancel" to "取消", "study" to "学习模式",
        "study_desc" to "学习时段限额减半，且不允许延期。", "break" to "休息时长",
        "extension" to "单次延期", "extensions" to "每日延期次数",
        "start" to "学习开始", "end" to "学习结束", "window" to "支持跨午夜；开始与结束相同表示全天。",
        "appearance" to "外观设置", "theme" to "主题", "system" to "跟随系统",
        "light" to "浅色", "dark" to "深色", "eye" to "护眼", "language" to "语言",
        "language_system" to "跟随系统", "advanced" to "关于与测试",
        "version" to "版本 1.1 · 连点 7 次启用测试模式",
        "test_enabled" to "测试模式已开启", "test_disabled" to "测试模式已关闭",
        "test_warning" to "测试模式：短时限仅用于验收，不建议日常使用。",
        "today_blocks" to "今日提醒次数", "today_extensions" to "今日已延期",
        "today_info" to "仅统计 BaraPause 运行期间监测的使用时长，并非系统全天使用记录。",
        "none_selected" to "请先选择受限 App。", "break_title" to "该歇一会儿啦",
        "break_desc" to " 已达到本轮上限。仅弹窗提醒，不锁屏。",
        "rest" to "休息", "extend" to "再用", "leave" to "先离开",
        "overlay_note" to "卡片以外的区域仍可正常操作。", "service_title" to "BaraPause 正在轻轻守门",
        "service_desc" to "仅在本机统计选定的 App。", "summary_apps" to "受限应用", "summary_blocks" to "今日提醒"
    );
    private val tw = mapOf(
        "home_desc" to "溫柔中斷無意識滑手機", "status" to "運作狀態", "permissions" to "必要權限",
        "permission_detail" to "使用情況權限用於辨識目前 App；懸浮視窗權限用於顯示休息提醒，不使用無障礙服務。",
        "usage_permission" to "授予使用情況存取權", "overlay_permission" to "允許顯示在其他應用程式上層",
        "notification_permission" to "允許提醒通知", "monitor" to "啟用 BaraPause", "monitor_on" to "執行中",
        "monitor_off" to "未啟用", "authorize_first" to "請先完成前兩項系統授權。",
        "selected_apps" to "受限 App", "settings" to "規則與外觀", "stats" to "今日統計",
        "battery" to "電池最佳化設定", "privacy" to "隱私", "privacy_detail" to "完全本機運作；不連網、不使用無障礙、不擷取畫面或鍵盤內容。",
        "apps_intro" to "選擇要提醒的 App，並各自設定時間。", "search" to "搜尋應用程式…",
        "selected" to "已選取", "all" to "全部", "browser" to "瀏覽器", "social" to "社交",
        "video" to "影音娛樂", "ai" to "AI 工具", "office" to "郵件辦公", "tools" to "工具", "other" to "其他",
        "list" to "列表", "grid" to "圖示", "empty" to "沒有符合條件的應用程式", "limit" to "時間上限",
        "custom" to "自訂分鐘", "enter_minutes" to "輸入分鐘數（1–240）", "invalid_minutes" to "請輸入 1–240 的整數。",
        "minute" to "分鐘", "seconds" to "秒", "save" to "儲存", "cancel" to "取消",
        "study" to "學習模式", "study_desc" to "學習時段上限減半，且不可延期。",
        "break" to "休息時間", "extension" to "單次延期", "extensions" to "每日延期次數",
        "start" to "學習開始", "end" to "學習結束", "window" to "支援跨午夜；起訖相同表示全天。",
        "appearance" to "外觀設定", "theme" to "主題", "system" to "跟隨系統",
        "light" to "淺色", "dark" to "深色", "eye" to "護眼", "language" to "語言",
        "language_system" to "跟隨系統", "advanced" to "關於與測試",
        "version" to "版本 1.1 · 連點 7 次開啟測試模式",
        "test_enabled" to "測試模式已開啟", "test_disabled" to "測試模式已關閉",
        "test_warning" to "測試模式僅供驗收，不建議日常使用。",
        "today_blocks" to "今日提醒", "today_extensions" to "今日已延期",
        "today_info" to "僅記錄 BaraPause 監控期間的時間，非系統全天使用時間。",
        "none_selected" to "請先選擇 App。", "break_title" to "該休息一下囉",
        "break_desc" to " 已達時間上限。只提醒，不鎖定螢幕。",
        "rest" to "休息", "extend" to "再用", "leave" to "先離開",
        "overlay_note" to "卡片以外仍可正常操作。", "service_title" to "BaraPause 正在陪你守門",
        "service_desc" to "僅在本機統計指定的 App。", "summary_apps" to "受限應用程式", "summary_blocks" to "今日提醒"
    );
    private val ja = mapOf(
        "home_desc" to "つい長く使ってしまうアプリに、やさしい休憩を。",
        "status" to "状態", "permissions" to "必要な権限",
        "permission_detail" to "使用状況へのアクセスで現在のアプリを検出し、他のアプリの上に小さな休憩カードを表示します。ユーザー補助は使いません。",
        "usage_permission" to "使用状況へのアクセスを許可", "overlay_permission" to "他のアプリの上に表示を許可",
        "notification_permission" to "通知を許可", "monitor" to "BaraPause を有効にする",
        "monitor_on" to "動作中", "monitor_off" to "停止中", "authorize_first" to "まず上の2つの権限を許可してください。",
        "selected_apps" to "制限するアプリ", "settings" to "ルールと外観", "stats" to "今日の記録",
        "battery" to "バッテリー最適化", "privacy" to "プライバシー",
        "privacy_detail" to "端末内だけで動作。通信・ユーザー補助・画面録画・入力や通知の読み取りは行いません。",
        "apps_intro" to "アプリを選び、それぞれに利用時間を設定します。", "search" to "アプリを検索…",
        "selected" to "選択済み", "all" to "すべて", "browser" to "ブラウザー", "social" to "SNS",
        "video" to "動画・娯楽", "ai" to "AI ツール", "office" to "メール・仕事",
        "tools" to "ツール", "other" to "その他", "list" to "一覧", "grid" to "アイコン",
        "empty" to "該当するアプリがありません", "limit" to "時間制限",
        "custom" to "時間を指定", "enter_minutes" to "分数を入力（1～240）",
        "invalid_minutes" to "1～240 の整数を入力してください。", "minute" to "分", "seconds" to "秒",
        "save" to "保存", "cancel" to "キャンセル", "study" to "勉強モード",
        "study_desc" to "勉強中は上限が半分になり、延長できません。",
        "break" to "休憩時間", "extension" to "1回の延長", "extensions" to "1日の延長回数",
        "start" to "開始", "end" to "終了", "window" to "日付をまたぐ時間帯に対応。同時刻なら終日。",
        "appearance" to "外観", "theme" to "テーマ", "system" to "端末に合わせる",
        "light" to "ライト", "dark" to "ダーク", "eye" to "目にやさしい",
        "language" to "言語", "language_system" to "端末に合わせる",
        "advanced" to "情報・テスト", "version" to "バージョン 1.1 · 7回タップでテストモード",
        "test_enabled" to "テストモード ON", "test_disabled" to "テストモード OFF",
        "test_warning" to "テストモード：短時間設定は動作確認用です。",
        "today_blocks" to "今日の休憩通知", "today_extensions" to "今日の延長",
        "today_info" to "監視中に計測した時間のみ表示します。OS全体の使用時間とは異なります。",
        "none_selected" to "まずアプリを選んでください。", "break_title" to "少し休憩しよう",
        "break_desc" to " は時間上限に達しました。画面はロックしません。",
        "rest" to "休憩", "extend" to "あと少し", "leave" to "アプリを離れる",
        "overlay_note" to "カードの外は操作できます。", "service_title" to "BaraPause が見守り中",
        "service_desc" to "選んだアプリのみ端末内で計測。", "summary_apps" to "制限アプリ", "summary_blocks" to "通知回数"
    );
    private val popupEn=mapOf(
        "popup_customize" to "Customize reminder", "popup_explain" to "Personalize the small break card. Changes appear on the next reminder.",
        "popup_preview" to "Preview card", "popup_position" to "Position", "popup_top" to "Top",
        "popup_middle" to "Center", "popup_bottom" to "Bottom", "popup_size" to "Card size",
        "popup_compact" to "Compact", "popup_normal" to "Standard",
        "popup_color" to "Color preset", "popup_sage" to "Sage", "popup_peach" to "Peach", "popup_lavender" to "Lavender",
        "popup_animation" to "Entry animation", "popup_spring" to "Bounce", "popup_fade" to "Fade", "popup_none" to "None",
        "popup_opacity" to "Opacity", "popup_mascot" to "Show capybara", "popup_messages" to "Custom messages",
        "popup_title" to "Custom title", "popup_body" to "Custom message",
        "popup_blank" to "Leave blank for translated default",
        "popup_placeholder" to "Use {app} in your message to show the app name. Leave blank for language-aware defaults.",
        "popup_reset" to "Restore default card", "popup_example_app" to "Example App", "popup_close" to "Close preview"
    )
    private val popupZh=mapOf(
        "popup_customize" to "自定义提醒弹窗", "popup_explain" to "定制水豚提醒卡片；修改后下次提醒生效。",
        "popup_preview" to "预览弹窗", "popup_position" to "显示位置", "popup_top" to "顶部",
        "popup_middle" to "居中", "popup_bottom" to "底部", "popup_size" to "卡片尺寸",
        "popup_compact" to "紧凑", "popup_normal" to "标准",
        "popup_color" to "配色预设", "popup_sage" to "鼠尾草绿", "popup_peach" to "蜜桃色", "popup_lavender" to "淡紫色",
        "popup_animation" to "进入动画", "popup_spring" to "弹起", "popup_fade" to "淡入", "popup_none" to "无动画",
        "popup_opacity" to "透明度", "popup_mascot" to "显示水豚", "popup_messages" to "自定义文案",
        "popup_title" to "自定义标题", "popup_body" to "自定义正文",
        "popup_blank" to "留空使用多语言默认文案",
        "popup_placeholder" to "正文可包含 {app}，显示当前 App 名称；留空则跟随语言。",
        "popup_reset" to "恢复默认弹窗", "popup_example_app" to "示例应用", "popup_close" to "关闭预览"
    )
    private val popupTw=mapOf(
        "popup_customize" to "自訂提醒視窗", "popup_explain" to "自訂水豚休息卡片；下次提醒生效。",
        "popup_preview" to "預覽視窗", "popup_position" to "顯示位置", "popup_top" to "頂部",
        "popup_middle" to "置中", "popup_bottom" to "底部", "popup_size" to "卡片大小",
        "popup_compact" to "精簡", "popup_normal" to "標準",
        "popup_color" to "配色", "popup_sage" to "鼠尾草綠", "popup_peach" to "蜜桃色", "popup_lavender" to "淡紫色",
        "popup_animation" to "進場動畫", "popup_spring" to "彈出", "popup_fade" to "淡入", "popup_none" to "無動畫",
        "popup_opacity" to "透明度", "popup_mascot" to "顯示水豚", "popup_messages" to "自訂文字",
        "popup_title" to "自訂標題", "popup_body" to "自訂內容",
        "popup_blank" to "留空使用各語言預設",
        "popup_placeholder" to "內容可使用 {app} 顯示應用名稱；留空則跟隨語言。",
        "popup_reset" to "還原預設", "popup_example_app" to "範例應用", "popup_close" to "關閉預覽"
    )
    private val popupJa=mapOf(
        "popup_customize" to "休憩カードのカスタマイズ", "popup_explain" to "次の通知から設定が反映されます。",
        "popup_preview" to "カードをプレビュー", "popup_position" to "表示位置", "popup_top" to "上",
        "popup_middle" to "中央", "popup_bottom" to "下", "popup_size" to "カードサイズ",
        "popup_compact" to "コンパクト", "popup_normal" to "標準",
        "popup_color" to "配色", "popup_sage" to "セージ", "popup_peach" to "ピーチ", "popup_lavender" to "ラベンダー",
        "popup_animation" to "アニメーション", "popup_spring" to "ポップ", "popup_fade" to "フェード", "popup_none" to "なし",
        "popup_opacity" to "不透明度", "popup_mascot" to "カピバラを表示", "popup_messages" to "カスタムメッセージ",
        "popup_title" to "タイトル", "popup_body" to "本文",
        "popup_blank" to "空欄なら言語別の標準文",
        "popup_placeholder" to "{app} で対象アプリ名を表示。空欄なら言語別の標準文。",
        "popup_reset" to "初期設定に戻す", "popup_example_app" to "サンプルアプリ", "popup_close" to "プレビューを閉じる"
    )

    private val monitorEn = mapOf(
        "diagnostics" to "Monitoring diagnostics",
        "restart_monitor" to "Restart monitoring",
        "diag_running" to "Monitor heartbeat active",
        "diag_stopped" to "Monitor heartbeat missing / stale",
        "diag_fg" to "Detected app", "diag_elapsed" to "Current session",
        "diag_reason" to "Monitor state", "diag_trigger" to "Last break trigger",
        "diag_error" to "Last error"
    )
    private val monitorZh = mapOf(
        "diagnostics" to "监控诊断", "restart_monitor" to "手动重启监控",
        "diag_running" to "监控心跳正常", "diag_stopped" to "监控心跳失效或尚未启动",
        "diag_fg" to "识别的前台 App", "diag_elapsed" to "本轮累计",
        "diag_reason" to "监控状态", "diag_trigger" to "最近触发",
        "diag_error" to "最近错误"
    )
    private val monitorTw = mapOf(
        "diagnostics" to "監控診斷", "restart_monitor" to "手動重新啟動監控",
        "diag_running" to "監控運作正常", "diag_stopped" to "監控停止或尚未啟動",
        "diag_fg" to "前景 App", "diag_elapsed" to "本輪累計",
        "diag_reason" to "監控狀態", "diag_trigger" to "最近觸發",
        "diag_error" to "最近錯誤"
    )
    private val monitorJa = mapOf(
        "diagnostics" to "監視診断", "restart_monitor" to "監視を再起動",
        "diag_running" to "監視は稼働中", "diag_stopped" to "監視が停止・未起動",
        "diag_fg" to "使用中のアプリ", "diag_elapsed" to "経過時間",
        "diag_reason" to "監視状態", "diag_trigger" to "最後の通知",
        "diag_error" to "最後のエラー"
    )

    private val newEn=mapOf(
        "back" to "Back", "exit_interface" to "Close app window (keep monitoring)",
        "select_all" to "Select all", "invert" to "Invert", "select_none" to "Deselect all",
        "bulk_scope" to "Bulk changes apply to current filter",
        "quick_search" to "Quick launch & search",
        "quick_search_explain" to "Search works directly only if the target app exposes a public Android search action; otherwise the query is copied before launching.",
        "quick_search_hint" to "Search installed app names", "query_text" to "Enter search keywords",
        "choose_app" to "Select an app", "launch_app" to "Open app",
        "search_in_app" to "Search in app", "copy_and_open" to "Copy query & open app",
        "search_fallback" to "This app has no public compatible search entry; text copied and app opened.",
        "query_required" to "Enter a query first",
        "popup_photo" to "Custom popup image (local)", "choose_photo" to "Select image / animated GIF or WebP",
        "remove_photo" to "Use built-in mascot", "photo_hint" to "Image stays on this device. File access can be revoked.",
        "popup_motion" to "Mascot movement", "motion_still" to "Still",
        "motion_breathe" to "Gentle breathing", "motion_bob" to "Playful bounce",
        "remaining" to "Remaining", "break_remaining" to "Rest remaining",
        "not_monitoring" to "No limited app active", "notification_note" to "The OS may hide this notification if you disable notifications."
    );
    private val newZh=mapOf(
        "back" to "返回", "exit_interface" to "退出界面（保留监控）",
        "select_all" to "全选", "invert" to "反选", "select_none" to "全不选",
        "bulk_scope" to "批量操作仅作用于当前筛选结果",
        "quick_search" to "智能启动与搜索",
        "quick_search_explain" to "目标 App 开放系统搜索接口时可直接搜索；否则复制关键词并打开应用。", "quick_search_hint" to "搜索已安装的软件",
        "query_text" to "输入搜索关键词", "choose_app" to "选择软件",
        "launch_app" to "打开软件", "search_in_app" to "应用内搜索",
        "copy_and_open" to "复制关键词并打开", "search_fallback" to "该软件未提供可调用的公开搜索入口，已复制关键词并打开软件。",
        "query_required" to "请先填写关键词",
        "popup_photo" to "自定义弹窗图片（本地）", "choose_photo" to "选择照片 / 动态 GIF 或 WebP",
        "remove_photo" to "恢复默认水豚", "photo_hint" to "图片只保留在本机；撤销文件授权后会恢复默认形象。",
        "popup_motion" to "动态效果", "motion_still" to "静止", "motion_breathe" to "轻柔呼吸",
        "motion_bob" to "活泼弹跳", "remaining" to "剩余", "break_remaining" to "休息剩余",
        "not_monitoring" to "当前没有受限 App", "notification_note" to "如果系统关闭了通知权限或通知频道，通知可能不会显示。"
    );
    private val newTw=mapOf(
        "back" to "返回", "exit_interface" to "關閉畫面（保留監控）",
        "select_all" to "全選", "invert" to "反選", "select_none" to "全部取消",
        "bulk_scope" to "批次操作僅作用於目前篩選結果", "quick_search" to "快速啟動與搜尋",
        "quick_search_hint" to "搜尋已安裝的 App", "quick_search_explain" to "只有 App 提供公開搜尋介面時才能直接搜尋；否則複製關鍵字後開啟。", "query_text" to "輸入搜尋關鍵字",
        "choose_app" to "選擇 App", "launch_app" to "開啟 App", "search_in_app" to "App 內搜尋",
        "copy_and_open" to "複製並開啟", "search_fallback" to "App 不支援公開搜尋入口；已複製關鍵字並開啟。",
        "query_required" to "請輸入關鍵字", "popup_photo" to "自訂本機圖片",
        "choose_photo" to "選擇圖片 / GIF / WebP", "remove_photo" to "還原水豚",
        "photo_hint" to "圖片只留在裝置內。", "popup_motion" to "動態效果",
        "motion_still" to "靜止", "motion_breathe" to "輕柔呼吸", "motion_bob" to "彈跳",
        "remaining" to "剩餘", "break_remaining" to "休息剩餘", "not_monitoring" to "目前沒有受限 App"
    );
    private val newJa=mapOf(
        "back" to "戻る", "exit_interface" to "画面を閉じる（監視は継続）",
        "select_all" to "すべて選択", "invert" to "選択を反転", "select_none" to "すべて解除",
        "bulk_scope" to "現在の絞り込み対象に適用", "quick_search" to "アプリ起動・検索",
        "quick_search_hint" to "インストール済みアプリを検索", "quick_search_explain" to "公開検索 API がないアプリはキーワードをコピーして開きます。", "query_text" to "検索キーワード",
        "choose_app" to "アプリを選択", "launch_app" to "開く",
        "search_in_app" to "アプリ内検索", "copy_and_open" to "コピーして開く",
        "search_fallback" to "このアプリは公開検索に非対応のため、キーワードをコピーして起動しました。",
        "query_required" to "キーワードを入力してください",
        "popup_photo" to "カスタム画像（ローカル）", "choose_photo" to "画像 / GIF / WebP を選ぶ",
        "remove_photo" to "カピバラに戻す", "photo_hint" to "画像は端末内のみ。",
        "popup_motion" to "動き", "motion_still" to "静止",
        "motion_breathe" to "呼吸", "motion_bob" to "ジャンプ",
        "remaining" to "残り", "break_remaining" to "休憩残り", "not_monitoring" to "制限アプリは未使用"
    );

    private val widgetEn=mapOf(
        "add_widget" to "Add home screen search widget",
        "widget_manual" to "Long-press desktop → Widgets / More widgets → BaraPause",
        "quick_search_widget" to "Tap search to enter keywords"
    )
    private val widgetZh=mapOf(
        "add_widget" to "添加桌面原子 / 搜索组件",
        "widget_manual" to "长按桌面 → 小组件 / 原子组件 → 更多组件 → BaraPause",
        "quick_search_widget" to "点击搜索框输入关键词"
    )
    private val widgetTw=mapOf(
        "add_widget" to "加入桌面搜尋小工具",
        "widget_manual" to "長按桌面 → 小工具 / 更多元件 → BaraPause",
        "quick_search_widget" to "點擊搜尋以輸入關鍵字"
    )
    private val widgetJa=mapOf(
        "add_widget" to "ホーム画面の検索ウィジェットを追加",
        "widget_manual" to "ホーム画面を長押し → ウィジェット → BaraPause",
        "quick_search_widget" to "タップして検索語を入力"
    )

    fun t(context: Context, key: String): String {
        val v = when (language(context)) { "zh-Hans" -> zh[key]; "zh-Hant" -> tw[key]; "ja" -> ja[key]; else -> en[key] }
        val loc=language(context)
        val p=when(loc) { "zh-Hans" -> popupZh[key]; "zh-Hant" -> popupTw[key]; "ja" -> popupJa[key]; else -> popupEn[key] }
        val monitor=when(loc) { "zh-Hans" -> monitorZh[key]; "zh-Hant" -> monitorTw[key]; "ja" -> monitorJa[key]; else -> monitorEn[key] }
        val n=when(loc) { "zh-Hans" -> newZh[key]; "zh-Hant" -> newTw[key]; "ja" -> newJa[key]; else -> newEn[key] }
        val widget=when(loc) { "zh-Hans" -> widgetZh[key]; "zh-Hant" -> widgetTw[key]; "ja" -> widgetJa[key]; else -> widgetEn[key] }
        return v ?: p ?: monitor ?: n ?: widget ?: en[key] ?: popupEn[key] ?: monitorEn[key] ?: newEn[key] ?: widgetEn[key] ?: key
    }
}
