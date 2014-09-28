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
package org.camunda.bpm.engine.variable.type;

import java.io.Serializable;
import java.util.Date;

import javax.lang.model.type.NullType;

import org.camunda.bpm.engine.impl.core.variable.type.AbstractVariableType;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueVariableTypeImpl;

/**
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 */
public interface VariableType extends Serializable {

  public static final PrimitiveValueVariableType<NullType> NULL = new AbstractVariableType<NullType>("Null");

  public static final PrimitiveValueVariableType<byte[]> BYTES = new PrimitiveValueVariableTypeImpl<byte[]>("Bytes", byte[].class);

  public static final PrimitiveValueVariableType<Boolean> BOOLEAN = new PrimitiveValueVariableTypeImpl<Boolean>(Boolean.TYPE);

  public static final PrimitiveValueVariableType<Short> SHORT = new PrimitiveValueVariableTypeImpl<Short>(Short.TYPE);

  public static final PrimitiveValueVariableType<Long> LONG = new PrimitiveValueVariableTypeImpl<Long>(Long.TYPE);

  public static final PrimitiveValueVariableType<Double> DOUBLE = new PrimitiveValueVariableTypeImpl<Double>(Double.TYPE);

  public static final PrimitiveValueVariableType<Date> DATE = new PrimitiveValueVariableTypeImpl<Date>(Date.class);

  public static final PrimitiveValueVariableType<String> STRING = new PrimitiveValueVariableTypeImpl<String>(String.class);

  public static final PrimitiveValueVariableType<Integer> INTEGER = new PrimitiveValueVariableTypeImpl<Integer>(Integer.TYPE);

  public static final VariableType OBJECT = new AbstractVariableType("Object", false);

  /**
   * Returns the canonical name of the variable type
   */
  String getName();

  /**
   * Indicates whether this type is primitive valued. Primitive valued types correspond to Java primitives and
   * can be handled natively by the process engine.
   *
   * @return true if this is a primitive valued type. False otherwise
   */
  boolean isPrimitiveValueType();

}
