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

package info.jtrac.wicket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;

/**
 * usage requires only passing a list dynamically by overriding
 */
public abstract class ReadOnlyRefreshingView<T> extends RefreshingView<T> {

    protected final AttributeModifier CLASS_ALT = AttributeModifier.replace("class", "alt");
    protected final AttributeModifier CLASS_SELECTED = AttributeModifier.replace("class", "selected");
    protected final AttributeModifier CLASS_ERROR_BACK = AttributeModifier.replace("class", "error-back");
    
    public ReadOnlyRefreshingView(String id) {
        super(id);
    }

    public abstract List<T> getObjectList();

    @Override
    protected Iterator<IModel<T>> getItemModels() {
        List<T> list = getObjectList();
        List<IModel<T>> models = new ArrayList<IModel<T>>(list.size());
        for (final T o : list) {
            models.add(new AbstractReadOnlyModel<T>() {
                @Override
                public T getObject() {
                    return o;
                }
            });
        }
        return models.iterator();
    }
}
