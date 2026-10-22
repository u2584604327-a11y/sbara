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
    fun t(context: Context, key: String): String {
        val v = when (language(context)) { "zh-Hans" -> zh[key]; "zh-Hant" -> tw[key]; "ja" -> ja[key]; else -> en[key] }
        return v ?: en[key] ?: key
    }
}
