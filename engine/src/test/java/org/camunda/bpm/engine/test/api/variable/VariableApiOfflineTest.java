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
package org.camunda.bpm.engine.test.api.variable;

import static org.camunda.bpm.engine.variable.VariableType.*;
import static org.camunda.bpm.engine.variable.Variables.*;
import static org.junit.Assert.*;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.BooleanVariableValue;
import org.camunda.bpm.engine.variable.value.DeserializedObjectVariableValue;
import org.camunda.bpm.engine.variable.value.IntegerVariableValue;
import org.camunda.bpm.engine.variable.value.ObjectVariableValue;
import org.camunda.bpm.engine.variable.value.StringVariableValue;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class VariableApiOfflineTest {

  private static final String STRING_VAR_NAME = "stringVariable";
  private static final String STRING_VAR_VALUE = "someString";

  private static final String INTEGER_VAR_NAME = "integerVariable";
  private static final Integer INTEGER_VAR_VALUE = 1;

  private static final String BOOLEAN_VAR_NAME = "booleanVariable";
  private static final Boolean BOOLEAN_VAR_VALUE = true;

  private static final String DESERIALIZED_OBJECT_VAR_NAME = "deserializedObject";
  private static final ExampleObject DESERIALIZED_OBJECT_VAR_VALUE = new ExampleObject();

  private static final String JSON_DATA_FORMAT_NAME = "application/json";


  @Test
  public void testCreatePrimitiveVariables() {

    VariableMap variables = createVariables()
      .variable(STRING_VAR_NAME, STRING_VAR_VALUE)
      .variable(INTEGER_VAR_NAME, INTEGER_VAR_VALUE)
      .variable(BOOLEAN_VAR_NAME, BOOLEAN_VAR_VALUE);

    assertEquals(STRING_VAR_VALUE, variables.get(STRING_VAR_NAME));
    assertEquals(INTEGER_VAR_VALUE, variables.get(INTEGER_VAR_NAME));
    assertEquals(BOOLEAN_VAR_VALUE, variables.get(BOOLEAN_VAR_NAME));

    assertEquals(STRING_VAR_VALUE, variables.getTypedValue(STRING_VAR_NAME).getValue());
    assertEquals(INTEGER_VAR_VALUE, variables.getTypedValue(INTEGER_VAR_NAME).getValue());
    assertEquals(BOOLEAN_VAR_VALUE, variables.getTypedValue(BOOLEAN_VAR_NAME).getValue());

    // no type information present

    try {
      variables.getTypedValue(STRING_VAR_NAME).getType();
      fail("exception expected");
    }catch(UnsupportedOperationException e) {
      // expected
    }

    try {
      variables.getTypedValue(INTEGER_VAR_NAME).getType();
      fail("exception expected");
    }catch(UnsupportedOperationException e) {
      // expected
    }

    try {
      variables.getTypedValue(BOOLEAN_VAR_NAME).getType();
      fail("exception expected");
    }catch(UnsupportedOperationException e) {
      // expected
    }

  }

  @Test
  public void testCreatePrimitiveVariablesWrapped() {

    VariableMap variables = createVariables()
      .variable(STRING_VAR_NAME, stringValue(STRING_VAR_VALUE))
      .variable(INTEGER_VAR_NAME, integerValue(INTEGER_VAR_VALUE))
      .variable(BOOLEAN_VAR_NAME, booleanValue(BOOLEAN_VAR_VALUE));

    assertEquals(STRING_VAR_VALUE, variables.get(STRING_VAR_NAME));
    assertEquals(INTEGER_VAR_VALUE, variables.get(INTEGER_VAR_NAME));
    assertEquals(BOOLEAN_VAR_VALUE, variables.get(BOOLEAN_VAR_NAME));

    // types are not lost

    assertEquals(STRING, variables.getTypedValue(STRING_VAR_NAME).getType());
    assertEquals(INTEGER, variables.getTypedValue(INTEGER_VAR_NAME).getType());
    assertEquals(BOOLEAN, variables.getTypedValue(BOOLEAN_VAR_NAME).getType());

    // get wrappers

    String stringValue = variables.<StringVariableValue>getTypedValue(STRING_VAR_NAME).getValue();
    assertEquals(STRING_VAR_VALUE, stringValue);
    Integer integerValue = variables.<IntegerVariableValue>getTypedValue(INTEGER_VAR_NAME).getValue();
    assertEquals(INTEGER_VAR_VALUE, integerValue);
    Boolean booleanValue = variables.<BooleanVariableValue>getTypedValue(BOOLEAN_VAR_NAME).getValue();
    assertEquals(BOOLEAN_VAR_VALUE, booleanValue);

    // types are not lost

    assertEquals(STRING, variables.getTypedValue(STRING_VAR_NAME).getType());
    assertEquals(INTEGER, variables.getTypedValue(INTEGER_VAR_NAME).getType());
    assertEquals(BOOLEAN, variables.getTypedValue(BOOLEAN_VAR_NAME).getType());

  }

  @Test
  public void testCreatePrimitiveVariablesNull() {

    VariableMap variables = createVariables()
      .variable(STRING_VAR_NAME, stringValue(null))
      .variable(INTEGER_VAR_NAME, integerValue(null))
      .variable(BOOLEAN_VAR_NAME, booleanValue(null));

    // get unwrapped values

    assertEquals(null, variables.get(STRING_VAR_NAME));
    assertEquals(null, variables.get(INTEGER_VAR_NAME));
    assertEquals(null, variables.get(BOOLEAN_VAR_NAME));

    // get wrappers

    String stringValue = variables.<StringVariableValue>getTypedValue(STRING_VAR_NAME).getValue();
    assertEquals(null, stringValue);
    Integer integerValue = variables.<IntegerVariableValue>getTypedValue(INTEGER_VAR_NAME).getValue();
    assertEquals(null, integerValue);
    Boolean booleanValue = variables.<BooleanVariableValue>getTypedValue(BOOLEAN_VAR_NAME).getValue();
    assertEquals(null, booleanValue);

    // types are not lost

    assertEquals(STRING, variables.getTypedValue(STRING_VAR_NAME).getType());
    assertEquals(INTEGER, variables.getTypedValue(INTEGER_VAR_NAME).getType());
    assertEquals(BOOLEAN, variables.getTypedValue(BOOLEAN_VAR_NAME).getType());

  }

  @Test
  public void testCreateObjectVariables() {

    VariableMap variables = createVariables()
      .variable(DESERIALIZED_OBJECT_VAR_NAME, objectValue(DESERIALIZED_OBJECT_VAR_VALUE));

    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.get(DESERIALIZED_OBJECT_VAR_NAME));
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.getValue(DESERIALIZED_OBJECT_VAR_NAME, ExampleObject.class));
    Object untypedValue = variables.getTypedValue(DESERIALIZED_OBJECT_VAR_NAME).getValue();
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, untypedValue);
    ExampleObject typedValue = variables.<DeserializedObjectVariableValue>getTypedValue(DESERIALIZED_OBJECT_VAR_NAME).getValue(ExampleObject.class);
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, typedValue);

    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE.getClass().getCanonicalName(), variables.<ObjectVariableValue>getTypedValue(DESERIALIZED_OBJECT_VAR_NAME).getObjectTypeName());
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE.getClass(), variables.<DeserializedObjectVariableValue>getTypedValue(DESERIALIZED_OBJECT_VAR_NAME).getObjectType());


    variables = createVariables()
        .variable(DESERIALIZED_OBJECT_VAR_NAME, objectValue(DESERIALIZED_OBJECT_VAR_VALUE).serializationDataFormat(JSON_DATA_FORMAT_NAME));

    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.get(DESERIALIZED_OBJECT_VAR_NAME));


  }


}
