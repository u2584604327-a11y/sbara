package app.barapause.mobile

data class AppRule(
    val packageName: String,
    val label: String,
    val limitMinutes: Int,
    val enabled: Boolean = true,
    val limitSeconds: Int = limitMinutes * 60
)
