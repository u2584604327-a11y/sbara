fun main() {
    val checks = listOf(
        RuleMath.effectiveLimitMs(20, false) == 1_200_000L,
        RuleMath.effectiveLimitMs(20, true) == 600_000L,
        RuleMath.effectiveLimitMs(1, true) == 60_000L,
        RuleMath.isMinuteInsideWindow(19 * 60, 19 * 60, 22 * 60),
        !RuleMath.isMinuteInsideWindow(18 * 60 + 59, 19 * 60, 22 * 60),
        RuleMath.isMinuteInsideWindow(23 * 60, 22 * 60, 60),
        RuleMath.isMinuteInsideWindow(30, 22 * 60, 60),
        !RuleMath.isMinuteInsideWindow(12 * 60, 22 * 60, 60),
        RuleMath.isMinuteInsideWindow(12 * 60, 0, 0)
    )
    require(checks.all { it }) { "CORE_RULE_TESTS=FAIL" }
    println("CORE_RULE_TESTS=PASS_9_OF_9")
}
