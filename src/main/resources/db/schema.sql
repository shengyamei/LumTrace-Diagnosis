-- ============================================================
-- AI就医便民小助手 数据库初始化脚本 (V1.0)
-- MySQL 8.0 | 严格按照项目架构说明书建表
-- ============================================================
CREATE DATABASE IF NOT EXISTS lumtrace_diagnosis DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lumtrace_diagnosis;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    username     VARCHAR(64)  NOT NULL UNIQUE COMMENT '账号',
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt加密存储',
    nickname     VARCHAR(64) COMMENT '昵称',
    phone        VARCHAR(20) COMMENT '手机号',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status       TINYINT DEFAULT 1 COMMENT '0禁用 1启用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 病程记录表
DROP TABLE IF EXISTS disease_record;
CREATE TABLE disease_record (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    record_time  DATETIME NOT NULL COMMENT '记录时间',
    symptom      VARCHAR(512) COMMENT '症状描述',
    temperature  DECIMAL(4,1) COMMENT '体温',
    pain_score   TINYINT COMMENT '疼痛评分1-10',
    remark       TEXT COMMENT '备注',
    img_url      VARCHAR(512) COMMENT '照片URL(后置)',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_time (user_id, record_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病程记录表';

-- 3. 医嘱表
DROP TABLE IF EXISTS doctor_order;
CREATE TABLE doctor_order (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    drug_name    VARCHAR(128) NOT NULL COMMENT '药品名称',
    dosage       VARCHAR(64) COMMENT '剂量',
    frequency    VARCHAR(64) COMMENT '频次(如: 每天2次)',
    remind_time  VARCHAR(32) COMMENT '提醒时间(如: 08:00,20:00)',
    start_date   DATE NOT NULL COMMENT '开始日期',
    end_date     DATE NOT NULL COMMENT '结束日期',
    is_finish    TINYINT DEFAULT 0 COMMENT '是否已完成整个疗程 0否 1是',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_date_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医嘱表';

-- 4. 提醒记录表
DROP TABLE IF EXISTS remind_log;
CREATE TABLE remind_log (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id     BIGINT NOT NULL COMMENT '医嘱ID',
    user_id      BIGINT NOT NULL COMMENT '用户ID',
    remind_time  DATETIME NOT NULL COMMENT '提醒时间',
    is_read      TINYINT DEFAULT 0 COMMENT '是否已读 0否 1是',
    is_taken     TINYINT DEFAULT 0 COMMENT '是否已服药 0否 1是',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_order (order_id),
    KEY idx_user_time (user_id, remind_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒记录表';

-- 5. 科室分诊规则表
DROP TABLE IF EXISTS dept_rule;
CREATE TABLE dept_rule (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword      VARCHAR(128) NOT NULL COMMENT '症状关键词',
    dept_name    VARCHAR(64) NOT NULL COMMENT '推荐科室',
    tip          VARCHAR(512) COMMENT '就医提示',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室分诊规则表';

-- 6. 药品饮食禁忌表
DROP TABLE IF EXISTS drug_food_rule;
CREATE TABLE drug_food_rule (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_name    VARCHAR(128) NOT NULL COMMENT '药品名称',
    forbid_food  VARCHAR(512) COMMENT '禁忌食物',
    suggest_food VARCHAR(512) COMMENT '推荐饮食',
    tip          VARCHAR(512) COMMENT '提示说明',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_drug (drug_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品饮食禁忌表';

-- 7. 公告与免责声明表
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    content      TEXT NOT NULL COMMENT '内容',
    type         VARCHAR(32) DEFAULT 'DISCLAIMER' COMMENT '类型: DISCLAIMER/NOTICE',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告与免责声明';

-- ============================================================
-- 初始化演示数据
-- ============================================================

-- 用户数据（密码BCrypt加密后的密文，明文为123456）
INSERT INTO `user`(username, password, nickname, phone) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', NULL),
('patient01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '演示患者', '13800000001'),
('patient02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张阿姨', '13800000002');

-- 免责声明
INSERT INTO sys_notice(content, type) VALUES
('本应用内容仅供参考，不能替代执业医师诊疗，如有紧急情况请立即拨打120。', 'DISCLAIMER'),
('AI导诊结果仅作为就医科室参考，不做疾病诊断，不替代医生面诊。', 'DISCLAIMER');

-- 科室分诊规则数据（首批40条，按科室建类目）
INSERT INTO dept_rule(keyword, dept_name, tip) VALUES
-- 呼吸系统
('咳嗽', '呼吸内科', '建议多饮温水，如持续超过一周请尽早就医'),
('咳痰', '呼吸内科', '注意观察痰液颜色，如有血丝立即就医'),
('气喘', '呼吸内科', '避免剧烈运动，及时就医'),
('喉咙痛', '耳鼻喉科', '忌辛辣刺激食物，多饮温水'),
('鼻塞', '耳鼻喉科', '可用热毛巾敷鼻部缓解，持续不适应就医'),
('流涕', '耳鼻喉科', '注意保暖，如伴有发热建议就诊'),
('打鼾', '耳鼻喉科', '建议侧卧睡眠，严重影响生活质量请就诊'),
('打喷嚏', '耳鼻喉科', '注意是否为过敏引起，持续发作建议就诊'),
-- 消化系统
('腹痛', '消化内科', '避免进食刺激性食物，持续疼痛请就医'),
('腹泻', '消化内科', '注意补充水分防止脱水，超过3天请就医'),
('恶心', '消化内科', '清淡饮食为主，避免油腻'),
('呕吐', '消化内科', '暂缓进食，少量多次饮水，剧烈呕吐请就医'),
('胃胀', '消化内科', '细嚼慢咽，避免产气食物'),
('反酸', '消化内科', '避免咖啡、浓茶及过甜食物'),
('便秘', '消化内科', '多食蔬果和粗粮，增加饮水量'),
('食欲不振', '消化内科', '少量多餐，如持续加重请就医'),
-- 心血管
('胸闷', '心血管内科', '注意休息避免劳累，频繁发作请立即就医'),
('心悸', '心血管内科', '避免咖啡因饮料，如伴胸痛请立即拨打120'),
('胸痛', '心血管内科', '警惕心梗可能，如有压榨感请立即拨打120'),
('头痛', '神经内科', '注意休息，避免精神紧张，持续剧烈头痛请就医'),
('头晕', '神经内科', '避免突然起身，频繁发作建议就诊'),
-- 骨科
('腰痛', '骨科', '避免久坐，可用硬板床休息，持续加重请就诊'),
('关节痛', '骨科', '注意关节保暖，避免过度负重'),
('脖子痛', '骨科', '避免长时间低头，适当活动颈部'),
('肩痛', '骨科', '避免提重物，注意肩部保暖'),
-- 皮肤
('皮疹', '皮肤科', '避免搔抓，忌海鲜辛辣，扩散加快请就诊'),
('瘙痒', '皮肤科', '避免热水烫洗，保持皮肤湿润'),
('脱发', '皮肤科', '注意作息规律，减少烫染'),
-- 内分泌
('多饮多尿', '内分泌科', '建议检查血糖，注意饮食控制'),
('体重骤降', '内分泌科', '如非刻意减肥请尽快就医排查'),
('怕冷乏力', '内分泌科', '注意保暖，建议检查甲状腺功能'),
-- 眼科
('眼红', '眼科', '避免揉眼，注意用眼卫生，持续不退请就诊'),
('视力模糊', '眼科', '避免长时间用眼，突然视力下降请立即就医'),
('眼睛干涩', '眼科', '适当休息，可使用人工泪液缓解'),
-- 口腔
('牙痛', '口腔科', '避免冷热刺激，尽快就诊处理'),
('牙龈出血', '口腔科', '使用软毛牙刷，持续出血建议就诊'),
-- 泌尿
('尿频', '泌尿外科', '注意局部卫生，多饮水，持续不适请就诊'),
('尿痛', '泌尿外科', '多饮水冲洗尿道，及时就医'),
('浮肿', '肾内科', '限制盐分摄入，明显浮肿请尽快就诊'),
-- 精神/睡眠
('失眠', '神经内科', '规律作息，避免睡前使用电子设备，长期失眠请就诊'),
('焦虑', '精神心理科', '适当运动放松，影响日常生活建议寻求专业帮助'),
-- 感冒通用
('发烧', '全科/发热门诊', '体温超过38.5℃建议物理降温并及时就医'),
('全身酸痛', '全科', '多休息多饮水，如伴高热请就医');

-- 药品饮食禁忌数据（首批25种常用药）
INSERT INTO drug_food_rule(drug_name, forbid_food, suggest_food, tip) VALUES
('头孢克肟', '酒精及含酒精饮料、酒心巧克力', '清淡饮食、多饮水', '头孢类药物与酒精可导致双硫仑样反应，严重可危及生命'),
('头孢拉定', '酒精及含酒精食品', '清淡饮食、维生素丰富蔬果', '服药期间及停药后7天内严禁饮酒'),
('阿莫西林', '酸性食物(柑橘、醋)', '温和易消化食物', '酸性环境可能降低药效'),
('甲硝唑', '酒精类饮品', '清淡饮食', '与酒精同服可引起严重不良反应'),
('布洛芬', '空腹、辛辣食物、咖啡', '饭后温水送服', '空腹服用可能刺激胃黏膜'),
('阿司匹林', '酸性食物、酒', '饭后服用', '空腹或饮酒增加胃出血风险'),
('对乙酰氨基酚', '酒精', '正常饮食', '与酒精同服增加肝损伤风险'),
('奥美拉唑', '油腻食物', '清淡、易消化', '饭前30分钟服用效果最佳'),
('蒙脱石散', '其他药物(需间隔)', '温水冲服', '需与其他药物间隔1-2小时服用'),
('氯雷他定', '葡萄柚汁', '正常饮食', '葡萄柚汁可能影响药物代谢'),
('阿奇霉素', '乳制品(牛奶)', '饭后或空腹均可', '乳制品可能影响吸收'),
('左氧氟沙星', '乳制品、含铁食物', '多饮水', '与钙铁同服降低药物吸收'),
('氨氯地平', '西柚/葡萄柚', '低盐饮食', '西柚影响降压药代谢，可能导致低血压'),
('卡托普利', '高钾食物(香蕉、橙子)', '低盐饮食', '可能引起血钾升高'),
('二甲双胍', '酒精、高糖食物', '控制碳水化合物摄入', '饮酒增加乳酸酸中毒风险'),
('格列本脲', '高糖食物', '定时定量进餐', '不按时进餐可能引起低血糖'),
('硝酸甘油', '酒精、西柚', '低脂饮食', '与酒精同服可能导致严重低血压'),
('华法林', '维生素K丰富食物(菠菜、甘蓝)', '饮食均衡稳定', '大量摄入维生素K可降低抗凝效果'),
('地高辛', '高纤维食物、乳制品', '含钾丰富食物', '需监测血钾，避免低钾'),
('泼尼松', '高盐高糖食物', '高蛋白、高钙、低盐', '长期服用注意补钙'),
('阿托伐他汀', '西柚/葡萄柚', '低脂饮食', '西柚可增加药物浓度导致肌病风险'),
('氯吡格雷', '酒精', '清淡饮食', '饮酒可能增加出血风险'),
('叶酸', '浓茶、咖啡', '富含铁质食物', '茶和咖啡影响叶酸吸收'),
('碳酸钙', '高草酸食物(菠菜)', '含维生素D食物', '草酸影响钙吸收，随餐服用效果好'),
('多潘立酮', '高脂食物', '饭前15-30分钟服用', '饭后服用影响吸收');
