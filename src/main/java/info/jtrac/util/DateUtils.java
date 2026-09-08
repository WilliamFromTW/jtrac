/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.util;

import info.jtrac.domain.User;
import info.jtrac.wicket.JtracApplication;
import info.jtrac.wicket.JtracSession;
import org.apache.wicket.Application;
import org.apache.wicket.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.util.StringUtils;

import org.ocpsoft.prettytime.PrettyTime;

/**
 * Date Formatting helper, currently date formats are hard-coded for the entire app
 * hence the use of static SimpleDateFormat instances, although they are known not to be synchronized
 */

public class DateUtils {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static boolean showPretty() {
		if (Session.exists()) {
			User user = JtracSession.get().getUser();
			if (user != null) {
				return user.isPrettyDates();
			}
		}
		return true;
	}

	private static PrettyTime getPrettyTime() {
		if (Session.exists()) {
			User user = JtracSession.get().getUser();
			if (user != null && user.getLocale() != null) {
				return new PrettyTime(new Locale(user.getLocale()));
			}
		}
		if (Application.exists()) {
			String defaultLocale = JtracApplication.get().getJtrac().getDefaultLocale();
			if (defaultLocale != null) {
				return new PrettyTime(new Locale(defaultLocale));
			}
		}
		return new PrettyTime(Locale.ENGLISH);
	}

    public static String format (Date date) {
		return format(date, false);
    }

	// the date format must never be pretty when used in search queries
    public static String format (Date date, boolean neverPretty) {
        return date == null
				? ""
				: (showPretty() && ! neverPretty
					? getPrettyTime().format(date)
					: dateFormat.format(date));
    }

    public static String formatTimeStamp (Date date) {
        return date == null
				? ""
				: (showPretty()
					? getPrettyTime().format(date)
					: dateTimeFormat.format(date));
    }

    public static Date convert (String s) {
        try {
            return dateFormat.parse(s);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
