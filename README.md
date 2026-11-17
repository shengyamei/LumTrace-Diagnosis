LumTrace‑Diagnosis（溯光智诊）
> 软件创新大赛 AI+健康赛道｜AI就医便民小助手
> 中文队名：溯光智诊｜英文队名：LumTrace
> 本系统为**就医便民辅助工具，不做疾病诊断、不开处方、不能替代执业医师**

📖 项目简介
AI就医便民小助手面向普通患者、中老年、慢性病群体，围绕**就诊前‑就诊中‑就诊后**全周期，提供4项核心能力：
1. AI科室导诊分诊（规则库优先 + 大模型兜底）
2. 病情病程记录与文本导出
3. 医嘱录入 + 服药定时提醒打卡
4. 药品‑饮食禁忌知识库查询

架构采用五层前后端分离：`Android客户端 ↔ SpringBoot后端 ↔ AI能力层 ↔ MySQL持久层 ↔ 外部对接层`。
核心亮点：**规则引擎优先抵御大模型幻觉，支持断网离线降级演示，适配大赛现场演示要求**。

> 免责声明：本应用内容仅供参考，不能替代执业医师诊疗，如有紧急情况请立即拨打 120。

🛠 技术栈
后端（本仓库）
- Java 17 + SpringBoot 4.0.x
- Maven
- MyBatis‑Plus（持久层）
- MySQL 8.0
- JWT 登录鉴权
- BCrypt 密码加密
- Spring Scheduler 定时任务（医嘱提醒）
- Lombok
- RESTful API
- 规则引擎 + LLM API（通义千问/文心一言，可替换Qwen本地轻量模型）

客户端（独立Android / uni‑app APK）
- uni‑app（Vue）编译APK / Kotlin Compose原生二选一
- Android本地通知 AlarmManager + WorkManager
- 适老化UI设计

📁 仓库目录简要说明
```
LumTrace‑Diagnosis
├── src/main/java/org/csu/lumtrace       # 业务源码
│   ├── config        # JWT、定时任务、全局配置
│   ├── controller    # 接口控制器
│   ├── service       # 业务逻辑层
│   ├── mapper        # MyBatis Mapper
│   ├── entity        # 数据库实体
│   ├── dto / vo      # 请求/返回对象
│   ├── util          # 工具类
│   ├── filter        # 鉴权、输出过滤（医疗敏感词）
│   └── ai            # AI层：规则引擎、大模型调用封装
├── src/main/resources
│   ├── application.yml      # 数据库、定时任务、LLM配置
│   └── mybatis              # mapper xml文件
├── sql
│   └── init_schema.sql      # 建表初始化脚本（7张业务表）
├── pom.xml
└── README.md
```

数据库核心表：`user`、`disease_record`病程记录、`doctor_order`医嘱、`remind_log`提醒日志、`dept_rule`分诊规则库、`drug_food_rule`药品禁忌库、`sys_notice`公告免责。

🚀 快速启动
1. 环境准备
- JDK17
- Maven 3.8+
- MySQL8.0，新建数据库 `lum_trace_db`

2. 数据库初始化
执行 `sql/init_schema.sql`，完成建表，可导入演示测试数据。

3. 修改配置文件 `application.yml`
修改数据库账号密码；配置大模型API‑Key（不需要大模型可开启离线降级开关）。
```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/lum_trace_db?useUnicode=true&characterEncoding=utf8
    username: 你的mysql账号
    password: 你的mysql密码
# ai:
#   llm‑api‑key: "填入你的key"
#   offline‑mode: false # true=完全离线，仅本地规则库运行
```

4. 运行项目
```bash
mvn clean install
# 运行主启动类 LumTraceDiagnosisApplication
```
服务默认端口：`8080`

5. 接口访问
后端接口前缀示例：`[http://127.0.0.1:8080/api/xxx](http://127.0.0.1:8080/api/xxx)`
> 建议使用Postman / Apifox导入接口文档进行调试。

📅 开发计划（硬截止：2026‑12‑15交付半成品可演示版本）
> 文档参考：AI医疗便民小助手项目架构说明书.docx
- W1 11‑17 ~ 11‑22：环境搭建，建表，接口文档冻结，知识库初稿
- W2 11‑23 ~ 11‑29：用户模块、分诊、禁忌接口开发
- W3 11‑30 ~ 12‑06：病程记录、医嘱定时提醒模块，部署双环境
- W4 12‑07 ~12‑13：全链路联调，断网降级、缺陷修复
- 12‑14 ~12‑15：版本冻结，备份，半成品验收，现场演示彩排

✅ 12‑15半成品验收要点
1. AI导诊分诊完整闭环（规则优先，大模型兜底，输出过滤）
2. 用药饮食禁忌查询完整闭环
3. 病程记录可录入、时间线展示、文本导出
4. 医嘱录入 + 服务端定时提醒，Android本地通知可用
5. 登录鉴权正常，首页免责声明；离线降级模式可切换

> 非核心功能（照片上传、PDF导出、统计报表等）后置到第二批次开发。

👥 团队成员分工
- 成员A｜队长 / 后端架构：数据库建模、后端核心模块、部署、演示保障
- 成员B｜Android客户端负责人：页面交互、适老化、本地通知、APK打包
- 成员C｜AI能力&测试：规则引擎、大模型接入、敏感词过滤、测试与演示脚本
- 成员D｜医学&合规：分诊/药品知识库编纂、医学审核、合规文案、可用性测试

⚠️ 重要约束与风险说明
1. **本系统禁止输出疾病诊断、治疗方案、处方建议；所有输出必须附带参考免责提示。**
2. AI层架构策略：**本地规则库优先命中，大模型仅作为兜底增强；后端做敏感词拦截过滤。**
3. 大赛现场支持断网离线模式：关闭大模型调用，仅依靠内置规则库完成演示。

📦 Git提交规范
```
feat: 新增功能
fix: 修复bug
refactor: 重构
docs: 文档更新
init: 项目初始化
```
> 主干main分支保证可编译运行；新功能建议开feature分支开发，完成后合并main。

📄 License
本项目用于软件创新大赛参赛，仅供学习与竞赛演示使用。
