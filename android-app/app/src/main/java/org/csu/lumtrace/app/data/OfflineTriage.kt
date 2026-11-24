package org.csu.lumtrace.app.data

/**
 * 离线兜底评估：接口不可用时，按症状关键词给出「建议就诊方向」，不做疾病诊断。
 */
object OfflineTriage {

    private data class Rule(val dept: String, val keywords: List<String>)

    private data class RuleScore(val dept: String, val count: Int, val names: List<String>)

    private val rules = listOf(
        Rule("呼吸内科", listOf("发热", "咳嗽", "咳痰", "咽痛", "鼻塞", "流鼻涕", "气短")),
        Rule("消化内科", listOf("腹痛", "腹泻", "恶心", "呕吐", "反酸", "食欲不振")),
        Rule("心血管内科", listOf("胸痛", "心悸", "胸闷")),
        Rule("神经内科", listOf("头痛", "眩晕", "失眠", "肢体麻木")),
        Rule("皮肤科", listOf("皮疹", "瘙痒")),
        Rule("泌尿外科", listOf("尿频", "尿痛")),
        Rule("骨科", listOf("关节痛", "腰背痛")),
        Rule("眼科", listOf("视物模糊")),
        Rule("耳鼻喉科", listOf("耳鸣", "咽痛", "鼻塞"))
    )

    fun evaluate(selected: List<SymptomDto>): DiagnosisResponse {
        if (selected.isEmpty()) {
            return DiagnosisResponse(
                topDisease = "未选择症状",
                confidence = 0.0,
                severity = 1,
                suggestion = "【离线演示】请先选择至少一个症状。"
            )
        }

        val total = selected.size.toDouble()
        val scores = rules.map { rule ->
            val hits = selected.filter { s -> rule.keywords.any { k -> s.name.contains(k) } }
            RuleScore(rule.dept, hits.size, hits.map { it.name })
        }.filter { it.count > 0 }.sortedByDescending { it.count }

        val candidates = scores.map { s ->
            CandidateDto(
                disease = s.dept + "方向",
                score = (s.count / total).coerceIn(0.0, 1.0),
                hitSymptoms = s.names
            )
        }

        val top = candidates.firstOrNull()
        val severity = selected.maxOfOrNull { it.defaultSeverity } ?: 1

        return DiagnosisResponse(
            recordId = 0,
            topDisease = top?.disease ?: "全科医学科方向",
            confidence = top?.score ?: 0.3,
            severity = severity,
            suggestion = buildSuggestion(severity, top?.disease),
            candidates = candidates
        )
    }

    private fun buildSuggestion(severity: Int, dept: String?): String {
        val base = when {
            severity >= 3 -> "症状较重，建议尽快前往医院就诊；如出现胸痛、呼吸困难、意识模糊，请立即拨打 120。"
            severity == 2 -> "建议近期到医院相关科室就诊，注意休息、多饮水，观察症状变化。"
            else -> "症状较轻，可先居家观察 2-3 天；若加重或出现新症状，请及时就医。"
        }
        val dir = if (dept != null) "可优先考虑" + dept + "（仅供参考）。" else ""
        return "【离线演示】" + dir + base
    }
}