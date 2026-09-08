package info.jtrac.htmlunit;

import java.io.IOException; 

import org.junit.*;
import org.junit.runners.MethodSorters;

import org.htmlunit.*;
import org.htmlunit.html.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicTest {

	private static WebClient webClient;
	private static HtmlPage page;

	@BeforeClass
	public static void init() throws Exception {
		webClient = new WebClient();
		//webClient.getOptions().setThrowExceptionOnScriptError(false);
	}

	@AfterClass
	public static void close() throws Exception {
		webClient.close();
	}

	@Test
    public void test_A_GetLoginPage() throws IOException {
        page = webClient.getPage("http://localhost:8888/app/login");
		WebAssert.assertTitleEquals(page, "JTrac Login");
    }

	@Test
    public void test_B_SuccessfulLogin() throws IOException {
		((HtmlElement) page.getElementByName("loginName")).type("admin");
		((HtmlElement) page.getElementByName("password")).type("admin");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Submit']")).click();
		WebAssert.assertTextPresent(page, "DASHBOARD");
    }

	@Test
    public void test_C_CreateNewSpaceAndAllocateAdmin() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'OPTIONS']]")).click();
        WebAssert.assertTextPresent(page, "Options Menu");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Manage Spaces']]")).click();
        WebAssert.assertTextPresent(page, "Space List");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Create New Space']]")).click();
        WebAssert.assertTextPresent(page, "Space Details");
		((HtmlElement) page.getElementByName("space.name")).type("Test Space");
		((HtmlElement) page.getElementByName("space.prefixCode")).type("TEST");
		((HtmlCheckBoxInput) page.getFirstByXPath("//input[@name='space.isActive']")).click();
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Next']")).click();
        WebAssert.assertTextPresent(page, "Custom Fields for Space");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Next']")).click();
        WebAssert.assertTextPresent(page, "Space Roles");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Save']")).click();
        WebAssert.assertTextPresent(page, "Users Allocated To Space");
		((HtmlCheckBoxInput) page.getFirstByXPath("//input[@name='roleAllocatePanel:checkGroup']")).click();
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Allocate']")).click();
        WebAssert.assertTextPresent(page, "Admin");
    }

	@Test
    public void test_D_CreateNewItem() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'DASHBOARD']]")).click();
        WebAssert.assertTextPresent(page, "Test Space");
		page = ((HtmlElement) page.getFirstByXPath("//img[@title='NEW']")).click();
        WebAssert.assertTextPresent(page, "Summary");
		((HtmlElement) page.getElementByName("summary")).type("Test Summary");
		((HtmlElement) page.getElementByName("detail")).type("Test Detail");
		((HtmlSelect) page.getElementByName("hideAssignedTo:border:assignedTo")).getOptionByText("Admin").setSelected(true);
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Submit']")).click();
        WebAssert.assertTitleContains(page, "TEST-1");
    }

	@Test
    public void test_E_SearchAllContainsItem() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'SEARCH']]")).click();
        WebAssert.assertTextPresent(page, "Show History");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Search']")).click();
        WebAssert.assertTextPresent(page, "1 Record Found");
		page = ((HtmlElement) page.getFirstByXPath("//a[contains(@href, 'TEST-1')]")).click();
        WebAssert.assertTextPresent(page, "History");
    }

	@Test
    public void test_F_UpdateHistoryForItem() throws Exception {
		((HtmlSelect) page.getElementByName("status")).setSelectedAttribute("Closed", true);
		((HtmlElement) page.getElementByName("comment")).type("Test Comment");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Submit']")).click();
        WebAssert.assertTextPresent(page, "Test Comment");
    }

	@Test
    public void test_G_CreateNewUser() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'OPTIONS']]")).click();
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Manage Users']]")).click();
        WebAssert.assertTextPresent(page, "Users and allocated Spaces");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Create New User']]")).click();
        WebAssert.assertTextPresent(page, "User Details");
		((HtmlElement) page.getElementByName("user.loginName")).type("testuser");
		((HtmlElement) page.getElementByName("user.name")).type("Test User");
		((HtmlElement) page.getElementByName("user.email")).type("foo@bar.com");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Submit']")).click();
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Search']")).click();
        WebAssert.assertTextPresent(page, "Test User");
    }

	@Test
    public void test_H_CreateStoredSearch() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'OPTIONS']]")).click();
        WebAssert.assertTextPresent(page, "Options Menu");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Configure Links']]")).click();
        WebAssert.assertTextPresent(page, "Link configuration");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Create a new link']]")).click();
        WebAssert.assertTextPresent(page, "Link details");
		((HtmlElement) page.getElementByName("name")).type("Test Link");
		((HtmlElement) page.getElementByName("query")).type("http://localhost:8888/app/item/list?summary=like_test");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Submit']")).click();
        WebAssert.assertTextPresent(page, "Test Link");
    }

	@Test
    public void test_I_CheckStoredSearch() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'DASHBOARD']]")).click();
        WebAssert.assertTextPresent(page, "Test Link");
		page = ((HtmlElement) page.getFirstByXPath("//a[contains(@href, 'like_test')]")).click();
        WebAssert.assertTextPresent(page, "1 Record Found");
    }

	@Test
    public void test_J_Wiki() throws IOException {
        page = webClient.getPage("http://localhost:8888/wiki/view");
		WebAssert.assertTitleEquals(page, "Home Page");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'SandBox']]")).click();
		WebAssert.assertTitleEquals(page, "Sand Box");
		((HtmlElement) page.getElementByName("q")).type("qweqwe");
		page = ((HtmlElement) page.getFirstByXPath("//input[@value='Search']")).click();
        WebAssert.assertTextPresent(page, "No results for");
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'Dashboard']]")).click();
		WebAssert.assertTitleEquals(page, "JTrac");
    }

	@Test
    public void test_K_Logout() throws Exception {
		page = ((HtmlElement) page.getFirstByXPath("//a[text()[normalize-space(.) = 'LOGOUT']]")).click();
		WebAssert.assertTextPresent(page, "Logout Successful");
    }
}
