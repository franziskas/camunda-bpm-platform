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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedVariableValueImpl;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.ValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableValueSerializers;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * @author Tom Baeyens
 */
public class VariableInstanceEntity implements VariableInstance, CoreVariableInstance, ValueFields, DbEntity, HasDbRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String activityInstanceId;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected VariableValue cachedValue;

  /** the name of the serializer used to serialize the value of this variable */
  protected String serializerName;

  boolean forcedUpdate;

  protected String errorMessage;

  protected String dataFormatId;
  protected String configuration;

  // Default constructor for SQL mapping
  public VariableInstanceEntity() {
  }

  public static VariableInstanceEntity createAndInsert(String name, VariableValue value) {
    VariableInstanceEntity variableInstance = create(name, value);
    insert(variableInstance);

    return variableInstance;
  }

  public static void insert(VariableInstanceEntity variableInstance) {
    Context
    .getCommandContext()
    .getDbEntityManager()
    .insert(variableInstance);
  }

  public static VariableInstanceEntity create(String name, VariableValue value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.setValue(value);

    return variableInstance;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
    forcedUpdate = true;
  }

  public void delete() {

    // clear value
    clearValueFields();

    // delete variable
    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (serializerName != null) {
      persistentState.put("serializerName", serializerName);
    }
    if (longValue != null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue != null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue != null) {
      persistentState.put("textValue", textValue);
    }
    if (textValue2 != null) {
      persistentState.put("textValue2", textValue2);
    }
    if (byteArrayValueId != null) {
      persistentState.put("byteArrayValueId", byteArrayValueId);
    }
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }
    if (dataFormatId != null) {
      persistentState.put("dataFormatId", dataFormatId);
    }
    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  // byte array value /////////////////////////////////////////////////////////

  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity,
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity

  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayValueId = byteArrayValueId;
    this.byteArrayValue = null;
  }

  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      // no lazy fetching outside of command context
      if(Context.getCommandContext() != null) {
        byteArrayValue = Context
          .getCommandContext()
          .getDbEntityManager()
          .selectById(ByteArrayEntity.class, byteArrayValueId);
      }
    }
    return byteArrayValue;
  }

  public void setByteArrayValue(byte[] bytes) {
    ByteArrayEntity byteArrayValue = null;
    if (this.byteArrayValueId!=null) {
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(byteArrayValueId);
    }
    if (bytes!=null) {
      byteArrayValue = new ByteArrayEntity(bytes);
      Context
        .getCommandContext()
        .getDbEntityManager()
        .insert(byteArrayValue);
    }
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
  }

  protected void deleteByteArrayValue() {
    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(byteArrayValueId);
    }
  }

  // type /////////////////////////////////////////////////////////////////////

  public Object getValue() {
    VariableValue variableValue = getTypedValue();
    if(variableValue != null) {
      return variableValue.getValue();
    } else {
      return null;
    }
  }

  public VariableValue getTypedValue() {
    if (errorMessage == null && cachedValue == null) {
      try {
        cachedValue = getSerializer().getValue(this);

      } catch(RuntimeException e) {
        // catch error message
        errorMessage = e.getMessage();

        //re-throw the exception
        throw e;
      }
    }
    return cachedValue;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public VariableValue setValue(VariableValue variableValue) {

    ValueSerializer serializer = null;

    if(serializerName != null) {
      ValueSerializer<?> currentSerializer = getSerializer();

      // check whether the current serializer can candle the new value
      if(currentSerializer.canSerialize(variableValue)) {
        serializer = currentSerializer;
      }
    }

    // if the current serializer cannot handle the value, attempt to determine a new serializer
    if(serializer == null) {
      serializer = getSerializers().findSerializerForValue(variableValue);
      // serializer changed -> clear value fields
      clearValueFields();
    }

    // set new value
    serializer.setValue(variableValue, this);

    if(variableValue instanceof UntypedVariableValueImpl) {
      // type has been detected
      variableValue = serializer.getValue(this);
    }

    return variableValue;
  }

  public void clearValueFields() {
    this.longValue = null;
    this.doubleValue = null;
    this.textValue = null;
    this.textValue2 = null;

    if(this.byteArrayValueId != null) {
      deleteByteArrayValue();
      setByteArrayValueId(null);
    }
  }

  public String getTypeName() {
    VariableValue typedValue = getTypedValue();
    if(typedValue != null) {
      return typedValue.getType().getName();
    }
    else {
      return null;
    }
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTextValue() {
    return textValue;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getName() {
    return name;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public void setSerializer(ValueSerializer<?> serializer) {
    this.serializerName = serializer.getName();
  }

  public void setSerializerName(String type) {
    this.serializerName = type;
  }

  public ValueSerializer<?> getSerializer() {
    if(serializerName != null) {
      return getSerializers().getSerializerByName(serializerName);
    } else {
      throw new ProcessEngineException("No serializer defined for this variable instance");
    }
  }

  public static VariableValueSerializers getSerializers() {
    if(Context.getCommandContext() != null) {
      return Context.getProcessEngineConfiguration()
          .getVariableTypes();
    } else {
      throw new ProcessEngineException("Cannot work with serializers outside of command context.");
    }
  }

  public VariableValue getCachedValue() {
    return cachedValue;
  }

  public void setCachedValue(VariableValue cachedValue) {
    this.cachedValue = cachedValue;
  }

  public String getTextValue2() {
    return textValue2;
  }

  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String acitivtyInstanceId) {
    this.activityInstanceId = acitivtyInstanceId;
  }

  public String getSerializerName() {
    return serializerName;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getDataFormatId() {
    return dataFormatId;
  }

  public void setDataFormatId(String dataFormatId) {
    this.dataFormatId = dataFormatId;
  }

  public String getVariableScope() {
    if (taskId != null) {
      return taskId;
    }

    if (executionId != null) {
      return executionId;
    }

    return caseExecutionId;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
      + "[id=" + id
      + ", revision=" + revision
      + ", name=" + name
      + ", processInstanceId=" + processInstanceId
      + ", executionId=" + executionId
      + ", caseInstanceId=" + caseInstanceId
      + ", caseExecutionId=" + caseExecutionId
      + ", taskId=" + taskId
      + ", activityInstanceId=" + activityInstanceId
      + ", longValue=" + longValue
      + ", doubleValue=" + doubleValue
      + ", textValue=" + textValue
      + ", textValue2=" + textValue2
      + ", byteArrayValue=" + byteArrayValue
      + ", byteArrayValueId=" + byteArrayValueId
      + ", forcedUpdate=" + forcedUpdate
      + ", dataFormatId=" + dataFormatId
      + ", configuration=" + configuration
      + "]";
  }

}
