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

package org.camunda.bpm.engine.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.delegate.ProcessEngineVariableType;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.VariableTypes;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceQueryImpl extends AbstractQuery<HistoricVariableInstanceQuery, HistoricVariableInstance> implements
        HistoricVariableInstanceQuery {

  private final static Logger LOGGER = Logger.getLogger(HistoricVariableInstanceQueryImpl.class.getName());

  private static final long serialVersionUID = 1L;
  protected String variableId;
  protected String processInstanceId;
  protected String variableName;
  protected String variableNameLike;
  protected QueryVariableValue queryVariableValue;
  protected String[] taskIds;
  protected String[] executionIds;
  protected String[] activityInstanceIds;

  protected boolean isByteArrayFetchingEnabled = true;
  protected boolean isCustomObjectDeserializationEnabled = true;

  public HistoricVariableInstanceQueryImpl() {
  }

  public HistoricVariableInstanceQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public HistoricVariableInstanceQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public HistoricVariableInstanceQuery variableId(String id) {
    ensureNotNull("variableId", id);
    this.variableId = id;
    return this;
  }

  public HistoricVariableInstanceQueryImpl processInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoricVariableInstanceQuery taskIdIn(String... taskIds) {
    ensureNotNull("Task Ids", taskIds);
    this.taskIds = taskIds;
    return this;
  }

  public HistoricVariableInstanceQuery executionIdIn(String... executionIds) {
    ensureNotNull("Execution Ids", executionIds);
    this.executionIds = executionIds;
    return this;
  }

  public HistoricVariableInstanceQuery activityInstanceIdIn(String... activityInstanceIds) {
    ensureNotNull("Activity Instance Ids", activityInstanceIds);
    this.activityInstanceIds = activityInstanceIds;
    return this;
  }

  public HistoricVariableInstanceQuery variableName(String variableName) {
    ensureNotNull("variableName", variableName);
    this.variableName = variableName;
    return this;
  }

  public HistoricVariableInstanceQuery variableValueEquals(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensureNotNull("variableValue", variableValue);
    this.variableName = variableName;
    queryVariableValue = new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true);
    return this;
  }

  public HistoricVariableInstanceQuery variableNameLike(String variableNameLike) {
    ensureNotNull("variableNameLike", variableNameLike);
    this.variableNameLike = variableNameLike;
    return this;
  }

  protected void ensureVariablesInitialized() {
    if (this.queryVariableValue != null) {
      VariableTypes variableTypes = Context.getProcessEngineConfiguration().getVariableTypes();
      queryVariableValue.initialize(variableTypes);
    }
  }

  public HistoricVariableInstanceQuery disableBinaryFetching() {
    isByteArrayFetchingEnabled = false;
    return this;
  }

  public HistoricVariableInstanceQuery disableCustomObjectDeserialization() {
    this.isCustomObjectDeserializationEnabled = false;
    return this;
  }

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    ensureVariablesInitialized();
    return commandContext.getHistoricVariableInstanceManager().findHistoricVariableInstanceCountByQueryCriteria(this);
  }

  public List<HistoricVariableInstance> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    ensureVariablesInitialized();
    List<HistoricVariableInstance> historicVariableInstances = commandContext
            .getHistoricVariableInstanceManager()
            .findHistoricVariableInstancesByQueryCriteria(this, page);

    if (historicVariableInstances!=null) {
      for (HistoricVariableInstance historicVariableInstance: historicVariableInstances) {

        HistoricVariableInstanceEntity variableInstanceEntity = (HistoricVariableInstanceEntity) historicVariableInstance;

        if (shouldFetchSerializedValueFor(variableInstanceEntity)) {
          try {
            variableInstanceEntity.getSerializedValue();

            if (shouldFetchValueFor(variableInstanceEntity)) {
              variableInstanceEntity.getValue();
            }

          } catch(Exception t) {
            // do not fail if one of the variables fails to load
            LOGGER.log(Level.FINE, "Exception while getting value for variable", t);
          }
        }

      }
    }
    return historicVariableInstances;
  }

  /**
   * eagerly fetch the variable's value unless the serialized value should not be fetched
   * or custom object fetching is disabled
   */
  protected boolean shouldFetchValueFor(HistoricVariableInstanceEntity variableInstance) {
    boolean shouldFetchCustomObjects = !variableInstance.storesCustomObjects() || isCustomObjectDeserializationEnabled;

    return shouldFetchSerializedValueFor(variableInstance) && shouldFetchCustomObjects;
  }

  /**
   * Eagerly fetch the variable's serialized value unless the type is "bytes" and
   * binary fetching disabled
   */
  protected boolean shouldFetchSerializedValueFor(HistoricVariableInstanceEntity variableInstance) {
    boolean shouldFetchBytes = !ProcessEngineVariableType.BYTES.getName().equals(variableInstance.getVariableType().getTypeName())
        || isByteArrayFetchingEnabled;

    return shouldFetchBytes;
  }

  // order by /////////////////////////////////////////////////////////////////

  public HistoricVariableInstanceQuery orderByProcessInstanceId() {
    orderBy(HistoricVariableInstanceQueryProperty.PROCESS_INSTANCE_ID);
    return this;
  }

  public HistoricVariableInstanceQuery orderByVariableName() {
    orderBy(HistoricVariableInstanceQueryProperty.VARIABLE_NAME);
    return this;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String[] getActivityInstanceIds() {
    return activityInstanceIds;
  }

  public String[] getTaskIds() {
    return taskIds;
  }

  public String[] getExecutionIds() {
    return executionIds;
  }

  public String getVariableName() {
    return variableName;
  }

  public String getVariableNameLike() {
    return variableNameLike;
  }

  public QueryVariableValue getQueryVariableValue() {
    return queryVariableValue;
  }

}
