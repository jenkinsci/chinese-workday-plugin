# Chinese Workday Plugin

[English](README.md)

## 当前状态

本仓库正处于持续开发中。

该插件已经为 Jenkins 的 Freestyle 任务和 Pipeline 提供了可用的中国工作日判断能力。

当前已具备的能力包括：

- 内置 `2020` 至 `2026` 年中国节假日与调休日历
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

## 快速开始

Freestyle 构建步骤示例：

```text
Chinese Workday Check
Date: 2025-10-03
Fail build on non-workday: enabled
```

Pipeline 布尔判断示例：

```groovy
def workday = isWorkday(date: '2025-10-03')
echo "workday=${workday}"
```

## 规划范围

本插件的目标是为 Jenkins 提供围绕中国工作日规则的能力，包括任务、Pipeline 以及管理员可维护的年份覆盖配置。

当前实现说明：

- 当前内置年份为 `2020` 至 `2026`
- 内置日历数据来源于国务院节假日通知
- 管理员可以在 `Manage Jenkins -> System -> Chinese Workday` 中新增未来年份，或覆盖内置年份
- `chineseWorkdaySupportedYears()` 会返回当前可用的内置年份和自定义年份
- 对于不支持的年份，插件会直接报错，而不是返回含糊结果
- 所有日期判断都固定使用 `Asia/Shanghai` 时区

具体功能范围后续仍会继续完善。

## 行为摘要

- 默认时区：`Asia/Shanghai`
- 数据优先级：内置资源 < 外部文件 < Jenkins 系统配置
- `isWorkday(...)` 和 `isHoliday(...)` 返回布尔值
- `chineseWorkday(...)` 会把可读结果输出到构建日志
- 对于不支持的年份，插件会明确失败，而不会静默回退到仅按周末判断

## 使用方式

### Freestyle 任务

添加构建步骤 `Chinese Workday Check`。

可用字段：

- `Date`：可选，使用 ISO 格式 `yyyy-MM-dd`；留空表示使用 `Asia/Shanghai` 下的“今天”
- `Fail build on non-workday`：可选；启用后，如果判断结果是非工作日，则当前构建步骤会失败，后续 Freestyle 构建步骤不会继续执行

示例：

- `Date`：`2025-10-03`

构建日志示例输出：

```text
Chinese Workday check
Date: 2025-10-03
Time zone: Asia/Shanghai
Workday: false
Holiday: true
```

### Pipeline：判断是否为工作日

当你需要在 Pipeline 中获取布尔值结果时，使用 `isWorkday(...)`。`date` 为可选项；省略时会使用 `Asia/Shanghai` 下的当前日期。该步骤不再单独暴露时区参数。

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

当你希望在 Pipeline 中获取“是否为非工作日”的布尔结果时，使用 `isHoliday(...)`。`date` 为可选项；省略时会使用 `Asia/Shanghai` 下的当前日期。该步骤不再单独暴露时区参数。

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
        date: '2025-10-03'
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

## 常见问题

### 为什么未来年份会失败？

如果某个年份（例如 `2027`）尚未内置，且你也没有配置覆盖数据，插件会显式失败，而不是猜测规则。此时可以先在
`Manage Jenkins -> System -> Chinese Workday` 中添加临时年份配置。

### 为什么周末也可能返回工作日？

因为中国的调休工作日可能落在周末。这些日期会优先于普通周末规则进行判断。

### 为什么系统配置会覆盖内置数据？

这是有意设计的，实际生效优先级为：

```text
内置资源 < 外部文件 < 系统配置
```

这样管理员无需重新构建插件，就可以修正或扩展某一年的数据。

## 节假日数据维护

内置节假日数据应通过明确的维护流程更新，而不是临时直接修改文件。关于权威来源、年度更新清单和校验流程，请参考
`docs/calendar-maintenance.md`。

## 开发

关于开发环境、代码结构和本地开发流程，请参考：

- `docs/development.md`
- `docs/architecture.md`
- `docs/calendar-maintenance.md`

## 贡献

欢迎在插件方向进一步稳定后参与贡献。

在此之前，优先提交小步、渐进式修改，帮助仓库持续从模板代码演进为真实插件实现。

可参考 Jenkins 社区贡献指南：

- https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md

## 许可证

本项目基于 MIT 协议发布。详见 `LICENSE.md`。
