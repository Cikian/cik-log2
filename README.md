# CIK-LOG

## 简介

logback 精美日志输出工具，自动检测宿主项目日志配置，当项目未配置 logback 时自动启用精美日志输出。

**核心功能：**
- 智能检测宿主项目是否存在 logback 配置文件
- 自动启用内置精美日志配置（控制台 + 文件 + HTML 格式）
- 同时支持 Spring Boot 和纯 Java 环境
- 精美 HTML 日志报告，支持展开/收起查看详细内容

## 依赖引入

```xml
<dependency>
    <groupId>cn.cikian</groupId>
    <artifactId>cik-log</artifactId>
    <version>1.2.11</version>
</dependency>
```

> Cikian Maven仓库地址：`http://mvnrep.cikian.cn/repository/public/`

## 使用方式

### 1. Spring Boot 项目

**无需任何配置，引入依赖即可使用！**

工具会自动检测项目是否存在以下配置文件：
- `logback.xml`
- `logback-test.xml`
- `logback-spring.xml`
- `logback-spring-test.xml`
- `logback.groovy`
- `logback-test.groovy`

如果检测到宿主项目有配置，工具不会做任何操作，完全交由宿主项目自行处理。

如果未检测到配置，则自动启用内置精美日志配置。

### 2. 纯 Java 项目

**同样无需任何配置，引入依赖即可使用！**

工具会通过 Java SPI 机制自动加载配置，行为与 Spring Boot 项目一致。

### 3. 自定义配置（可选）

如果需要自定义日志路径或其他配置，可以在项目中添加 `logback.xml` 文件，工具会自动识别并使用您的配置。

默认日志输出路径：`../logs/`

## 输出格式

工具提供三种日志输出方式：

1. **控制台输出** - 带颜色高亮的格式化日志
2. **普通文件输出** - 标准 log 格式文件
3. **精美 HTML 输出** - 带样式的 HTML 日志报告

### HTML 日志特性

- 现代化 UI 设计，支持暗黑/浅色主题
- 日志级别颜色区分（TRACE/DEBUG/INFO/WARN/ERROR）
- 支持展开/收起查看长日志内容
- 悬停显示完整类名
- 按级别高亮显示

## 项目结构

```
src/main/java/cn/cikian/logback/config/
├── SmartLogbackConfigurator.java   # 智能配置装载器
└── LogbackCustomHtmlLayout.java    # HTML日志布局
src/main/resources/
├── logback-default-cik.xml         # 默认日志配置
└── META-INF/services/
    └── ch.qos.logback.classic.spi.Configurator  # SPI配置
```

## 注意事项

1. 如果宿主项目已经配置了 logback，本工具不会做任何干预
2. 可通过系统属性 `logging.config` 或环境变量 `LOGGING_CONFIG` 指定自定义配置文件路径
3. 日志文件默认保留 30 天（普通日志）和 7 天（HTML 日志）

## 版本历史

- **1.2.11** - 独立为 logback 日志工具，移除 OSS 相关功能
- **1.2.x** - 修复 Spring Boot 环境双表头问题
- **1.1.x** - 初始版本