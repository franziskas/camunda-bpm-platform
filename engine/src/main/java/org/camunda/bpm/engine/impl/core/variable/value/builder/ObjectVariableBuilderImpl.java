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
package org.camunda.bpm.engine.impl.core.variable.value.builder;

import org.camunda.bpm.engine.impl.core.variable.value.DeserializedObjectVariableValueImpl;
import org.camunda.bpm.engine.variable.builder.ObjectVariableBuilder;
import org.camunda.bpm.engine.variable.value.DeserializedObjectVariableValue;
import org.camunda.bpm.engine.variable.value.ObjectVariableValue;

/**
 * @author Daniel Meyer
 *
 */
public class ObjectVariableBuilderImpl implements ObjectVariableBuilder {

  protected DeserializedObjectVariableValueImpl variableValue;

  public ObjectVariableBuilderImpl(Object value) {
    variableValue = new DeserializedObjectVariableValueImpl(value);
  }

  public ObjectVariableBuilderImpl(DeserializedObjectVariableValue value) {
    variableValue = (DeserializedObjectVariableValueImpl) value;
  }

  public ObjectVariableValue create() {
    return variableValue;
  }

  public ObjectVariableBuilder serializationDataFormat(String dataFormatName) {
    variableValue.setSerializationDataFormat(dataFormatName);
    return this;
  }

}
