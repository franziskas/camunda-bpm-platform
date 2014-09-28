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
package org.camunda.bpm.engine.variable;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.core.variable.value.BooleanVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.BytesVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.DateVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.DoubleVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.IntegerVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.LongVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.NullVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.ShortVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.StringVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedVariableValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.ObjectVariableBuilderImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.SerializedObjectVariableBuilderImpl;
import org.camunda.bpm.engine.variable.builder.ObjectVariableBuilder;
import org.camunda.bpm.engine.variable.builder.SerializedObjectVariableBuilder;
import org.camunda.bpm.engine.variable.builder.VariableBuilder;
import org.camunda.bpm.engine.variable.value.BooleanVariableValue;
import org.camunda.bpm.engine.variable.value.BytesVariableValue;
import org.camunda.bpm.engine.variable.value.DateVariableValue;
import org.camunda.bpm.engine.variable.value.DoubleVariableValue;
import org.camunda.bpm.engine.variable.value.IntegerVariableValue;
import org.camunda.bpm.engine.variable.value.LongVariableValue;
import org.camunda.bpm.engine.variable.value.ShortVariableValue;
import org.camunda.bpm.engine.variable.value.StringVariableValue;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 *
 * @author Daniel Meyer
 *
 */
public class Variables {

  public static VariableMap createVariables() {
    return new VariableMapImpl();
  }

  public static VariableMap fromMap(Map<String, Object> map) {
    if(map instanceof VariableMap) {
      return (VariableMap) map;
    }
    else {
      return new VariableMapImpl(map);
    }
  }

  public static ObjectVariableBuilder objectValue(Object value) {
    return new ObjectVariableBuilderImpl(value);
  }

  public static SerializedObjectVariableBuilder serializedObjectValue(byte[] value) {
    return new SerializedObjectVariableBuilderImpl(value);
  }

  public static SerializedObjectVariableBuilder serializedObjectValue(String value) {
    try {
      return new SerializedObjectVariableBuilderImpl(value.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      throw new ProcessEngineException(e);
    }
  }

  public static IntegerVariableValue integerValue(Integer integer) {
    return new IntegerVariableValueImpl(integer);
  }

  public static StringVariableValue stringValue(String stringValue) {
    return new StringVariableValueImpl(stringValue);
  }

  public static BooleanVariableValue booleanValue(Boolean booleanValue) {
    return new BooleanVariableValueImpl(booleanValue);
  }

  public static BytesVariableValue byteArrayValue(byte[] bytes) {
    return new BytesVariableValueImpl(bytes);
  }

  public static DateVariableValue dateValue(Date date) {
    return new DateVariableValueImpl(date);
  }

  public static LongVariableValue longValue(Long longValue) {
    return new LongVariableValueImpl(longValue);
  }

  public static ShortVariableValue shortValue(Short shortValue) {
    return new ShortVariableValueImpl(shortValue);
  }

  public static DoubleVariableValue doubleValue(Double doubleValue) {
    return new DoubleVariableValueImpl(doubleValue);
  }

  public static VariableValue untypedNullValue() {
    return NullVariableValueImpl.INSTANCE;
  }

  public static VariableValue untypedValue(Object value) {
    if(value == null) {
      return untypedNullValue();
    }
    else if (value instanceof VariableBuilder<?>) {
      return ((VariableBuilder<?>) value).create();
    }
    else if (value instanceof VariableValue) {
      return (VariableValue) value;
    }
    else {
      // unknown value
      return new UntypedVariableValueImpl(value);
    }
  }
}
