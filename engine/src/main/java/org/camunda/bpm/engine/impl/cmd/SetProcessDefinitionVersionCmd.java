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

package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.*;


/**
 * {@link Command} that changes the process definition version of an existing
 * process instance.
 * 
 * Warning: This command will NOT perform any migration magic and simply set the
 * process definition version in the database, assuming that the user knows,
 * what he or she is doing.
 * 
 * This is only useful for simple migrations. The new process definition MUST
 * have the exact same activity id to make it still run.
 * 
 * Furthermore, activities referenced by sub-executions and jobs that belong to
 * the process instance MUST exist in the new process definition version.
 * 
 * The command will fail, if there is already a {@link ProcessInstance} or
 * {@link HistoricProcessInstance} using the new process definition version and
 * the same business key as the {@link ProcessInstance} that is to be migrated.
 * 
 * If the process instance is not currently waiting but actively running, then
 * this would be a case for optimistic locking, meaning either the version
 * update or the "real work" wins, i.e., this is a race condition.
 * 
 * @see http://forums.activiti.org/en/viewtopic.php?t=2918
 * @author Falko Menge
 */
public class SetProcessDefinitionVersionCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  private final String processInstanceId;
  private final Integer processDefinitionVersion;

  public SetProcessDefinitionVersionCmd(String processInstanceId, Integer processDefinitionVersion) {
    ensureNotEmpty("The process instance id is mandatory", "processInstanceId", processInstanceId);
    ensureNotNull("The process definition version is mandatory", "processDefinitionVersion", processDefinitionVersion);
    ensurePositive("The process definition version must be positive", "processDefinitionVersion", processDefinitionVersion);
    this.processInstanceId = processInstanceId;
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public Void execute(CommandContext commandContext) {
    // check that the new process definition is just another version of the same
    // process definition that the process instance is using
    ExecutionManager executionManager = commandContext.getExecutionManager();
    ExecutionEntity processInstance = executionManager.findExecutionById(processInstanceId);
    if (processInstance == null) {
      throw new ProcessEngineException("No process instance found for id = '" + processInstanceId + "'.");
    } else if (!processInstance.isProcessInstanceExecution()) {
      throw new ProcessEngineException(
        "A process instance id is required, but the provided id " +
        "'"+processInstanceId+"' " +
        "points to a child execution of process instance " +
        "'"+processInstance.getProcessInstanceId()+"'. " +
        "Please invoke the "+getClass().getSimpleName()+" with a root execution id.");
    }
    ProcessDefinitionImpl currentProcessDefinitionImpl = processInstance.getProcessDefinition();

    DeploymentCache deploymentCache = Context
      .getProcessEngineConfiguration()
      .getDeploymentCache();
    ProcessDefinitionEntity currentProcessDefinition;
    if (currentProcessDefinitionImpl instanceof ProcessDefinitionEntity) {
      currentProcessDefinition = (ProcessDefinitionEntity) currentProcessDefinitionImpl;
    } else {
      currentProcessDefinition = deploymentCache.findDeployedProcessDefinitionById(currentProcessDefinitionImpl.getId());
    }

    ProcessDefinitionEntity newProcessDefinition = deploymentCache
      .findDeployedProcessDefinitionByKeyAndVersion(currentProcessDefinition.getKey(), processDefinitionVersion);
    
    validateAndSwitchVersionOfExecution(commandContext, processInstance, newProcessDefinition);
    
    // switch the historic process instance to the new process definition version
    HistoricProcessInstanceManager historicProcessInstanceManager = commandContext.getHistoricProcessInstanceManager();
    if (historicProcessInstanceManager.isHistoryEnabled()) {
      HistoricProcessInstanceEntity historicProcessInstance = historicProcessInstanceManager.findHistoricProcessInstance(processInstanceId);
      historicProcessInstance.setProcessDefinitionId(newProcessDefinition.getId());
    }
    
    // switch all sub-executions of the process instance to the new process definition version
    List<ExecutionEntity> childExecutions = executionManager
      .findChildExecutionsByParentExecutionId(processInstanceId);
    for (ExecutionEntity executionEntity : childExecutions) {
      validateAndSwitchVersionOfExecution(commandContext, executionEntity, newProcessDefinition);
    }
    
    return null;
  }

  protected void validateAndSwitchVersionOfExecution(CommandContext commandContext, ExecutionEntity execution, ProcessDefinitionEntity newProcessDefinition) {
    // check that the new process definition version contains the current activity
    if (execution.getActivity() != null) {
      String activityId = execution.getActivity().getId();
      ActivityImpl newActivity = newProcessDefinition.findActivity(activityId);

      if (newActivity == null) {
        throw new ProcessEngineException(
          "The new process definition " +
          "(key = '" + newProcessDefinition.getKey() + "') " +
          "does not contain the current activity " +
          "(id = '" + activityId + "') " +
          "of the process instance " +
          "(id = '" + processInstanceId + "').");
        }

        // clear cached activity so that outgoing transitions are refreshed
        execution.setActivity(newActivity);
      }

    // switch the process instance to the new process definition version
    execution.setProcessDefinition(newProcessDefinition);
    
    // and change possible existing tasks (as the process definition id is stored there too)
    List<TaskEntity> tasks = commandContext.getTaskManager().findTasksByExecutionId(execution.getId());
    for (TaskEntity taskEntity : tasks) {
      taskEntity.setProcessDefinitionId(newProcessDefinition.getId());
    }
  }

}
