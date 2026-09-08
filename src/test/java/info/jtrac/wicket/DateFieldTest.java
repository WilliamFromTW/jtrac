package info.jtrac.wicket;

import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateFieldTest {

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester();
    }

    @After
    public void tearDown() {
        if (tester != null) {
            tester.destroy();
        }
    }

    @Test
    public void testDateFieldInputTypes() {
        DateField dateField = new DateField("testDate");
        String[] types = dateField.getInputTypes();
        Assert.assertNotNull(types);
        Assert.assertTrue(Arrays.asList(types).contains("date"));
        Assert.assertTrue(Arrays.asList(types).contains("text"));
    }

    @Test
    public void testDateFieldConverter() {
        DateField dateField = new DateField("testDate");
        IConverter<Date> converter = dateField.getConverter(Date.class);
        Assert.assertNotNull(converter);

        // Convert string to Date
        Date parsed = converter.convertToObject("2026-09-09", Locale.ENGLISH);
        Assert.assertNotNull(parsed);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parsed);
        Assert.assertEquals(2026, cal.get(Calendar.YEAR));
        Assert.assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));
        Assert.assertEquals(9, cal.get(Calendar.DAY_OF_MONTH));

        // Convert Date to string
        String formatted = converter.convertToString(parsed, Locale.ENGLISH);
        Assert.assertEquals("2026-09-09", formatted);

        // Null and empty handling
        Assert.assertNull(converter.convertToObject(null, Locale.ENGLISH));
        Assert.assertNull(converter.convertToObject("", Locale.ENGLISH));
        Assert.assertEquals("", converter.convertToString(null, Locale.ENGLISH));
    }
}
