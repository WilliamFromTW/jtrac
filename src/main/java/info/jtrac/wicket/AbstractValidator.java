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

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Compatibility adapter for Wicket 1.3 AbstractValidator in Wicket 9
 */
public abstract class AbstractValidator<T> implements IValidator<T> {
    private static final long serialVersionUID = 1L;

    @Override
    public void validate(IValidatable<T> validatable) {
        onValidate(validatable);
    }

    protected abstract void onValidate(IValidatable<T> validatable);

    protected String resourceKey() {
        return null;
    }

    protected void error(IValidatable<T> validatable) {
        ValidationError error = new ValidationError();
        String key = resourceKey();
        if (key != null) {
            error.addKey(key);
        }
        validatable.error(error);
    }

    protected void error(IValidatable<T> validatable, String resourceKey) {
        ValidationError error = new ValidationError();
        error.addKey(resourceKey);
        validatable.error(error);
    }
}
