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

import org.camunda.bpm.engine.variable.type.VariableType;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * A {@link ValueSerializer} persists {@link VariableValue VariableValues} of a given
 * {@link VariableType Type} to provided {@link ValueFields}.
 *
 * @author Daniel Meyer
 * @author Tom Baeyens
 * @author roman.smirnov
 */
public interface ValueSerializer<T extends VariableValue> {

  /**
   * The name of this serializer. The name is used when persisting the ValueFields populated by this serializer.
   *
   * @return the name of this serializer.
   */
  String getName();

  /**
   * The {@link VariableType VariableType} supported by this serializer.
   * @return the VariableType supported by this serializer
   */
  VariableType getType();

  /**
   * Serialize a {@link VariableValue} to the {@link ValueFields}.
   *
   * @param value the {@link VariableValue} to persist
   * @param valueFields the {@link ValueFields} to which the value should be persisted
   */
  void setValue(T value, ValueFields valueFields);

  /**
   * Retrieve a {@link VariableValue} from the provided {@link ValueFields}.
   *
   * @param valueFields the {@link ValueFields} to retrieve the value from
   * @return the {@link VariableValue}
   */
  T getValue(ValueFields valueFields);

  /**
   * Used for auto-detecting the value type of a variable.
   * An implementation must return true if it is able to handle values of the provided type.
   *
   * @param value the value
   * @return true if this {@link ValueSerializer} is able to handle the provided value
   */
  boolean canSerialize(VariableValue value);
}
