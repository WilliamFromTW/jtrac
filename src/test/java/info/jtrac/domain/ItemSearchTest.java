package info.jtrac.domain;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ItemSearchTest {

    @Test
    public void testColumnHeadingsToSearchExcludesSummaryAndDetail() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        List<ColumnHeading> searchHeadings = itemSearch.getColumnHeadingsToSearch();
        for (ColumnHeading ch : searchHeadings) {
            Assert.assertNotEquals("summary", ch.getNameText());
            Assert.assertNotEquals("detail", ch.getNameText());
            Assert.assertNotEquals("lastChanged", ch.getNameText());
        }

        // But summary must still exist in render list for issue title display
        List<ColumnHeading> renderHeadings = itemSearch.getColumnHeadingsToRender();
        boolean hasSummaryInRender = false;
        for (ColumnHeading ch : renderHeadings) {
            if ("summary".equals(ch.getNameText())) {
                hasSummaryInRender = true;
                break;
            }
        }
        Assert.assertTrue(hasSummaryInRender);
    }

    @Test
    public void testSetAndGetSearchText() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        Assert.assertNull(itemSearch.getSearchText());
        itemSearch.setSearchText("network timeout");
        Assert.assertEquals("network timeout", itemSearch.getSearchText());

        itemSearch.setSearchText("");
        Assert.assertNull(itemSearch.getSearchText());
    }

    @Test
    public void testInitFromPageParametersWithSearchText() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        User user = new User();
        PageParameters params = new PageParameters();
        params.set("pageSize", "25");
        params.set("searchText", "crash report");

        itemSearch.initFromPageParameters(params, user, null);
        Assert.assertEquals("crash report", itemSearch.getSearchText());

        PageParameters qs = itemSearch.getAsQueryString();
        Assert.assertEquals("crash report", qs.get("searchText").toString());
    }

    @Test
    public void testInitFromPageParametersLegacySummaryMapping() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        User user = new User();
        PageParameters params = new PageParameters();
        params.set("pageSize", "25");
        params.set("summary", "like_database error");

        itemSearch.initFromPageParameters(params, user, null);
        Assert.assertEquals("database error", itemSearch.getSearchText());
    }

    @Test
    public void testInitFromPageParametersLegacyDetailMapping() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        User user = new User();
        PageParameters params = new PageParameters();
        params.set("pageSize", "25");
        params.set("detail", "like_attachment.pdf");

        itemSearch.initFromPageParameters(params, user, null);
        Assert.assertEquals("attachment.pdf", itemSearch.getSearchText());
    }

    @Test
    public void testInitFromPageParametersRawLegacyDetailMapping() {
        Space space = new Space();
        space.setMetadata(new Metadata());
        ItemSearch itemSearch = new ItemSearch(space);

        User user = new User();
        PageParameters params = new PageParameters();
        params.set("pageSize", "25");
        params.set("detail", "attachment.pdf");

        itemSearch.initFromPageParameters(params, user, null);
        Assert.assertEquals("attachment.pdf", itemSearch.getSearchText());
    }

    @Test
    public void testGlobalSearchSuperUserCriteriaDoesNotRestrictByEmptySpace() {
        User superUser = new User();
        superUser.setLoginName("admin");
        superUser.addSpaceWithRole(null, "ROLE_ADMIN");
        Assert.assertTrue(superUser.isSuperUser());
        Assert.assertTrue(superUser.getSpaces().isEmpty());

        ItemSearch itemSearch = new ItemSearch(superUser);
        Assert.assertNull(itemSearch.getSpace());
        Assert.assertTrue(itemSearch.getSelectedSpaces().isEmpty());

        org.hibernate.criterion.DetachedCriteria criteria = itemSearch.getCriteriaForCount();
        Assert.assertNotNull(criteria);
    }

    @Test
    public void testGlobalSearchRegularUserWithSpaces() {
        User user = new User();
        user.setLoginName("user1");
        Space space = new Space();
        space.setId(10);
        space.setPrefixCode("DEMO");
        space.setName("Demo Space");
        user.addSpaceWithRole(space, "ROLE_USER");
        Assert.assertFalse(user.isSuperUser());
        Assert.assertEquals(1, user.getSpaces().size());

        ItemSearch itemSearch = new ItemSearch(user);
        Assert.assertEquals(1, itemSearch.getSelectedSpaces().size());
        org.hibernate.criterion.DetachedCriteria criteria = itemSearch.getCriteriaForCount();
        Assert.assertNotNull(criteria);
    }

    @Test
    public void testGlobalSearchRegularUserWithNoSpaces() {
        User user = new User();
        user.setLoginName("guest");
        Assert.assertFalse(user.isSuperUser());
        Assert.assertTrue(user.getSpaces().isEmpty());

        ItemSearch itemSearch = new ItemSearch(user);
        Assert.assertTrue(itemSearch.getSelectedSpaces().isEmpty());
        org.hibernate.criterion.DetachedCriteria criteria = itemSearch.getCriteriaForCount();
        Assert.assertNotNull(criteria);
    }
}