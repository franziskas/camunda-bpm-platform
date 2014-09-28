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
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * Interface describing a container for all available {@link ValueSerializer}s of variables.
 *
 * @author dsyer
 * @author Frederik Heremans
 * @author Daniel Meyer
 */
public interface VariableValueSerializers {

  /**
   * Selects the {@link ValueSerializer} which should be used for persisting a VariableValue.
   *
   *
   * @param value the value to persist
   * @return the VariableValueserializer selected for persisting the value
   * @throws ProcessEngineException in case no matching serializer can be found
   */
  @SuppressWarnings("rawtypes")
  public ValueSerializer findSerializerForValue(VariableValue value);

  /**
   *
   * @return the serializer for the given serializerName name.
   * Returns null if no type was found with the name.
   */
  public ValueSerializer<?> getSerializerByName(String serializerName);

  public VariableValueSerializers addSerializer(ValueSerializer<?> serializer);

  /**
   * Add type at the given index. The index is used when finding a serializer for a VariableValue. When
   * different serializers can store a specific variable value, the one with the smallest
   * index will be used.
   */
  public VariableValueSerializers addSerializer(ValueSerializer<?> serializer, int index);

  public VariableValueSerializers removeSerializer(ValueSerializer<?> serializer);

  public int getSerializerIndex(ValueSerializer<?> serializer);

  public int getSerializerIndexByName(String serializerName);

}