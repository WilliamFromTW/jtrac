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

import info.jtrac.exception.SearchQueryParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene Index Searching implementation using native Lucene API
 */
public class IndexSearcher {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Directory indexDirectory;
    private Analyzer analyzer;

    public void setIndexDirectory(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public boolean validateQuery(String text) {
        if (text == null || text.trim().length() == 0) {
            return true;
        }
        QueryParser parser = new QueryParser(Version.LUCENE_29, "text", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        try {
            parser.parse(text);
            return true;
        } catch (ParseException e) {
            try {
                parser.parse(QueryParser.escape(text));
                return true;
            } catch (ParseException pe) {
                return false;
            }
        }
    }

    public List<Long> findItemIdsContainingText(String text) {
        QueryParser parser = new QueryParser(Version.LUCENE_29, "text", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.OR);
        Query query;
        try {
            query = parser.parse(text);
        } catch (ParseException e) {
            logger.debug("Query parsing failed for raw text '{}', attempting escaped fallback: {}", text, e.getMessage());
            try {
                query = parser.parse(QueryParser.escape(text));
            } catch (ParseException pe) {
                logger.warn("Query parsing failed for escaped '{}': {}", text, pe.getMessage());
                throw new SearchQueryParseException(pe.getMessage(), pe);
            }
        }

        try {
            if (!IndexReader.indexExists(indexDirectory)) {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Error checking index existence", e);
            return Collections.emptyList();
        }

        IndexReader reader = null;
        org.apache.lucene.search.IndexSearcher searcher = null;
        try {
            reader = IndexReader.open(indexDirectory, true);
            searcher = new org.apache.lucene.search.IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, 1000);

            // If no hits found and query text contains special characters (e.g. Service:Auth), attempt escaped query fallback
            if (topDocs.scoreDocs.length == 0 && containsLuceneSpecialChars(text)) {
                try {
                    Query escapedQuery = parser.parse(QueryParser.escape(text));
                    TopDocs escapedDocs = searcher.search(escapedQuery, 1000);
                    if (escapedDocs.scoreDocs.length > 0) {
                        topDocs = escapedDocs;
                    }
                } catch (Exception e) {
                    logger.debug("Escaped fallback search failed for '{}': {}", text, e.getMessage());
                }
            }

            // If no hits found for a simple single word, attempt automatic prefix wildcard fallback (e.g. win -> win*)
            if (topDocs.scoreDocs.length == 0 && isEligibleForPrefixFallback(text)) {
                try {
                    Query fallbackQuery = parser.parse(text.trim() + "*");
                    TopDocs fallbackDocs = searcher.search(fallbackQuery, 1000);
                    if (fallbackDocs.scoreDocs.length > 0) {
                        topDocs = fallbackDocs;
                    }
                } catch (Exception e) {
                    logger.debug("Prefix fallback search failed for '{}': {}", text, e.getMessage());
                }
            }

            List<Long> hitIds = new ArrayList<Long>(topDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Long id = ItemIdHitExtractor.extractItemId(doc);
                if (id != null) {
                    hitIds.add(id);
                }
            }
            return hitIds;
        } catch (Exception e) {
            logger.error("Error searching index for query: " + text, e);
            throw new RuntimeException("Error searching index for query: " + text, e);
        } finally {
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (Exception e) {
                    logger.error("Error closing Lucene IndexSearcher", e);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    logger.error("Error closing Lucene IndexReader", e);
                }
            }
        }
    }

    private boolean isEligibleForPrefixFallback(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.length() < 2) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-') {
                return false;
            }
        }
        return true;
    }

    private boolean containsLuceneSpecialChars(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')'
                    || c == '{' || c == '}' || c == '[' || c == ']' || c == '^'
                    || c == '"' || c == '~' || c == '*' || c == '?' || c == '\\' || c == '/') {
                return true;
            }
        }
        return false;
    }

}
