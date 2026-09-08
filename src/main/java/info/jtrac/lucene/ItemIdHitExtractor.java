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

package info.jtrac.lucene;

import org.apache.lucene.document.Document;

/**
 * Converts a search hit Document into an Item id, which we can later use to load the actual object
 */
public class ItemIdHitExtractor {

    public static Long extractItemId(Document document) {
        if (document == null) {
            return null;
        }
        String type = document.get("type");
        if (type == null) {
            return null;
        }
        if (type.equals("item")) {
            String id = document.get("id");
            return Long.valueOf(id);
        } else if (type.equals("history")) {
            String itemId = document.get("itemId");
            return Long.valueOf(itemId);
        } else {
            throw new RuntimeException("Unexpected lucene search result: " + document);
        }
    }
}
