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

import java.io.Serializable;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;

/**
 * Compatibility adapter for Wicket 1.3 SimpleAttributeModifier in Wicket 9
 */
public class SimpleAttributeModifier extends AttributeModifier {
    private static final long serialVersionUID = 1L;

    public SimpleAttributeModifier(String attribute, Serializable value) {
        super(attribute, value);
    }

    public SimpleAttributeModifier(String attribute, IModel<?> replaceModel) {
        super(attribute, replaceModel);
    }
}
