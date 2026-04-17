# 节假日数据维护

[English](calendar-maintenance.en.md)

本文说明如何新增或更新插件内置的中国节假日日历数据。

## 数据来源策略

- 首选权威来源：国务院办公厅年度节假日通知
- 首选发布渠道：国务院或其他政府官网页面
- 如果官方页面地址发生变化，应在日历文件中保留最终可访问的规范来源链接
- Jenkins 运行时可以通过 `Manage Jenkins -> System -> Chinese Workday` 做临时覆盖，但仓库中的内置数据仍应通过代码评审方式更新

## 更新清单

新增内置年份（例如 `2027`）时，请按以下步骤执行：

1. 找到该年度的官方节假日通知
2. 根据通知确认全部放假区间和调休上班日期
3. 新增 `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/2027.properties`
4. 在文件顶部添加 `# Source: ...` 注释
5. 更新 `src/main/resources/io/jenkins/plugins/chinese_workday/calendars/index.properties`
6. 新增或更新自动化测试，覆盖代表性的节假日和调休工作日
7. 运行本地校验命令
8. 在 pull request 描述中附上来源链接和校验结果

## 文件格式

示例：

```properties
# Source: https://www.gov.cn/example-notice
holidays=\
  2027-01-01,\
  2027-02-10..2027-02-16,\
  2027-10-01..2027-10-07
makeUpWorkdays=\
  2027-02-07,\
  2027-02-20,\
  2027-09-26
```

规则：

- 日期使用 ISO 格式 `yyyy-MM-dd`
- 日期区间使用 `..`
- `2027.properties` 中的所有日期都必须属于 `2027` 年
- 同一个日期不能同时出现在 `holidays` 和 `makeUpWorkdays` 中
- 为了可读性，内置数据文件优先采用多行书写格式

## 校验流程

推荐本地命令：

```bash
mvn -Dtest=CalendarResourceIntegrityTest,ChineseWorkdayServiceTest surefire:test
mvn test
```

校验重点：

- 内置资源文件与 `index.properties` 保持一致
- 每个内置年份文件都能被成功解析
- 每个内置年份文件都包含来源注释
- 现有年份的代表性服务测试仍然通过

## 发布说明

如果某个新年份还没有进入已发布的插件版本，管理员可以先通过 Jenkins 系统配置页进行临时配置，待后续插件版本发布后再切换到内置数据。
