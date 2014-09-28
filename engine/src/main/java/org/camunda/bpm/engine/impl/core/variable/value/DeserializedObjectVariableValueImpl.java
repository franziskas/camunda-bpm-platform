/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.core.variable.value;

import org.camunda.bpm.engine.variable.value.DeserializedObjectVariableValue;

/**
 * @author Daniel Meyer
 *
 */
public class DeserializedObjectVariableValueImpl extends ObjectVariableValueImpl implements DeserializedObjectVariableValue {

  private static final long serialVersionUID = 1L;

  protected String objectTypeName;

  public DeserializedObjectVariableValueImpl(Object value) {
    super(value);
  }

  public String getObjectTypeName() {
    if(objectTypeName == null) {
      Class<?> objectType = getObjectType();
      if(objectType != null) {
        return objectType.getCanonicalName();
      }
    }
    return objectTypeName;
  }

  public void setObjectTypeName(String objectTypeName) {
    this.objectTypeName = objectTypeName;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(Class<T> type) {
    return (T) getValue();
  }

  public Class<?> getObjectType() {
    Object value = getValue();
    if(value != null) {
      return value.getClass();
    } else {
      return null;
    }
  }

}
