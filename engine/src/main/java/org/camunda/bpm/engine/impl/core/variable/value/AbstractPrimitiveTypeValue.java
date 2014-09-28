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

import org.camunda.bpm.engine.variable.type.PrimitiveValueVariableType;
import org.camunda.bpm.engine.variable.value.PrimitiveTypeValue;

/**
 * @author Daniel Meyer
 *
 */
public class AbstractPrimitiveTypeValue<T> extends AbstractVariableValue<T> implements PrimitiveTypeValue<T> {

  private static final long serialVersionUID = 1L;

  public AbstractPrimitiveTypeValue(T value, PrimitiveValueVariableType<T> type) {
    super(value, type);
  }

  public T setValue(T value) {
    T prevValue = this.value;
    this.value = value;
    return prevValue;
  }

  @SuppressWarnings("unchecked")
  public PrimitiveValueVariableType<T> getType() {
    return (PrimitiveValueVariableType<T>) super.getType();
  }

}
