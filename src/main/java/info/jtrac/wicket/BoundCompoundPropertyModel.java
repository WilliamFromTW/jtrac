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

import org.apache.wicket.Component;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

/**
 * Compatibility adapter for Wicket 1.3 BoundCompoundPropertyModel in Wicket 9
 */
public class BoundCompoundPropertyModel<T> extends CompoundPropertyModel<T> {
    private static final long serialVersionUID = 1L;

    public BoundCompoundPropertyModel(T object) {
        super(object);
    }

    public BoundCompoundPropertyModel(IModel<T> model) {
        super(model);
    }

    public <C extends Component> C bind(C component) {
        return component;
    }

    public <C extends Component> C bind(C component, String propertyExpression) {
        component.setDefaultModel(bind(propertyExpression));
        return component;
    }
}
