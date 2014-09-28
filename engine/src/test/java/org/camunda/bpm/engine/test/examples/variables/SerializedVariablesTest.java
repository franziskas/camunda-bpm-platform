/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.examples.variables;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.SerializedObjectVariableValue;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableType;
import org.junit.Assert;

public class SerializedVariablesTest extends PluggableProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String ONE_SUB_PROCESS = "org/camunda/bpm/engine/test/api/oneSubProcess.bpmn20.xml";

  protected static byte[] byteArray = new byte[] {42, 47};
  protected static SimpleSerializableBean serializable = new SimpleSerializableBean(42);
  protected static long longValue = 12345678L;

  protected static final VariableSpec BOOLEAN_VARIABLE_SPEC = new VariableSpec()
    .name("booleanVariable")
    .value(true)
    .serializedValue(true)
    .variableTypeName(VariableType.BOOLEAN.getName())
    .valueTypeName(Boolean.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec BYTE_ARRAY_VARIABLE_SPEC = new VariableSpec()
    .name("byteArrayVariable")
    .value(byteArray)
    .serializedValue(byteArray)
    .variableTypeName(VariableType.BYTES.getName())
    .valueTypeName(byte[].class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec SERIALIZABLE_VARIABLE_SPEC = new VariableSpec()
    .name("serializableVariable")
    .value(serializable)
    .serializedValue(toExpectedBytes(serializable))
    .variableTypeName(VariableType.SERIALIZABLE.getName())
    .valueTypeName(Serializable.class.getSimpleName())
    .storesCustomObjects(true)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec DATE_VARIABLE_SPEC = new VariableSpec()
    .name("dateVariable")
    .value(new Date(longValue))
    .serializedValue(longValue)
    .variableTypeName(VariableType.DATE.getName())
    .valueTypeName(Date.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec INT_VARIABLE_SPEC = new VariableSpec()
    .name("intVariable")
    .value(42)
    .serializedValue(42)
    .variableTypeName(VariableType.INTEGER.getName())
    .valueTypeName(Integer.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec DOUBLE_VARIABLE_SPEC = new VariableSpec()
    .name("doubleVariable")
    .value(4.2d)
    .serializedValue(4.2d)
    .variableTypeName(VariableType.DOUBLE.getName())
    .valueTypeName(Double.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec SHORT_VARIABLE_SPEC = new VariableSpec()
    .name("shortVariable")
    .value((short) 4)
    .serializedValue((short) 4)
    .variableTypeName(VariableType.SHORT.getName())
    .valueTypeName(Short.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec LONG_VARIABLE_SPEC = new VariableSpec()
    .name("longVariable")
    .value(longValue)
    .serializedValue(longValue)
    .variableTypeName(VariableType.LONG.getName())
    .valueTypeName(Long.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec NULL_VARIABLE_SPEC = new VariableSpec()
    .name("nullVariable")
    .value(null)
    .serializedValue(null)
    .variableTypeName(VariableType.NULL.getName())
    .valueTypeName("Null")
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  protected static final VariableSpec STRING_VARIABLE_SPEC = new VariableSpec()
    .name("stringVariable")
    .value("a String value")
    .serializedValue("a String value")
    .variableTypeName(VariableType.STRING.getName())
    .valueTypeName(String.class.getSimpleName())
    .storesCustomObjects(false)
    .configuration(new HashMap<String, Object>());

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedBooleanValue() {
    assertGetSerializedValue("oneTaskProcess", BOOLEAN_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedBooleanValue() {
    assertSetSerializedValue("oneTaskProcess", BOOLEAN_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedByteArrayValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap(BYTE_ARRAY_VARIABLE_SPEC.getName(), BYTE_ARRAY_VARIABLE_SPEC.getValue()));

    VariableInstance variableInstance = getVariableInstance(BYTE_ARRAY_VARIABLE_SPEC.getName());
    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();
    Assert.assertArrayEquals((byte[]) BYTE_ARRAY_VARIABLE_SPEC.getSerializedValue(), (byte[]) serializedValue.getTypedValue());
    assertEquals(BYTE_ARRAY_VARIABLE_SPEC.getConfiguration(), serializedValue.getConfig());
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedByteArrayValue() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.setVariableFromSerialized(instance.getId(), BYTE_ARRAY_VARIABLE_SPEC.getName(), BYTE_ARRAY_VARIABLE_SPEC.getSerializedValue(),
        BYTE_ARRAY_VARIABLE_SPEC.getVariableTypeName(), BYTE_ARRAY_VARIABLE_SPEC.getConfiguration());

    VariableInstance variableInstance = getVariableInstance(BYTE_ARRAY_VARIABLE_SPEC.getName());
    assertEquals(BYTE_ARRAY_VARIABLE_SPEC.getName(), variableInstance.getName());
    Assert.assertArrayEquals((byte[]) BYTE_ARRAY_VARIABLE_SPEC.getSerializedValue(), (byte[]) variableInstance.getTypedValue());
    assertEquals(BYTE_ARRAY_VARIABLE_SPEC.getVariableTypeName(), variableInstance.getSerializerName());

    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();

    Assert.assertArrayEquals((byte[]) BYTE_ARRAY_VARIABLE_SPEC.getSerializedValue(), (byte[]) serializedValue.getTypedValue());
    assertEquals(BYTE_ARRAY_VARIABLE_SPEC.getConfiguration(), serializedValue.getConfig());
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedSerializableValue() {
    runtimeService.startProcessInstanceByKey("oneTaskProcess", newMap(SERIALIZABLE_VARIABLE_SPEC.getName(), SERIALIZABLE_VARIABLE_SPEC.getValue()));

    VariableInstance variableInstance = getVariableInstance(SERIALIZABLE_VARIABLE_SPEC.getName());
    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();
    Assert.assertArrayEquals((byte[]) SERIALIZABLE_VARIABLE_SPEC.getSerializedValue(), (byte[]) serializedValue.getTypedValue());
    assertEquals(SERIALIZABLE_VARIABLE_SPEC.getConfiguration(), serializedValue.getConfig());
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedSerializableValue() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    runtimeService.setVariableFromSerialized(instance.getId(), SERIALIZABLE_VARIABLE_SPEC.getName(), SERIALIZABLE_VARIABLE_SPEC.getSerializedValue(),
        SERIALIZABLE_VARIABLE_SPEC.getVariableTypeName(), SERIALIZABLE_VARIABLE_SPEC.getConfiguration());

    VariableInstance variableInstance = getVariableInstance(SERIALIZABLE_VARIABLE_SPEC.getName());
    assertEquals(SERIALIZABLE_VARIABLE_SPEC.getName(), variableInstance.getName());
    assertEquals(SERIALIZABLE_VARIABLE_SPEC.getValue(), variableInstance.getTypedValue());
    assertEquals(SERIALIZABLE_VARIABLE_SPEC.getVariableTypeName(), variableInstance.getSerializerName());

    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();

    Assert.assertArrayEquals((byte[]) SERIALIZABLE_VARIABLE_SPEC.getSerializedValue(), (byte[]) serializedValue.getTypedValue());
    assertEquals(SERIALIZABLE_VARIABLE_SPEC.getConfiguration(), serializedValue.getConfig());
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedDateValue() {
    assertGetSerializedValue("oneTaskProcess", DATE_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedDateValue() {
    assertSetSerializedValue("oneTaskProcess", DATE_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedIntValue() {
    assertGetSerializedValue("oneTaskProcess", INT_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedIntValue() {
    assertSetSerializedValue("oneTaskProcess", INT_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedDoubleValue() {
    assertGetSerializedValue("oneTaskProcess", DOUBLE_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedDoubleValue() {
    assertSetSerializedValue("oneTaskProcess", DOUBLE_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedShortValue() {
    assertGetSerializedValue("oneTaskProcess", SHORT_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedShortValue() {
    assertSetSerializedValue("oneTaskProcess", SHORT_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedLongValue() {
    assertGetSerializedValue("oneTaskProcess", LONG_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedLongValue() {
    assertSetSerializedValue("oneTaskProcess", LONG_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedNullTypeValue() {
    assertGetSerializedValue("oneTaskProcess", NULL_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedNullTypeValue() {
    assertSetSerializedValue("oneTaskProcess", NULL_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testGetSerializedStringValue() {
    assertGetSerializedValue("oneTaskProcess", STRING_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedStringValue() {
    assertSetSerializedValue("oneTaskProcess", STRING_VARIABLE_SPEC);
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetMalformedVariableType() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    try {
      runtimeService.setVariableFromSerialized(instance.getId(), "aName", null, "No Valid Type", null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetNullVariableType() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    try {
      runtimeService.setVariableFromSerialized(instance.getId(), "aName", null, null, null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }

  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedNullValue() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariableFromSerialized(instance.getId(), "aName", null,
        VariableType.STRING.getName(), null);

    VariableInstance variableInstance = getVariableInstance("aName");
    assertNotNull(variableInstance);
    assertEquals(VariableType.STRING.getName(), variableInstance.getSerializerName());
    assertNull(variableInstance.getTypedValue());
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetSerializedValueWithNullConfiguration() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariableFromSerialized(instance.getId(), "aName", "a String Value",
        VariableType.STRING.getName(), null);

    VariableInstance variableInstance = getVariableInstance("aName");
    assertNotNull(variableInstance);
    assertNotNull(variableInstance.getSerializedValue().getConfig());
    assertTrue(variableInstance.getSerializedValue().getConfig().isEmpty());
  }

  public void testSetSerializedValueForNonExistingExecution() {
    try {
      runtimeService.setVariableFromSerialized("a non existing id", "aName", "aValue",
          VariableType.STRING.getName(), null);
      fail();
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testUpdateSerializedVariable() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(instance.getId(), "aVariable", "aStringValue");

    // update to same type
    runtimeService.setVariableFromSerialized(instance.getId(), "aVariable", "anotherStringValue",
        VariableType.STRING.getName(), null);
    VariableInstance variableInstance = getVariableInstance("aVariable");
    assertNotNull(variableInstance);
    assertEquals("anotherStringValue", variableInstance.getTypedValue());
    assertEquals(VariableType.STRING.getName(), variableInstance.getSerializerName());

    // update to another type
    runtimeService.setVariableFromSerialized(instance.getId(), "aVariable", 42,
        VariableType.INTEGER.getName(), null);
    variableInstance = getVariableInstance("aVariable");
    assertNotNull(variableInstance);
    assertEquals(42, variableInstance.getTypedValue());
    assertEquals(VariableType.INTEGER.getName(), variableInstance.getSerializerName());
  }

  @Deployment(resources= ONE_SUB_PROCESS)
  public void testSetSerializedValueLocal() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("startSimpleSubProcess");
    String subprocessExecutionId = taskService.createTaskQuery().singleResult().getExecutionId();

    assertFalse(instance.getId().equals(subprocessExecutionId));

    runtimeService.setVariableFromSerialized(subprocessExecutionId, "aVariable", "aValue",
        VariableType.STRING.getName(), null);
    VariableInstance variableInstance = getVariableInstance("aVariable");
    assertNotNull(variableInstance);
    assertEquals(instance.getId(), variableInstance.getExecutionId());

    runtimeService.setVariableLocalFromSerialized(subprocessExecutionId, "aLocalVariable", "anothervalue",
        VariableType.STRING.getName(), null);
    variableInstance = getVariableInstance("aLocalVariable");
    assertNotNull(variableInstance);
    assertEquals(subprocessExecutionId, variableInstance.getExecutionId());
  }

  @Deployment
  public void testSetSerializedValueFromDelegate() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("variableToSet", "testVar");

    runtimeService.startProcessInstanceByKey("testProcess", vars);

    // assertions are part of the invoked delegate
  }

  @Deployment(resources= ONE_TASK_PROCESS)
  public void testSetNonNullSerializedValueForNullType() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      runtimeService.setVariableFromSerialized(instance.getId(), "aVar", "a non-null value", VariableType.NULL.getName(), null);
      fail("should fail as non-null value is not allowed for null type");
    } catch (BadUserRequestException e) {
      // expected
    }

  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSetSerializedValueForNullVariable() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    try {
      runtimeService.setVariableFromSerialized(instance.getId(), null, "value", VariableType.STRING.getName(), null);
      fail("should fail as null is not allowed as variable name");
    } catch (ProcessEngineException e) {
      // expected
    }
  }

  public void assertGetSerializedValue(String processKey, VariableSpec variableSpec) {
    runtimeService.startProcessInstanceByKey(processKey, newMap(variableSpec.getName(), variableSpec.getValue()));

    VariableInstance variableInstance = getVariableInstance(variableSpec.getName());
    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();
    assertEquals(variableSpec.getSerializedValue(), serializedValue.getTypedValue());
    assertEquals(variableSpec.getConfiguration(), serializedValue.getConfig());

  }

  public void assertSetSerializedValue(String processKey, VariableSpec variableSpec) {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(processKey);

    runtimeService.setVariableFromSerialized(instance.getId(), variableSpec.getName(), variableSpec.getSerializedValue(),
        variableSpec.getVariableTypeName(), variableSpec.getConfiguration());

    VariableInstance variableInstance = getVariableInstance(variableSpec.getName());
    assertEquals(variableSpec.getName(), variableInstance.getName());
    assertEquals(variableSpec.getValue(), variableInstance.getTypedValue());
    assertEquals(variableSpec.getVariableTypeName(), variableInstance.getSerializerName());
    assertEquals(variableSpec.getValueTypeName(), variableInstance.getValueTypeName());
    assertEquals(variableSpec.getStoresCustomObjects(), variableInstance.storesCustomObjects());

    SerializedObjectVariableValue serializedValue = variableInstance.getSerializedValue();

    assertEquals(variableSpec.getSerializedValue(), serializedValue.getTypedValue());
    assertEquals(variableSpec.getConfiguration(), serializedValue.getConfig());
  }

  protected VariableInstance getVariableInstance(String name) {
    return runtimeService.createVariableInstanceQuery().variableName(name).singleResult();
  }

  protected static byte[] toExpectedBytes(Object o) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = null;
    try {
      ois = new ObjectOutputStream(baos);
      ois.writeObject(o);
    } catch (IOException e) {
      throw new ProcessEngineException(e);
    } finally {
      IoUtil.closeSilently(ois);
    }
    return baos.toByteArray();
  }

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, value);
    return map;
  }
}
