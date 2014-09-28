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

import org.camunda.bpm.engine.impl.core.variable.value.SerializedObjectVariableValueImpl;
import org.camunda.bpm.engine.variable.builder.ObjectVariableBuilder;
import org.camunda.bpm.engine.variable.builder.SerializedObjectVariableBuilder;
import org.camunda.bpm.engine.variable.value.ObjectVariableValue;
import org.camunda.bpm.engine.variable.value.SerializedObjectVariableValue;

/**
 * @author Daniel Meyer
 *
 */
public class SerializedObjectVariableBuilderImpl implements SerializedObjectVariableBuilder {

  protected SerializedObjectVariableValueImpl variableValue;

  public SerializedObjectVariableBuilderImpl(byte[] value) {
    variableValue = new SerializedObjectVariableValueImpl(value);
  }

  public SerializedObjectVariableBuilderImpl(SerializedObjectVariableValue value) {
    variableValue = (SerializedObjectVariableValueImpl) value;
  }

  public ObjectVariableBuilder serializationDataFormat(String dataFormatName) {
    variableValue.setSerializationDataFormat(dataFormatName);
    return this;
  }

  public ObjectVariableValue create() {
    return variableValue;
  }

  public SerializedObjectVariableBuilder objectTypeName(String typeName) {
    variableValue.setObjectTypeName(typeName);
    return this;
  }

}
