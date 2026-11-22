-- LumTrace-Diagnosis 数据库初始化脚本
-- MySQL 8.x
CREATE DATABASE IF NOT EXISTS lumtrace_diagnosis DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lumtrace_diagnosis;

-- 用户
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    username     VARCHAR(64)  NOT NULL UNIQUE,
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt或SHA-256摘要',
    nickname     VARCHAR(64),
    phone        VARCHAR(20),
    email        VARCHAR(64),
    gender       TINYINT DEFAULT 0 COMMENT '0未知 1男 2女',
    birth_date   DATE,
    role         VARCHAR(16) DEFAULT 'PATIENT' COMMENT 'PATIENT/DOCTOR/ADMIN',
    status       TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 医生
DROP TABLE IF EXISTS t_doctor;
CREATE TABLE t_doctor (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT,
    name         VARCHAR(64) NOT NULL,
    department   VARCHAR(64),
    title        VARCHAR(32),
    hospital     VARCHAR(128),
    specialty    VARCHAR(255),
    introduction TEXT,
    status       TINYINT DEFAULT 1,
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_dept (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生表';

-- 症状字典
DROP TABLE IF EXISTS t_symptom;
CREATE TABLE t_symptom (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    name              VARCHAR(64) NOT NULL UNIQUE,
    category          VARCHAR(64),
    description       VARCHAR(255),
    default_severity  TINYINT DEFAULT 1,
    status            TINYINT DEFAULT 1,
    create_time       DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='症状字典';

-- 科室分诊规则库（架构说明书 2.5 dept_rule：规则库优先命中，不经大模型）
DROP TABLE IF EXISTS t_dept_rule;
CREATE TABLE t_dept_rule (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword    VARCHAR(64) NOT NULL COMMENT '症状关键词，口语化优先',
    dept_name  VARCHAR(64) NOT NULL COMMENT '推荐科室',
    tip        VARCHAR(255) COMMENT '就医提示',
    KEY idx_keyword (keyword),
    KEY idx_dept (dept_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室分诊规则库';

-- 医学知识库：疾病-症状权重
DROP TABLE IF EXISTS t_medical_knowledge;
CREATE TABLE t_medical_knowledge (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    disease_id    BIGINT,
    disease_name  VARCHAR(64) NOT NULL,
    symptom_id    BIGINT NOT NULL,
    weight        DECIMAL(4,3) DEFAULT 0.500,
    required      TINYINT DEFAULT 0,
    KEY idx_disease (disease_name),
    KEY idx_symptom (symptom_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医学知识库';

-- 诊断记录
DROP TABLE IF EXISTS t_diagnosis_record;
CREATE TABLE t_diagnosis_record (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id             BIGINT NOT NULL,
    symptom_ids         VARCHAR(512),
    self_description    TEXT,
    age                 INT,
    gender              TINYINT,
    ai_disease          VARCHAR(64),
    confidence          DECIMAL(4,3),
    ai_suggestion       TEXT,
    candidate_diseases  TEXT COMMENT 'JSON数组',
    status              VARCHAR(16) DEFAULT 'PENDING',
    create_time         DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI诊断记录';

-- 诊断报告
DROP TABLE IF EXISTS t_diagnosis_report;
CREATE TABLE t_diagnosis_report (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id      BIGINT NOT NULL,
    user_id        BIGINT NOT NULL,
    report_no      VARCHAR(64) UNIQUE,
    content        TEXT,
    doctor_id      BIGINT,
    doctor_review  TEXT,
    status         VARCHAR(32) DEFAULT 'AI_ONLY',
    create_time    DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user (user_id),
    KEY idx_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='诊断报告';

-- ===================  初始化数据  ===================
-- 常见症状
INSERT INTO t_symptom(name, category, description, default_severity) VALUES
('发热',     '全身',    '体温超过37.3℃',           2),
('咳嗽',     '呼吸系统','刺激性或咳痰',             1),
('咽痛',     '呼吸系统','咽喉疼痛或肿胀',           1),
('流涕',     '呼吸系统','鼻塞或清水样鼻涕',         1),
('头痛',     '神经系统','头部钝痛或搏动性痛',       1),
('肌肉酸痛', '肌肉骨骼','全身酸痛乏力',             1),
('腹泻',     '消化系统','大便次数增多或稀便',       2),
('呕吐',     '消化系统','胃内容物反流',             2),
('腹痛',     '消化系统','腹部隐痛或绞痛',           2),
('胸闷',     '心血管',  '胸部压迫感',               3),
('气促',     '呼吸系统','呼吸费力或加快',           3),
('皮疹',     '皮肤',    '皮肤红斑或瘙痒',           1),
('失眠',     '神经系统','入睡困难或多梦',           1),
('乏力',     '全身',    '精神不振易疲劳',           1);

-- 常见疾病 + 权重（示例：普通感冒、流感、急性胃肠炎、心绞痛）
-- 假设 symptom_id 按插入顺序：1发热 2咳嗽 3咽痛 4流涕 5头痛 6肌肉酸痛 7腹泻 8呕吐 9腹痛 10胸闷 11气促 12皮疹 13失眠 14乏力
INSERT INTO t_medical_knowledge(disease_name, symptom_id, weight, required) VALUES
-- 普通感冒
('普通感冒', 2, 0.9, 1), ('普通感冒', 3, 0.85, 0), ('普通感冒', 4, 0.9, 1), ('普通感冒', 1, 0.4, 0),
-- 流行性感冒
('流行性感冒', 1, 0.95, 1), ('流行性感冒', 6, 0.85, 0), ('流行性感冒', 5, 0.8, 0), ('流行性感冒', 2, 0.7, 0), ('流行性感冒', 14, 0.8, 0),
-- 急性胃肠炎
('急性胃肠炎', 7, 0.9, 1), ('急性胃肠炎', 8, 0.8, 0), ('急性胃肠炎', 9, 0.85, 1), ('急性胃肠炎', 1, 0.5, 0),
-- 心绞痛
('心绞痛', 10, 0.95, 1), ('心绞痛', 11, 0.75, 0), ('心绞痛', 5, 0.4, 0);

-- 科室分诊规则首批样例（原型用，正式 40 条由成员D提供并医学复核）
INSERT INTO t_dept_rule(keyword, dept_name, tip) VALUES
('喉咙疼',   '耳鼻喉科',   '建议避免辛辣刺激饮食，多饮水，症状持续超过一周请及时就诊'),
('嗓子疼',   '耳鼻喉科',   '建议避免辛辣刺激饮食，多饮水，症状持续超过一周请及时就诊'),
('咽痛',     '耳鼻喉科',   '建议避免辛辣刺激饮食，多饮水，伴随高热时尽快就诊'),
('扁桃体',   '耳鼻喉科',   '建议耳鼻喉科面诊，不要自行使用抗生素'),
('耳鸣',     '耳鼻喉科',   '建议耳鼻喉科检查听力与耳道情况'),
('咳嗽',     '呼吸内科',   '注意休息与保暖，痰中带血或持续超过两周请尽快就诊'),
('咳痰',     '呼吸内科',   '注意休息与保暖，建议记录痰液颜色与量供医生参考'),
('气促',     '呼吸内科',   '出现呼吸困难请立即就医，不要拖延'),
('喘不上气', '呼吸内科',   '出现呼吸困难请立即就医或拨打 120'),
('发烧',     '发热门诊',   '请先测量并记录体温，持续高热请尽快到发热门诊就诊'),
('发热',     '发热门诊',   '请先测量并记录体温，持续高热请尽快到发热门诊就诊'),
('腹痛',     '消化内科',   '记录疼痛位置与持续时间，剧烈腹痛请立即急诊'),
('肚子疼',   '消化内科',   '记录疼痛位置与持续时间，剧烈腹痛请立即急诊'),
('腹泻',     '消化内科',   '注意补充水分与电解质，出现脱水症状请及时就诊'),
('拉肚子',   '消化内科',   '注意补充水分与电解质，出现脱水症状请及时就诊'),
('反酸',     '消化内科',   '建议清淡饮食，避免饱餐后立即平卧'),
('胸闷',     '心血管内科', '胸闷伴大汗或放射痛请立即拨打 120'),
('心口疼',   '心血管内科', '胸闷心痛请立即停止活动并尽快就医'),
('心慌',     '心血管内科', '建议心内科评估心率与心电图'),
('心悸',     '心血管内科', '建议心内科评估心率与心电图'),
('头痛',     '神经内科',   '突发剧烈头痛或伴呕吐请立即急诊'),
('头晕',     '神经内科',   '起身时动作放慢，反复发作建议神经内科就诊'),
('手麻',     '神经内科',   '注意记录麻木部位与持续时间'),
('皮疹',     '皮肤科',     '避免抓挠与热水烫洗，拍摄患处照片便于面诊'),
('瘙痒',     '皮肤科',     '避免抓挠与热水烫洗，不建议自行外用药物'),
('起红疹',   '皮肤科',     '避免抓挠，记录是否接触新食物或物品'),
('腰痛',     '骨科',       '避免久坐与负重，出现下肢麻木请尽快就诊'),
('关节痛',   '骨科',       '减少负重活动，急性肿胀期可先冷敷'),
('扭伤',     '骨科',       '损伤 48 小时内冷敷并制动，明显肿痛请拍片排查'),
('颈椎',     '骨科',       '避免长时间低头，出现手麻请尽快就诊'),
('尿频',     '泌尿外科',   '注意多饮水，伴尿痛或血尿请尽快就诊'),
('尿痛',     '泌尿外科',   '注意多饮水，伴发热请尽快就诊'),
('血尿',     '泌尿外科',   '出现血尿请尽快到泌尿外科就诊'),
('眼睛红',   '眼科',       '避免揉眼与佩戴隐形眼镜，视力下降请立即就诊'),
('视物模糊', '眼科',       '视力突然下降请立即急诊'),
('眼痛',     '眼科',       '眼痛伴头痛恶心请立即急诊排查');

-- 默认管理员/示例用户（密码为明文，实际请替换成BCrypt摘要）
INSERT INTO t_user(username, password, nickname, role) VALUES
('admin',    '123456', '系统管理员', 'ADMIN'),
('patient01','123456', '演示患者',   'PATIENT');

INSERT INTO t_doctor(user_id, name, department, title, hospital, specialty) VALUES
(NULL, '张伟', '呼吸内科', '主治医师', '中南大学湘雅医院', '慢性咳嗽、哮喘'),
(NULL, '李静', '消化内科', '副主任医师', '湖南省人民医院', '胃肠炎、幽门螺杆菌'),
(NULL, '王强', '心血管内科', '主任医师', '中南大学湘雅二医院', '冠心病、心绞痛');
