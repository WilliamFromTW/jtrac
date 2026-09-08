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

import java.io.File;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring FactoryBean to initialize a native Lucene FSDirectory
 */
public class LuceneDirectoryFactoryBean implements FactoryBean<Directory> {

    private String path;

    public void setPath(String path) {
        if (path != null && path.contains("${jtrac.home}")) {
            String jtracHome = System.getProperty("jtrac.home");
            if (jtracHome == null || jtracHome.trim().isEmpty()) {
                jtracHome = "target/home";
            }
            path = path.replace("${jtrac.home}", jtracHome);
        }
        this.path = path;
    }

    @Override
    public Directory getObject() throws Exception {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return FSDirectory.open(dir);
    }

    @Override
    public Class<?> getObjectType() {
        return Directory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
