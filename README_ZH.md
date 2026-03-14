# Chinese Workday Plugin

[English](README.md)

## 当前状态

本仓库正处于持续开发中。

该插件已经为 Jenkins 的 Freestyle 任务和 Pipeline 提供了可用的中国工作日判断能力。

当前已具备的能力包括：

- 内置 `2025` 和 `2026` 年中国节假日与调休日历
- 支持 Freestyle 构建步骤
- 提供 `isWorkday(...)`、`isHoliday(...)` 和 `chineseWorkdaySupportedYears()` 等 Pipeline 步骤
- 支持在 Jenkins 系统配置中新增或覆盖指定年份的节假日配置

当前功能范围仍保持聚焦，后续会继续补充更多年份及相关集成能力。

## 安装

你可以通过以下两种方式安装插件。

### 方式一：从 Jenkins 插件中心安装

如果你的 Jenkins 更新中心中已经提供该插件：

1. 打开 `Manage Jenkins -> Plugins`
2. 搜索 `Chinese Workday`
3. 选中插件并安装
4. 如果 Jenkins 提示重启，则按提示重启

### 方式二：手动上传 `hpi` 安装包

先在本地构建插件：

```bash
mvn package
```

构建完成后，在 Jenkins 中上传 `target/` 目录下生成的 `.hpi` 文件：

1. 打开 `Manage Jenkins -> Plugins`
2. 打开 `Advanced settings` 区域
3. 在 `Deploy Plugin` 中选择生成的 `.hpi` 文件
4. 上传插件，并在 Jenkins 提示时重启

如果你是在本地开发，也可以直接启动一个预加载该插件的测试 Jenkins：

```bash
mvn hpi:run
```

## 规划范围

本插件的目标是为 Jenkins 提供与中国工作日规则相关的能力，例如：

- 判断某一天是否为工作日
- 处理法定节假日
- 处理调休工作日
- 支持在 Jenkins 任务和 Pipeline 中使用

当前实现说明：

- 当前内置年份为 `2025` 和 `2026`
- 内置日历数据来源于国务院节假日通知
- 管理员可以在 `Manage Jenkins -> System -> Chinese Workday` 中新增未来年份，或覆盖内置年份
- 支持年份会从内置日历资源中自动发现
- 对于支持的年份，工作日返回 `true`，非工作日返回 `false`
- 对于不支持的年份，插件会直接报错，而不是返回含糊结果
- 默认时区为 `Asia/Shanghai`
- 在 Pipeline 中可以使用 `isWorkday(...)` 获取布尔结果
- 在 Pipeline 中可以使用 `isHoliday(...)` 获取布尔结果
- 在 Pipeline 中可以使用 `chineseWorkdaySupportedYears()` 查询内置和自定义年份

具体功能范围后续仍会继续完善。

## 使用方式

### Freestyle 任务

添加构建步骤 `Chinese Workday Check`。

可用字段：

- `Date`：可选，使用 ISO 格式 `yyyy-MM-dd`；留空表示使用所选时区下的“今天”
- `Time zone`：可选，默认值为 `Asia/Shanghai`
- `Fail build on non-workday`：可选；启用后，如果判断结果是非工作日，则当前构建步骤会失败，后续 Freestyle 构建步骤不会继续执行

示例：

- `Date`：`2025-10-03`
- `Time zone`：`Asia/Shanghai`

构建日志示例输出：

```text
Chinese Workday check
Date: 2025-10-03
Time zone: Asia/Shanghai
Workday: false
Holiday: true
```

### Pipeline：判断是否为工作日

当你需要在 Pipeline 中获取布尔值结果时，使用 `isWorkday(...)`。`date` 为可选项；省略时会使用所选时区下的当前日期。`timeZone` 也为可选项；省略时默认使用 `Asia/Shanghai`。

Scripted Pipeline 或 Declarative Pipeline 的 `script` 示例：

```groovy
def result = isWorkday(date: '2025-10-03')

echo "workday=${result}"

if (!result) {
    echo 'Skip release actions on non-workdays.'
}
```

返回值语义：

- `true`：该日期是中国工作日
- `false`：该日期是中国非工作日
- 不支持的年份：步骤执行失败并报错

使用默认日期的示例：

```groovy
def todayIsWorkday = isWorkday()
echo "todayIsWorkday=${todayIsWorkday}"
```

### Pipeline：判断是否为节假日

当你希望在 Pipeline 中获取“是否为非工作日”的布尔结果时，使用 `isHoliday(...)`。`date` 为可选项；省略时会使用所选时区下的当前日期。`timeZone` 也为可选项；省略时默认使用 `Asia/Shanghai`。

```groovy
def holiday = isHoliday(date: '2025-10-03')
echo "holiday=${holiday}"
```

返回值语义：

- `true`：该日期是中国节假日 / 非工作日
- `false`：该日期是中国工作日
- 不支持的年份：步骤执行失败并报错

### Pipeline：查询支持的年份

使用 `chineseWorkdaySupportedYears()` 查询当前可用的内置年份和自定义年份。

```groovy
def years = chineseWorkdaySupportedYears()
echo "supportedYears=${years.join(',')}"
```

### Pipeline：Builder 风格步骤

插件还暴露了一个 Builder 风格的步骤 `chineseWorkday(...)`，会将结果直接输出到构建日志。

```groovy
node {
    chineseWorkday(
        date: '2025-10-03',
        timeZone: 'Asia/Shanghai'
    )
}
```

这种形式适合你希望直接查看日志输出，而不是消费布尔返回值的场景。

## 系统配置

管理员可以在 `Manage Jenkins -> System -> Chinese Workday` 中新增或覆盖节假日日历。

推荐操作步骤：

1. 打开 `Manage Jenkins -> System`
2. 找到 `Chinese Workday` 配置区域
3. 点击 `Add calendar`
4. 填写目标年份、节假日和调休工作日
5. 保存系统配置

每条日历配置包含以下字段：

- `Year`：年份，例如 `2027`
- `Holidays`：ISO 日期或使用 `..` 表示的日期范围
- `Make-up workdays`：ISO 日期或使用 `..` 表示的日期范围；这些日期即使落在周末，也会被视为工作日

输入格式规则：

- 日期必须使用 ISO 格式 `yyyy-MM-dd`
- 日期范围使用 `..`，例如 `2027-10-01..2027-10-07`
- 多个日期可以使用逗号或换行分隔
- `2027` 年的配置中，所有日期都必须属于 `2027` 年
- 同一个日期不能同时出现在 `Holidays` 和 `Make-up workdays` 中
- 如果某个配置年份与内置年份相同，则配置内容会覆盖内置日历

配置示例：

```text
Year: 2027
Holidays:
2027-01-01
2027-02-10..2027-02-16
2027-04-05
2027-10-01..2027-10-07

Make-up workdays:
2027-02-07
2027-02-20
2027-09-26
```

保存系统配置后，后续的构建和 Pipeline 步骤会自动使用新的日历数据。

出于兼容性考虑，插件仍会读取可选的文件覆盖目录
`$JENKINS_HOME/chinese-workday/calendars/`，但系统配置中的内容优先级高于这些文件。

## 节假日数据维护

内置节假日数据应通过明确的维护流程更新，而不是临时直接修改文件。

- 权威来源：国务院办公厅年度节假日通知
- 每个内置年份文件应保留 `# Source: ...` 注释
- 新增内置年份时，同时更新年份文件和 `index.properties`
- 修改内置数据时，应同步补充或更新自动化测试

具体维护清单与校验流程见 `docs/calendar-maintenance.md`。

## 开发

环境要求：

- JDK `17+`
- Maven `3.9.6+`

这些约束来自本仓库所使用的 Jenkins Plugin Parent POM。

常用命令：

```bash
mvn test
mvn package
mvn hpi:run
```

说明：

- `mvn test`：运行 Jenkins 插件测试
- `mvn package`：构建插件产物
- `mvn hpi:run`：启动一个本地 Jenkins 实例，便于手动验证

## 仓库说明

- `src/main/java/`：插件实现代码
- `src/main/resources/`：Jelly 页面、帮助文档和国际化资源
- `src/test/java/`：自动化测试

## 贡献

欢迎在插件方向进一步稳定后参与贡献。

在此之前，优先提交小步、渐进式修改，帮助仓库持续从模板代码演进为真实插件实现。

可参考 Jenkins 社区贡献指南：

- https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md

## 许可证

本项目基于 MIT 协议发布。详见 `LICENSE.md`。
