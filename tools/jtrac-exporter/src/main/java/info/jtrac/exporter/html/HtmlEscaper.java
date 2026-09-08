package info.jtrac.exporter.html;

public class HtmlEscaper {

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String escapeWithBreaks(String text) {
        if (text == null) {
            return "";
        }
        String escaped = escape(text);
        return escaped.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");
    }
}
