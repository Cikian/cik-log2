package cn.cikian.logback.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.spi.ContextAwareBase;

import java.net.URL;

/**
 * 智能日志配置装载器：当宿主项目无标准 logback 配置时，自动启用工具类精美日志
 *
 * @author Cikian
 * @version 1.2
 * @implNote 修复了在 Spring Boot 环境中由于二次初始化（Context Reset）及默认配置覆盖导致日志内容丢失、只剩表头或双表头的问题。
 * @see <a href="https://www.cikian.cn">https://www.cikian.cn</a>
 * @since 2026-06-16 21:50
 */
public class SmartLogbackConfigurator extends ContextAwareBase implements Configurator {

    @Override
    public void configure(LoggerContext loggerContext) {
        // 1. 检查类路径下是否存在宿主项目的标准配置文件（完整检测各类命名契约契合 Spring 规范）
        URL hostConfig = Thread.currentThread().getContextClassLoader().getResource("logback.xml");
        URL hostTestConfig = Thread.currentThread().getContextClassLoader().getResource("logback-test.xml");
        URL hostSpringConfig = Thread.currentThread().getContextClassLoader().getResource("logback-spring.xml");
        URL hostSpringTestConfig = Thread.currentThread().getContextClassLoader().getResource("logback-spring-test.xml");
        URL hostGroovyConfig = Thread.currentThread().getContextClassLoader().getResource("logback.groovy");
        URL hostGroovyTestConfig = Thread.currentThread().getContextClassLoader().getResource("logback-test.groovy");

        boolean hasHostConfig = (hostConfig != null || hostTestConfig != null ||
                hostSpringConfig != null || hostSpringTestConfig != null ||
                hostGroovyConfig != null || hostGroovyTestConfig != null);

        // 同时也检查宿主项目是否通过配置属性或环境变量明确指定了外部日志路径
        String customConfigPath = System.getProperty("logging.config");
        if (customConfigPath == null) {
            customConfigPath = System.getenv("LOGGING_CONFIG");
        }

        if (hasHostConfig || customConfigPath != null) {
            // 如果宿主项目自己有配置，工具包什么都不做，完全交由宿主项目或 Spring Boot 自行处理
            return;
        }

        // 2. 宿主项目没有任何日志配置，启用我们的内置精美日志
        URL defaultConfig = Thread.currentThread().getContextClassLoader().getResource("logback-default-cik.xml");
        if (defaultConfig != null) {
            boolean isSpringBootPresent = false;
            try {
                // 检测当前类路径中是否存在 Spring Boot 核心环境类
                Class.forName("org.springframework.boot.SpringApplication", false, Thread.currentThread().getContextClassLoader());
                isSpringBootPresent = true;
            } catch (ClassNotFoundException e) {
                // 纯 Java 环境，无需特殊处理
            }

            if (isSpringBootPresent) {
                /*
                 * 【Spring Boot 环境完美解决方案】
                 * 彻底移除 LoggerContextListener，避免引发双表头。
                 * 直接将内置配置文件的全路径 URL 写入系统属性 "logging.config" 中。
                 * * 这样一来：
                 * 1. Spring Boot 极早期启动时会使用默认标准控制台输出。
                 * 2. 当启动到准备环境阶段，Spring Boot 日志系统读取到我们注入的 "logging.config"，
                 * 将其视为最高优先级的标准指定配置文件。
                 * 3. Spring Boot 会自主调用官方的干净加载链路，通过 Joran 统一加载它，
                 * 且【绝对不会】再触发毁灭性的 loadDefaults() 覆写逻辑！
                 * * 最终效果：完美的单表头，且全量业务日志顺畅输出。
                 */
                System.setProperty("logging.config", defaultConfig.toExternalForm());
            } else {
                /*
                 * 【纯 Java / 非 Spring Boot 环境解决方案】
                 * 直接使用 JoranConfigurator 编排加载内置配置文件即可。
                 * 非 Spring 环境结构简单，不会发生中途强行 Reset 和覆写行为。
                 */
                try {
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(loggerContext);
                    configurator.doConfigure(defaultConfig);
                } catch (JoranException e) {
                    addError("动态加载工具类内置日志配置失败", e);
                }
            }
        }
    }
}