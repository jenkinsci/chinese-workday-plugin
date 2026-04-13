# Agent Guide

## 项目定位

- 这是一个 Jenkins 插件项目，使用 Maven 构建，产物类型为 `hpi`。
- 仓库已经从模板骨架切换到 Chinese Workday 插件实现，当前已具备可用的 Freestyle、Pipeline 和系统配置能力。
- 当前已内置 2020 至 2026 年中国节假日与调休规则；管理员也可以在 `Manage Jenkins -> System -> Chinese Workday` 中补充或覆盖年份配置。
- 仍保留 `$JENKINS_HOME/chinese-workday/calendars/` 文件覆盖机制作为兼容路径，但这不再是首选入口。
- 优先保持 Jenkins 插件的标准目录结构、构建链路和测试链路可用。

## 技术栈

- Java / JDK：`17+`
- Maven：`3.9.6+`
- Jenkins Plugin Parent：`org.jenkins-ci.plugins:plugin:6.2138.v03274d462c13`
- 以上版本约束已从 parent pom 确认：
  - `maven.compiler.release=17`
  - `requireMavenVersion=[3.9.6,)`

## 当前工作原则

- 不要把模板示例代码误认为正式业务实现。
- 新增功能时，优先围绕“Chinese Workday”插件目标设计，不要重新引入模板式示例逻辑。
- 修改功能时，保持 Java 类、Jelly 页面、国际化资源、测试用例同步更新。
- 面向管理员的年份维护能力，优先走 Jenkins 系统配置入口；文件覆盖机制仅作为兼容方案维护。
- 涉及年份数据加载或覆盖顺序时，遵循“内置资源 < 外部文件 < 系统配置”的优先级。
- 如无必要，不要大规模重命名或清理模板文件；优先渐进式替换。

## 目录约定

- `pom.xml`：Maven 与 Jenkins 插件配置
- `Jenkinsfile`：CI 构建入口
- `src/main/java/`：插件 Java 代码
- `src/main/resources/`：Jelly 页面、帮助文档、国际化资源
- `src/test/java/`：单元测试与 JenkinsRule 测试
- `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/`：内置年份日历资源
- `src/main/resources/io/jenkins/plugins/chinese_workday/config/ChineseWorkdayGlobalConfiguration/`：系统配置页面与帮助文档
- `README.md`：对外说明与安装、配置、使用文档

## 开发建议

- 新功能优先做成可测试的 Jenkins Step、Builder 或相关扩展点。
- 当前同时提供 Builder 和专用 Pipeline Step；涉及 Pipeline 结果消费时，优先使用 `isChineseWorkday` 或 `isChineseHoliday`；涉及年份发现时使用 `chineseWorkdaySupportedYears`。
- 涉及管理员维护年份时，优先扩展 `ChineseWorkdayGlobalConfiguration` 与相关数据模型，而不是继续增加必须登录服务器改文件的路径。
- 每增加一个配置项，同时补齐：
  - 数据绑定
  - 配置页面
  - 校验逻辑
  - 文案资源
  - 测试
- 修改 Java、测试或资源文件后，提交前优先执行 `mvn spotless:apply`，避免遗漏 spotless 格式问题。
- 若修改系统配置的数据结构，记得同时检查 `configure` 持久化、`help-*.html`、`config.properties` 与配置回显行为。
- 若引入日期、节假日、调休日等规则，先明确数据来源、更新策略和时区边界。
- 涉及中国工作日判断时，默认以中国时区和中国法定节假日规则为准，避免直接复用通用周末判断。
- 新增年份数据时，优先在 `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/` 下增加对应年份资源，并补充测试用例。
- 涉及年份来源合并时，注意同时覆盖内置资源、外部文件兼容逻辑、系统配置覆盖逻辑三类场景。

## 常用命令

```bash
mvn spotless:apply
mvn test
mvn package
mvn hpi:run
```

## 后续优先事项

- 扩展更多年份的中国节假日与调休数据
- 评估节假日数据的外部来源与更新机制
- 补充更多系统配置、Pipeline、Freestyle 的使用示例
- 增加覆盖更多业务场景的自动化测试
