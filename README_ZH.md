# Chinese Workday Plugin

[![GitHub release](https://img.shields.io/github/v/release/jenkinsci/chinese-workday-plugin?sort=semver)](https://github.com/jenkinsci/chinese-workday-plugin/releases)
[![Jenkins Plugin Site](https://img.shields.io/badge/Jenkins-Plugin%20Site-D24939?logo=jenkins&logoColor=white)](https://plugins.jenkins.io/chinese-workday)
[![Jenkins Security Scan](https://github.com/jenkinsci/chinese-workday-plugin/actions/workflows/jenkins-security-scan.yml/badge.svg)](https://github.com/jenkinsci/chinese-workday-plugin/actions/workflows/jenkins-security-scan.yml)
[![License](https://img.shields.io/github/license/jenkinsci/chinese-workday-plugin)](LICENSE.md)

[English](README.md)

## 概览

Chinese Workday Plugin 让 Jenkins 在判断是否执行任务时，按中国法定节假日和调休规则工作，而不是只按普通工作日/周末规则处理。适合用于控制 Pipeline stage、定时任务或未来日期判断。

快速了解：

- 内置 `2020` 至 `2026` 年中国节假日与调休日历
- 提供 `isChineseWorkday(...)`、`isChineseHoliday(...)` 和 `chineseWorkdaySupportedYears()` 等 Pipeline 步骤
- 支持在 Jenkins 系统配置中新增或覆盖指定年份日历
- 仍兼容 `$JENKINS_HOME/chinese-workday/calendars/` 文件覆盖方式

## 常见场景

- 仅在中国工作日执行发布或部署 stage
- 在中国节假日或调休日跳过定时任务
- 在插件尚未内置下一年数据时，先补一份临时年份配置

## 安装

### 方式一：从 Jenkins 插件中心安装

1. 打开 `Manage Jenkins -> Plugins`
2. 搜索 `Chinese Workday`
3. 安装插件，并在 Jenkins 提示时重启

### 方式二：手动上传 `hpi` 安装包

先在本地构建插件：

```bash
mvn package
```

然后在 Jenkins 中上传 `target/` 目录下生成的 `.hpi` 文件，例如 `target/chinese-workday.hpi`：

1. 打开 `Manage Jenkins -> Plugins`
2. 打开 `Advanced settings`
3. 在 `Deploy Plugin` 中选择 `.hpi` 文件
4. 上传插件，并在 Jenkins 提示时重启

## 快速开始

仅在中国工作日执行发布 stage：

```groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo '构建阶段每天都会执行。'
            }
        }
        stage('Release') {
            when {
                expression {
                    isChineseWorkday()
                }
            }
            steps {
                echo '仅在中国工作日执行发布。'
            }
        }
    }
}
```

## 如果下一年数据还没内置

- 对于不支持的年份，插件会明确失败，不会静默回退到仅按周末判断
- 判断未来日期前，可以先调用 `chineseWorkdaySupportedYears()`
- 管理员可以在 `Manage Jenkins -> System -> Chinese Workday` 中补临时年份配置
- 从每年 12 月开始，如果下一年仍不可用，Jenkins 管理员会看到提醒

## 行为摘要

- 默认时区：`Asia/Shanghai`
- 数据优先级：内置资源 < 外部文件 < Jenkins 系统配置
- 同年份下，系统配置会覆盖内置数据

## 使用方式

### Pipeline

按用途选择步骤：

- `isChineseWorkday(...)`：返回是否为中国工作日
- `isChineseHoliday(...)`：返回是否为中国节假日或调休日
- `chineseWorkdaySupportedYears()`：查看当前可用年份

说明：

- 所有 Pipeline 步骤都使用 `Asia/Shanghai`
- `date` 为可选参数；省略时表示判断今天
- 不支持的年份会显式失败

#### 判断今天是否为工作日

```groovy
def todayIsWorkday = isChineseWorkday()
echo "todayIsWorkday=${todayIsWorkday}"

if (!todayIsWorkday) {
    echo '今天是中国非工作日，跳过发布动作。'
}
```

#### 判断指定日期

```groovy
def holiday = isChineseHoliday(date: '2025-10-03')
echo "holiday=${holiday}"
```

#### 在 `when` 中控制 stage

```groovy
pipeline {
    agent any
    stages {
        stage('Release') {
            when {
                expression {
                    isChineseWorkday()
                }
            }
            steps {
                echo '仅在中国工作日执行发布。'
            }
        }
    }
}
```

#### 先判断一次，再复用到后续 stage

```groovy
pipeline {
    agent any
    stages {
        stage('Prepare') {
            steps {
                script {
                    env.RUN_RELEASE = isChineseWorkday() ? 'true' : 'false'
                }
            }
        }
        stage('Release') {
            when {
                expression {
                    env.RUN_RELEASE == 'true'
                }
            }
            steps {
                echo '今天允许执行发布。'
            }
        }
    }
}
```

#### 判断未来日期前，先确认年份是否已支持

```groovy
def targetYear = 2027
def years = chineseWorkdaySupportedYears()

if (!years.contains(targetYear)) {
    error "Chinese workday calendar for ${targetYear} is not configured yet."
}

echo "isWorkday=${isChineseWorkday(date: '2027-10-02')}"
```

### Freestyle

添加构建步骤 `Chinese Workday Check`。

- `Date`：可选，ISO `yyyy-MM-dd`；留空表示使用 `Asia/Shanghai` 下的今天
- `Fail build on non-workday`：若为非工作日则让当前步骤失败，后续 Freestyle 步骤不再执行

构建日志示例：

```text
Chinese Workday check
Date: 2025-10-03
Time zone: Asia/Shanghai
Workday: false
Holiday: true
```

## 系统配置

管理员可以在 `Manage Jenkins -> System -> Chinese Workday` 中新增或覆盖日历。

快速操作：

1. 打开 `Manage Jenkins -> System`
2. 找到 `Chinese Workday`，点击 `Add calendar`
3. 填写年份、节假日和调休工作日并保存

字段说明：

- `Year`：目标年份，例如 `2027`
- `Holidays`：ISO 日期或使用 `..` 的日期范围
- `Make-up workdays`：即使落在周末也要按工作日处理的日期

填写规则：

- 日期格式必须为 `yyyy-MM-dd`
- 日期范围使用 `..`，例如 `2027-10-01..2027-10-07`
- 多个日期可用逗号或换行分隔
- 某一年的条目中，所有日期都必须属于该年份
- 同一个日期不能同时出现在 `Holidays` 和 `Make-up workdays` 中

示例：

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

保存后，后续构建会立即使用新日历。出于兼容性考虑，插件仍会读取 `$JENKINS_HOME/chinese-workday/calendars/` 下的可选文件覆盖，但 Jenkins 系统配置优先级更高。

从每年 12 月开始，如果下一年数据仍不可用，Jenkins 管理员还会看到一条管理提醒。提醒会链接到 `Manage Jenkins -> System -> Chinese Workday`，方便先补临时配置，或者在新版本已内置该年份时直接升级插件。

## 常见问题

- 未来年份缺失：在 `Manage Jenkins -> System -> Chinese Workday` 中补一份临时年份配置
- 周末返回工作日：因为中国调休工作日可能落在周末
- 系统配置覆盖内置数据：这是有意设计，优先级为 `内置资源 < 外部文件 < 系统配置`

## 节假日数据维护

内置节假日数据应通过明确的维护流程更新，而不是临时直接修改文件。关于权威来源、年度更新清单和校验流程，请参考 `docs/calendar-maintenance.md`。

## 开发与贡献

欢迎贡献。建议优先提交小步、渐进式修改，并保持行为稳定、测试充分。

仓库内常用说明：

- `CONTRIBUTING.md`
- `docs/development.md`
- `docs/architecture.md`
- `docs/calendar-maintenance.md`

Jenkins 社区贡献指南：

- https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md

## 许可证

本项目基于 MIT 协议发布。详见 `LICENSE.md`。
