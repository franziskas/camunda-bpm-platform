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
package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.VariableType;
import org.camunda.bpm.engine.variable.value.ObjectVariableValue;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractObjectSerializer extends AbstractValueSerializer<ObjectVariableValue> {

  protected String serializationDataFormat;

  public AbstractObjectSerializer(String serializationDataFormat) {
    super(VariableType.OBJECT);
    this.serializationDataFormat = serializationDataFormat;
  }

  public void setValue(ObjectVariableValue value, ValueFields valueFields) {
    ByteArrayValueSerializer.setBytes(valueFields, getBytes(value, valueFields));
  }

  public ObjectVariableValue getValue(ValueFields valueFields) {

    // reuse cached object
    VariableValue cachedObject = valueFields.getCachedValue();
    if (cachedObject!=null) {
      if(cachedObject instanceof ObjectVariableValue) {
        return (ObjectVariableValue) cachedObject;
      }
      else {
        return createTypedValue(cachedObject.getValue());
      }
    }

    Object object = null;

    byte[] bytes = ByteArrayValueSerializer.getBytes(valueFields);
    if(bytes != null) {
      getObject(bytes, valueFields);
    }

    return createTypedValue(object);
  }

  protected abstract byte[] getBytes(ObjectVariableValue value, ValueFields valueFields);

  protected abstract Object getObject(byte[] bytes, ValueFields valueFields);

  protected boolean canSerializeValue(VariableValue variableValue) {
    Object value = variableValue.getValue();

    boolean canHandleValue = (value == null || canSerializeObject(value));

    // throw exception if user requested serialization by the dataformat used by this
    // serializer but the serializer is unable to handle the value.
    if(value instanceof ObjectVariableValue) {
      String requestedDataFormat = ((ObjectVariableValue)value).getSerializationDataFormat();
      if(requestedDataFormat != null && !serializationDataFormat.equals(requestedDataFormat) && !canHandleValue) {
        throw new ProcessEngineException("Cannot serialize object '"+value+"' with requested dataformat '"+requestedDataFormat+".");
      }
    }

    return canHandleValue;
  }

  protected abstract boolean canSerializeObject(Object value);

  protected ObjectVariableValue createTypedValue(Object object) {
    return Variables.objectValue(object)
      .serializationDataFormat(serializationDataFormat)
      .create();
  }

}
