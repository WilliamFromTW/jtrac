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

import info.jtrac.domain.AbstractItem;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lucene Indexing implementation using native Lucene API
 */
public class Indexer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Directory indexDirectory;
    private Analyzer analyzer;

    public void setIndexDirectory(Directory indexDirectory) {
        this.indexDirectory = indexDirectory;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public synchronized void index(AbstractItem item) {
        IndexWriter writer = null;
        try {
            if (IndexWriter.isLocked(indexDirectory)) {
                logger.warn("Forced unlocking of Lucene index directory: {}", indexDirectory);
                IndexWriter.unlock(indexDirectory);
            }
            boolean create = !IndexReader.indexExists(indexDirectory);
            writer = new IndexWriter(indexDirectory, analyzer, create, IndexWriter.MaxFieldLength.UNLIMITED);
            writer.addDocument(item.createDocument());
        } catch (Exception e) {
            logger.error("Error indexing item: " + item, e);
            throw new RuntimeException("Error indexing item: " + item, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                    logger.error("Error closing Lucene IndexWriter", e);
                }
            }
        }
    }
}
