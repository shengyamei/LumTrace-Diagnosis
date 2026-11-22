package org.csu.lumtrace.lumtracediagnosis.ai;

import org.csu.lumtrace.lumtracediagnosis.entity.DeptRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分诊规则匹配原型单元测试（成员C / W1 验收物）。
 * <p>
 * 不依赖数据库：直接构造规则列表，验证匹配算法本身。
 */
class DeptTriageEngineTest {

    /** 与 schema.sql 首批种子数据同构的最小规则集 */
    private static final List<DeptRule> RULES = List.of(
            rule("喉咙疼", "耳鼻喉科", "建议避免辛辣刺激饮食，多饮水"),
            rule("咽痛", "耳鼻喉科", "建议避免辛辣刺激饮食，多饮水"),
            rule("发烧", "发热门诊", "请先测量并记录体温"),
            rule("咳嗽", "呼吸内科", "注意休息与保暖"),
            rule("反酸", "消化内科", "建议清淡饮食"),
            rule("胸闷", "心血管内科", "胸闷伴大汗请立即就医"),
            rule("心慌", "心血管内科", "建议心内科评估心率"),
            rule("皮疹", "皮肤科", "避免抓挠与热水烫洗")
    );

    @Test
    @DisplayName("说明书功能1用例：反复喉咙疼、发烧3天 → 耳鼻喉科")
    void shouldTriageSoreThroat() {
        DeptTriageEngine.TriageResult result = DeptTriageEngine.match("反复喉咙疼、发烧3天", RULES);

        assertNotNull(result);
        assertEquals("耳鼻喉科", result.deptName());
        assertTrue(result.hitKeywords().contains("喉咙疼"));
        assertEquals("建议避免辛辣刺激饮食，多饮水", result.tip());
    }

    @Test
    @DisplayName("多关键词命中同一科室时得分累加")
    void shouldAccumulateScoreInSameDept() {
        DeptTriageEngine.TriageResult result = DeptTriageEngine.match("有点胸闷，还老是心慌", RULES);

        assertNotNull(result);
        assertEquals("心血管内科", result.deptName());
        assertEquals(List.of("胸闷", "心慌"), result.hitKeywords());
        assertEquals(4, result.score());
    }

    @Test
    @DisplayName("长关键词优先：心口疼 不应被泛化的「疼」规则抢走")
    void shouldPreferLongerKeyword() {
        List<DeptRule> rules = List.of(
                rule("疼", "全科门诊", "可以先挂全科"),
                rule("心口疼", "心血管内科", "胸闷心痛请尽快就医")
        );

        DeptTriageEngine.TriageResult result = DeptTriageEngine.match("我今天心口疼得厉害", rules);

        assertNotNull(result);
        assertEquals("心血管内科", result.deptName());
    }

    @Test
    @DisplayName("无任何关键词命中时返回 null，交由大模型兜底")
    void shouldReturnNullWhenNoHit() {
        assertNull(DeptTriageEngine.match("最近总是丢三落四", RULES));
    }

    @Test
    @DisplayName("空主诉与空规则库安全返回 null")
    void shouldHandleEmptyInput() {
        assertNull(DeptTriageEngine.match("   ", RULES));
        assertNull(DeptTriageEngine.match(null, RULES));
        assertNull(DeptTriageEngine.match("随便说点什么", List.of()));
    }

    private static DeptRule rule(String keyword, String deptName, String tip) {
        DeptRule r = new DeptRule();
        r.setKeyword(keyword);
        r.setDeptName(deptName);
        r.setTip(tip);
        return r;
    }
}
