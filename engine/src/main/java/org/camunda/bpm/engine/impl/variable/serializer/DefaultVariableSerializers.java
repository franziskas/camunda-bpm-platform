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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedVariableValueImpl;
import org.camunda.bpm.engine.variable.type.VariableType;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class DefaultVariableSerializers implements Serializable, VariableValueSerializers {

  private static final long serialVersionUID = 1L;

  protected List<ValueSerializer<?>> serializerList = new ArrayList<ValueSerializer<?>>();
  protected Map<String, ValueSerializer<?>> serializerMap = new HashMap<String, ValueSerializer<?>>();

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void serializeValue(VariableValue value, ValueFields valueFields) {

    ValueSerializer serializer = findSerializerForValue(value);
    serializer.setValue(value, valueFields);

  }

  public ValueSerializer<?> getSerializerByName(String serializerName) {
    return serializerMap.get(serializerName);
  }

  public ValueSerializer<?> findSerializerForValue(VariableValue value) {

    VariableType type = null;

    if(!(value instanceof UntypedVariableValueImpl)) {
      type = value.getType();
    }

    for (ValueSerializer<?> serializer : serializerList) {
      if(type == null || serializer.getType().equals(type)) {
        // if type is null => ask handler whether it can handle the value
        // if types match, this handler can handle values of this type
        //    => BUT we still need to ask as the handler may not be able to handle ALL values of this type.
        if(serializer.canSerialize(value)) {
          return serializer;
        }
      }
    }

    throw new ProcessEngineException("Could not find a serializer for value " + value);
  }

  public DefaultVariableSerializers addSerializer(ValueSerializer<?> serializer) {
    return addSerializer(serializer, serializerList.size());
  }

  public DefaultVariableSerializers addSerializer(ValueSerializer<?> serializer, int index) {
    serializerList.add(index, serializer);
    serializerMap.put(serializer.getName(), serializer);
    return this;
  }

  public void setSerializerList(List<ValueSerializer<?>> serializerList) {
    this.serializerList.clear();
    this.serializerList.addAll(serializerList);
    this.serializerMap.clear();
    for (ValueSerializer<?> serializer : serializerList) {
      serializerMap.put(serializer.getName(), serializer);
    }
  }

  public int getSerializerIndex(ValueSerializer<?> serializer) {
    return serializerList.indexOf(serializer);
  }

  public int getSerializerIndexByName(String serializerName) {
    ValueSerializer<?> serializer = serializerMap.get(serializerName);
    if(serializer != null) {
      return getSerializerIndex(serializer);
    } else {
      return -1;
    }
  }

  public VariableValueSerializers removeSerializer(ValueSerializer<?> serializer) {
    serializerList.remove(serializer);
    serializerMap.remove(serializer.getName());
    return this;
  }

}
