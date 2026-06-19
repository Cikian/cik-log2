package cn.cikian.logback.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * HTML日志样式
 *
 * @author Cikian
 * @version 1.1
 * @implNote 优化高并发下的时间戳获取逻辑，支持异步日志；修复动态追加日志时的前端交互失效缺陷。
 * @see <a href="https://www.cikian.cn">https://www.cikian.cn</a>
 * @since 2026-06-20 01:00
 */
public class LogbackCustomHtmlLayout extends LayoutBase<ILoggingEvent> {

    // 采用标准 DateTimeFormatter（线程安全）
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());

    @Override
    public String getFileHeader() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\">\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "<title>系统运行控制台日志</title>\n" +
                "<style>\n" +
                "  :root {\n" +
                "    --bg-color: #f8fafc;\n" +
                "    --card-bg: #ffffff;\n" +
                "    --text-main: #1e293b;\n" +
                "    --text-muted: #64748b;\n" +
                "    --border-color: #e2e8f0;\n" +
                "    --primary: #4f46e5;\n" +
                "  }\n" +
                "  body {\n" +
                "    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
                "    background-color: var(--bg-color);\n" +
                "    color: var(--text-main);\n" +
                "    margin: 0;\n" +
                "    padding: 24px;\n" +
                "  }\n" +
                "  .header-container {\n" +
                "    display: flex;\n" +
                "    align-items: center;\n" +
                "    justify-content: space-between;\n" +
                "    margin-bottom: 20px;\n" +
                "    border-bottom: 2px solid var(--border-color);\n" +
                "    padding-bottom: 12px;\n" +
                "  }\n" +
                "  h3 {\n" +
                "    margin: 0;\n" +
                "    font-size: 22px;\n" +
                "    font-weight: 700;\n" +
                "    color: #0f172a;\n" +
                "    letter-spacing: -0.5px;\n" +
                "  }\n" +
                "  .subtitle {\n" +
                "    font-size: 13px;\n" +
                "    color: var(--text-muted);\n" +
                "  }\n" +
                "  .table-container {\n" +
                "    background: var(--card-bg);\n" +
                "    border-radius: 12px;\n" +
                "    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);\n" +
                "    border: 1px solid var(--border-color);\n" +
                "    overflow: hidden;\n" +
                "  }\n" +
                "  table {\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;\n" +
                "    text-align: left;\n" +
                "    font-size: 13.5px;\n" +
                "    table-layout: fixed;\n" +
                "  }\n" +
                "  th {\n" +
                "    background-color: #f1f5f9;\n" +
                "    color: #475569;\n" +
                "    font-weight: 600;\n" +
                "    padding: 14px 16px;\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
                "  }\n" +
                "  tr {\n" +
                "    transition: background-color 0.2s ease;\n" +
                "    content-visibility: auto;\n" +
                "    contain-intrinsic-size: 60px;\n" +
                "  }\n" +
                "  tr:hover {\n" +
                "    background-color: #f8fafc !important;\n" +
                "  }\n" +
                "  td {\n" +
                "    padding: 12px 16px;\n" +
                "    border-bottom: 1px solid var(--border-color);\n" +
                "    word-wrap: break-word;\n" +
                "    vertical-align: top;\n" +
                "  }\n" +
                "  /* 列宽控制 */\n" +
                "  .col-time { width: 180px; text-align: center; color: var(--text-muted); font-variant-numeric: tabular-nums; }\n" +
                "  .col-level { width: 100px; text-align: center; }\n" +
                "  .col-logger { width: 380px; text-align: center; color: #0d9488; font-weight: 500; font-family: monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }\n" +
                "  \n" +
                "  /* 日志详情展开/收起核心交互样式 */\n" +
                "  .msg-container {\n" +
                "    display: flex;\n" +
                "    align-items: flex-start;\n" +
                "    width: 100%;\n" +
                "  }\n" +
                "  .msg-content {\n" +
                "    flex: 1;\n" +
                "    font-family: Consolas, Monaco, 'Andale Mono', monospace;\n" +
                "    color: #334155;\n" +
                "    white-space: nowrap;\n" +
                "    overflow: hidden;\n" +
                "    text-overflow: ellipsis;\n" +
                "  }\n" +
                "  /* 当父级行被标记为展开状态时的表现 */\n" +
                "  tr.expanded .msg-content {\n" +
                "    white-space: pre-wrap;\n" +
                "    word-wrap: break-word;\n" +
                "  }\n" +
                "  .btn-toggle {\n" +
                "    background: transparent;\n" +
                "    border: none;\n" +
                "    color: #3b82f6;\n" +
                "    cursor: pointer;\n" +
                "    font-size: 13px;\n" +
                "    padding: 0 0 0 8px;\n" +
                "    margin-top: 1px;\n" +
                "    flex-shrink: 0;\n" +
                "    outline: none;\n" +
                "  }\n" +
                "  .btn-toggle:hover {\n" +
                "    text-decoration: underline;\n" +
                "  }\n" +
                "\n" +
                "  /* 级别标签样式 */\n" +
                "  .badge {\n" +
                "    display: inline-block;\n" +
                "    padding: 4px 8px;\n" +
                "    font-size: 11px;\n" +
                "    font-weight: 700;\n" +
                "    border-radius: 6px;\n" +
                "    text-align: center;\n" +
                "    letter-spacing: 0.5px;\n" +
                "    width: 55px;\n" +
                "  }\n" +
                "  .bg-trace { background-color: #f1f5f9; color: #64748b; border: 1px solid #cbd5e1; }\n" +
                "  .bg-debug { background-color: #ecfdf5; color: #059669; border: 1px solid #a7f3d0; }\n" +
                "  .bg-info { background-color: #eff6ff; color: #2563eb; border: 1px solid #bfdbfe; }\n" +
                "  .bg-warn { background-color: #fffbeb; color: #d97706; border: 1px solid #fde68a; }\n" +
                "  .bg-error { background-color: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }\n" +
                "  /* 行级高亮微调 */\n" +
                "  tr.WARN { background-color: #fffdf5; }\n" +
                "  tr.ERROR { background-color: #fffafb; }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<script>\n" +
                "  // 极致精简、且完全兼容流式追加节点的点击展开/收起逻辑\n" +
                "  function toggleMsg(btn) {\n" +
                "    var row = btn.closest('tr');\n" +
                "    if (row.classList.contains('expanded')) {\n" +
                "      row.classList.remove('expanded');\n" +
                "      btn.innerText = '展开';\n" +
                "    } else {\n" +
                "      row.classList.add('expanded');\n" +
                "      btn.innerText = '收起';\n" +
                "    }\n" +
                "  }\n" +
                "</script>\n" +
                "<div class=\"header-container\">\n" +
                "  <h3>系统控制台运行日志</h3>\n" +
                "  <div class=\"subtitle\">Document: <a href=\"https://www.cikian.cn\" target=\"_blank\">Cikian</a></div>\n" +
                "</div>\n" +
                "<div class=\"table-container\">\n" +
                "  <table>\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th class=\"col-time\">时间</th>\n" +
                "        <th class=\"col-level\">级别</th>\n" +
                "        <th class=\"col-logger\">执行类名</th>\n" +
                "        <th class=\"col-msg\">日志详情</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tbody>";
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();
        String level = event.getLevel().toString();
        String levelLower = level.toLowerCase();

        // 核心修改：使用事件固有的时间戳，完美承接异步 Appender，避免时钟阻塞和跨线程调度时间差错
        String formattedTime = DATE_FORMAT.format(Instant.ofEpochMilli(event.getTimeStamp()));

        sb.append("<tr class=\"").append(level).append("\">")
                .append("<td class=\"col-time\">").append(formattedTime).append("</td>")
                .append("<td class=\"col-level\"><span class=\"badge bg-").append(levelLower).append("\">").append(level).append("</span></td>")
                .append("<td class=\"col-logger\" title=\"").append(event.getLoggerName()).append("\">").append(event.getLoggerName()).append("</td>")
                .append("<td class=\"col-msg\">")
                .append("<div class=\"msg-container\">")
                .append("<div class=\"msg-content\">").append(escapeHtml(event.getFormattedMessage())).append("</div>")
                .append("<button class=\"btn-toggle\" onclick=\"toggleMsg(this)\">展开</button>")
                .append("</div>")
                .append("</td>")
                .append("</tr>\n");
        return sb.toString();
    }

    @Override
    public String getFileFooter() {
        return "    </tbody>\n" +
                "  </table>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}