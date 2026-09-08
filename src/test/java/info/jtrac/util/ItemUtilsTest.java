package info.jtrac.util;

import org.junit.*;

public class ItemUtilsTest {

	@Test
    public void testHtmlEscaping() {
        Assert.assertEquals("&nbsp;&nbsp;&nbsp;&nbsp;", ItemUtils.fixWhiteSpace("    "));
        Assert.assertEquals("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", ItemUtils.fixWhiteSpace(" \t"));
        Assert.assertEquals("Hello World", ItemUtils.fixWhiteSpace("Hello World"));
        Assert.assertEquals("", ItemUtils.fixWhiteSpace(""));
        Assert.assertEquals("", ItemUtils.fixWhiteSpace(null));
        Assert.assertEquals("Hello<br/>World", ItemUtils.fixWhiteSpace("Hello\nWorld"));
        Assert.assertEquals("Hello<br/>&nbsp;&nbsp;World", ItemUtils.fixWhiteSpace("Hello\n  World"));
        Assert.assertEquals("Hello<br/>&nbsp;World<br/>&nbsp;&nbsp;&nbsp;&nbsp;Everyone", ItemUtils.fixWhiteSpace("Hello\n World\n\tEveryone"));
        Assert.assertEquals("Hello&nbsp;&nbsp;&nbsp;&nbsp;World", ItemUtils.fixWhiteSpace("Hello\tWorld"));
    }

	@Test
    public void testMarkdown() {
        Assert.assertEquals(null, ItemUtils.renderMarkdown(null));
        Assert.assertEquals("", ItemUtils.renderMarkdown(""));
        Assert.assertEquals("<p>Hello World</p>", ItemUtils.renderMarkdown("Hello World"));
        Assert.assertEquals("<p><em>Hello</em> <strong>World</strong></p>", ItemUtils.renderMarkdown("*Hello* **World**"));
        Assert.assertEquals("<h1>Hello World</h1>", ItemUtils.renderMarkdown("# Hello World"));
        Assert.assertEquals("<h2>Hello World</h2>", ItemUtils.renderMarkdown("## Hello World"));
        Assert.assertEquals("<blockquote>\n<p>Hello World</p>\n</blockquote>", ItemUtils.renderMarkdown("> Hello World"));
    }

}
