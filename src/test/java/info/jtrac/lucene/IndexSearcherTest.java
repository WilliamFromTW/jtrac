package info.jtrac.lucene;

import info.jtrac.domain.Item;

import java.io.File;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.junit.*;

public class IndexSearcherTest {

    private ApplicationContext context;

	@Before
    public void setUp() {
        File home = new File("target/home");
        if (!home.exists()) {
            home.mkdir();
        }
        File file = new File("target/home/indexes");
        if (!file.exists()) {
            file.mkdir();
        } else {
            for (File f : file.listFiles()) {
                f.delete();
            }
        }
        System.setProperty("jtrac.home", home.getAbsolutePath());
        context = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/applicationContext-lucene.xml");
    }

	@Test
    public void testFindItemIdsBySearchingWithinSummaryAndDetailFields() throws Exception {
        Item item = new Item();
        item.setId(1);
        item.setSummary("this is a test summary");
        item.setDetail("the quick brown fox jumped over the lazy dogs");
        Indexer indexer = (Indexer) context.getBean("indexer");
        indexer.index(item);
        IndexSearcher searcher = (IndexSearcher) context.getBean("indexSearcher");
        List list = searcher.findItemIdsContainingText("lazy");
        Assert.assertEquals(1, list.size());
        list = searcher.findItemIdsContainingText("foo");
        Assert.assertEquals(0, list.size());
        list = searcher.findItemIdsContainingText("summary");
        Assert.assertEquals(1, list.size());
    }

	@Test
    public void testIfUmlautsCanBeIndexedAndSearchedFor() {
        Item item = new Item();
        item.setId(1);
        item.setSummary("this does not contain an umlaut");
        item.setDetail("there is an umlaut right here --> \u00fcmlaut");
        Indexer indexer = (Indexer) context.getBean("indexer");
        indexer.index(item);
        IndexSearcher searcher = (IndexSearcher) context.getBean("indexSearcher");
        List list = searcher.findItemIdsContainingText("\u00fcmlaut");
        Assert.assertEquals(1, list.size());
    }

	@Test
    public void testEnglishStemmingWindowsMatchesWindow() {
        Item item = new Item();
        item.setId(2);
        item.setSummary("Docx attachment details");
        item.setDetail("Deployment on Windows Server environment");
        Indexer indexer = (Indexer) context.getBean("indexer");
        indexer.index(item);
        IndexSearcher searcher = (IndexSearcher) context.getBean("indexSearcher");

        // Searching singular 'window' matches plural 'Windows'
        List list1 = searcher.findItemIdsContainingText("window");
        Assert.assertEquals(1, list1.size());

        // Searching plural 'Windows' matches
        List list2 = searcher.findItemIdsContainingText("Windows");
        Assert.assertEquals(1, list2.size());

        // Searching lowercase 'windows' matches
        List list3 = searcher.findItemIdsContainingText("windows");
        Assert.assertEquals(1, list3.size());
    }

	@Test
    public void testAutoPrefixFallback() {
        Item item = new Item();
        item.setId(3);
        item.setSummary("System config");
        item.setDetail("Configuring Microsoft Windows services");
        Indexer indexer = (Indexer) context.getBean("indexer");
        indexer.index(item);
        IndexSearcher searcher = (IndexSearcher) context.getBean("indexSearcher");

        // Prefix 'win' automatically falls back to 'win*' and finds 'Windows'
        List list = searcher.findItemIdsContainingText("win");
        Assert.assertEquals(1, list.size());
    }

	@Test
    public void testChineseUnigramSearch() {
        Item item = new Item();
        item.setId(4);
        item.setSummary("繁體中文標題");
        item.setDetail("系統測試與附件檢索支援多語系運作");
        Indexer indexer = (Indexer) context.getBean("indexer");
        indexer.index(item);
        IndexSearcher searcher = (IndexSearcher) context.getBean("indexSearcher");

        List list1 = searcher.findItemIdsContainingText("測試");
        Assert.assertEquals(1, list1.size());

        List list2 = searcher.findItemIdsContainingText("多語系");
        Assert.assertEquals(1, list2.size());
    }

}
