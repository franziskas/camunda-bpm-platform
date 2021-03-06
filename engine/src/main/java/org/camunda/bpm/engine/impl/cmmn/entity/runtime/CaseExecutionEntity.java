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
package org.camunda.bpm.engine.impl.cmmn.entity.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnSentryPart;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.core.operation.CoreAtomicOperation;
import org.camunda.bpm.engine.impl.core.variable.CorePersistentVariableStore;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HasDbReferences;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.pvm.PvmProcessDefinition;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionEntity extends CmmnExecution implements CaseExecution, CaseInstance, DbEntity, HasDbRevision, HasDbReferences {

  private static final long serialVersionUID = 1L;

  // current position /////////////////////////////////////////////////////////

  /** the case instance.  this is the root of the execution tree.
   * the caseInstance of a case instance is a self reference. */
  protected transient CaseExecutionEntity caseInstance;

  /** the parent execution */
  protected transient CaseExecutionEntity parent;

  /** nested executions */
  protected List<CaseExecutionEntity> caseExecutions;

  /** nested case sentry parts */
  protected List<CaseSentryPartEntity> caseSentryParts;
  protected Map<String, List<CaseSentryPartEntity>> sentries;

  /** reference to a sub process instance, not-null if currently subprocess is started from this execution */
  protected transient ExecutionEntity subProcessInstance;

  protected transient CaseExecutionEntity subCaseInstance;

  protected transient CaseExecutionEntity superCaseExecution;

  // associated entities /////////////////////////////////////////////////////

  protected CaseExecutionEntityVariableStore variableStore = new CaseExecutionEntityVariableStore(this);

  // Persistence //////////////////////////////////////////////////////////////

  protected int revision = 1;
  protected String caseDefinitionId;
  protected String activityId;
  protected String activityName;
  protected String caseInstanceId;
  protected String parentId;
  protected String superCaseExecutionId;

  // case definition ///////////////////////////////////////////////////////////

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  /** ensures initialization and returns the case definition. */
  public CmmnCaseDefinition getCaseDefinition() {
    ensureCaseDefinitionInitialized();
    return caseDefinition;
  }

  public void setCaseDefinition(CmmnCaseDefinition caseDefinition) {
    super.setCaseDefinition(caseDefinition);

    caseDefinitionId = null;
    if (caseDefinition != null) {
      caseDefinitionId = caseDefinition.getId();
    }

  }

  protected void ensureCaseDefinitionInitialized() {
    if ((caseDefinition == null) && (caseDefinitionId != null)) {

      CaseDefinitionEntity deployedCaseDefinition = Context
        .getProcessEngineConfiguration()
        .getDeploymentCache()
        .getCaseDefinitionById(caseDefinitionId);

      setCaseDefinition(deployedCaseDefinition);
    }
  }

  // parent ////////////////////////////////////////////////////////////////////

  public CaseExecutionEntity getParent() {
    ensureParentInitialized();
    return parent;
  }

  public void setParent(CmmnExecution parent) {
    this.parent = (CaseExecutionEntity) parent;

    if (parent != null) {
      this.parentId = parent.getId();
    } else {
      this.parentId = null;
    }
  }

  protected void ensureParentInitialized() {
    if (parent == null && parentId != null) {

      parent = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(parentId);
    }
  }

  public String getParentId() {
    return parentId;
  }

  // activity //////////////////////////////////////////////////////////////////

  public String getActivityId() {
    return activityId;
  }

  public String getActivityName() {
    return activityName;
  }

  public CmmnActivity getActivity() {
    ensureActivityInitialized();
    return super.getActivity();
  }

  public void setActivity(CmmnActivity activity) {
    super.setActivity(activity);
    if (activity != null) {
      this.activityId = activity.getId();
      this.activityName = activity.getName();
    } else {
      this.activityId = null;
      this.activityName = null;
    }
  }

  protected void ensureActivityInitialized() {
    if ((activity == null) && (activityId != null)) {
      setActivity(getCaseDefinition().findActivity(activityId));
    }
  }

  // case executions ////////////////////////////////////////////////////////////////

  public List<CaseExecutionEntity> getCaseExecutions() {
    return new ArrayList<CaseExecutionEntity>(getCaseExecutionsInternal());
  }

  protected List<CaseExecutionEntity> getCaseExecutionsInternal() {
    ensureCaseExecutionsInitialized();
    return caseExecutions;
  }

  protected void ensureCaseExecutionsInitialized() {
    if (caseExecutions == null) {
      this.caseExecutions = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findChildCaseExecutionsByParentCaseExecutionId(id);
    }
  }

  // case instance /////////////////////////////////////////////////////////

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public CaseExecutionEntity getCaseInstance() {
    ensureCaseInstanceInitialized();
    return caseInstance;
  }

  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionEntity) caseInstance;

    if (caseInstance != null) {
      this.caseInstanceId = this.caseInstance.getId();
    }
  }

  protected void ensureCaseInstanceInitialized() {
    if ((caseInstance == null) && (caseInstanceId != null)) {

      caseInstance =  Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(caseInstanceId);

    }
  }

  @Override
  public boolean isCaseInstanceExecution() {
    return parentId == null;
  }

  protected CaseExecutionEntity createCaseExecution(CmmnActivity activity) {
    CaseExecutionEntity child = newCaseExecution();

    // set activity to execute
    child.setActivity(activity);

    // handle child/parent-relation
    child.setParent(this);
    getCaseExecutionsInternal().add(child);

    // set case instance
    child.setCaseInstance(getCaseInstance());

    // set case definition
    child.setCaseDefinition(getCaseDefinition());

    return child;
  }

  protected CaseExecutionEntity newCaseExecution() {
    CaseExecutionEntity newCaseExecution = new CaseExecutionEntity();

    Context
      .getCommandContext()
      .getCaseExecutionManager()
      .insertCaseExecution(newCaseExecution);

    return newCaseExecution;
  }

  // sub process instance ///////////////////////////////////////////////////

  public ExecutionEntity getSubProcessInstance() {
    ensureSubProcessInstanceInitialized();
    return subProcessInstance;
  }

  public void setSubProcessInstance(PvmExecutionImpl subProcessInstance) {
    this.subProcessInstance = (ExecutionEntity) subProcessInstance;
  }

  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition) {
    return createSubProcessInstance(processDefinition, null);
  }

  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey) {
    return createSubProcessInstance(processDefinition, businessKey, getCaseInstanceId());
  }

  public ExecutionEntity createSubProcessInstance(PvmProcessDefinition processDefinition, String businessKey, String caseInstanceId) {
    ExecutionEntity subProcessInstance = (ExecutionEntity) processDefinition.createProcessInstance(businessKey, caseInstanceId);

    // manage bidirectional super-subprocess relation
    subProcessInstance.setSuperCaseExecution(this);
    setSubProcessInstance(subProcessInstance);

    return subProcessInstance;
  }

  protected void ensureSubProcessInstanceInitialized() {
    if (subProcessInstance == null) {
      subProcessInstance = Context
        .getCommandContext()
        .getExecutionManager()
        .findSubProcessInstanceBySuperCaseExecutionId(id);
    }
  }

  // sub-/super- case instance ////////////////////////////////////////////////////

  public CaseExecutionEntity getSubCaseInstance() {
    ensureSubCaseInstanceInitialized();
    return subCaseInstance;
  }

  public void setSubCaseInstance(CmmnExecution subCaseInstance) {
    this.subCaseInstance = (CaseExecutionEntity) subCaseInstance;
  }

  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition) {
    return createSubCaseInstance(caseDefinition, null);
  }

  public CaseExecutionEntity createSubCaseInstance(CmmnCaseDefinition caseDefinition, String businessKey) {
    CaseExecutionEntity subCaseInstance = (CaseExecutionEntity) caseDefinition.createCaseInstance(businessKey);

    // manage bidirectional super-sub-case-instances relation
    subCaseInstance.setSuperCaseExecution(this);
    setSubCaseInstance(subCaseInstance);

    return subCaseInstance;
  }

  protected void ensureSubCaseInstanceInitialized() {
    if (subCaseInstance == null) {
      subCaseInstance = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findSubCaseInstanceBySuperCaseExecutionId(id);
    }
  }

  public String getSuperCaseExecutionId() {
    return superCaseExecutionId;
  }

  public void setSuperCaseExecutionId(String superCaseExecutionId) {
    this.superCaseExecutionId = superCaseExecutionId;
  }

  public CmmnExecution getSuperCaseExecution() {
    ensureSuperCaseExecutionInitialized();
    return superCaseExecution;
  }

  public void setSuperCaseExecution(CmmnExecution superCaseExecution) {
    this.superCaseExecution = (CaseExecutionEntity) superCaseExecution;

    if (superCaseExecution != null) {
      this.superCaseExecutionId = superCaseExecution.getId();
    } else {
      this.superCaseExecutionId = null;
    }
  }

  protected void ensureSuperCaseExecutionInitialized() {
    if (superCaseExecution == null && superCaseExecutionId != null) {
      superCaseExecution = Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(superCaseExecutionId);
    }
  }

  // sentry /////////////////////////////////////////////////////////////////////////

  public List<CaseSentryPartEntity> getCaseSentryParts() {
    ensureCaseSentryPartsInitialized();
    return caseSentryParts;
  }

  protected void ensureCaseSentryPartsInitialized() {
    if (caseSentryParts == null) {

      caseSentryParts = Context
        .getCommandContext()
        .getCaseSentryPartManager()
        .findCaseSentryPartsByCaseExecutionId(id);

      sentries = new HashMap<String, List<CaseSentryPartEntity>>();

      for (CaseSentryPartEntity sentryPart : caseSentryParts) {

        String sentryId = sentryPart.getSentryId();
        List<CaseSentryPartEntity> parts = sentries.get(sentryId);

        if (parts == null) {
          parts = new ArrayList<CaseSentryPartEntity>();
          sentries.put(sentryId, parts);
        }

        parts.add(sentryPart);

      }

    }
  }

  protected void addSentryPart(CmmnSentryPart sentryPart) {
    CaseSentryPartEntity entity = (CaseSentryPartEntity) sentryPart;

    getCaseSentryParts().add(entity);

    String sentryId = sentryPart.getSentryId();
    List<CaseSentryPartEntity> parts = sentries.get(sentryId);

    if (parts == null) {
      parts = new ArrayList<CaseSentryPartEntity>();
      sentries.put(sentryId, parts);
    }

    parts.add(entity);
  }

  protected List<CaseSentryPartEntity> findSentry(String sentryId) {
    ensureCaseSentryPartsInitialized();
    return sentries.get(sentryId);
  }

  protected CaseSentryPartEntity newSentryPart() {
    CaseSentryPartEntity caseSentryPart = new CaseSentryPartEntity();

    Context
      .getCommandContext()
      .getCaseSentryPartManager()
      .insertCaseSentryPart(caseSentryPart);

    return caseSentryPart;
  }

  // variables //////////////////////////////////////////////////////////////

  protected CorePersistentVariableStore getVariableStore() {
    return variableStore;
  }

  protected void initializeVariableInstanceBackPointer(VariableInstanceEntity variableInstance) {
    variableInstance.setCaseInstanceId(caseInstanceId);
    variableInstance.setCaseExecutionId(id);
  }

  protected List<VariableInstanceEntity> loadVariableInstances() {
    return Context
        .getCommandContext()
        .getVariableInstanceManager()
        .findVariableInstancesByCaseExecutionId(id);
  }

  // toString /////////////////////////////////////////////////////////////

  public String toString() {
    if (isCaseInstanceExecution()) {
      return "CaseInstance["+getToStringIdentity()+"]";
    } else {
      return "CaseExecution["+getToStringIdentity()+"]";
    }
  }

  protected String getToStringIdentity() {
    return id;
  }

  // delete/remove ///////////////////////////////////////////////////////

  public void remove() {
    super.remove();

    variableStore.removeVariablesWithoutFiringEvents();

    CommandContext commandContext = Context.getCommandContext();

    for (CaseSentryPartEntity sentryPart : getCaseSentryParts()) {
      commandContext
        .getCaseSentryPartManager()
        .deleteSentryPart(sentryPart);
    }

    // finally delete this execution
    commandContext
      .getCaseExecutionManager()
      .deleteCaseExecution(this);
  }

  // persistence /////////////////////////////////////////////////////////

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public int getRevisionNext() {
    return revision + 1;
  }

  public void forceUpdate() {
    Context.getCommandContext()
      .getDbEntityManager()
      .forceUpdate(this);
  }

  public boolean hasReferenceTo(DbEntity entity) {

    if (entity instanceof CaseExecutionEntity) {
      CaseExecutionEntity otherEntity = (CaseExecutionEntity) entity;
      String otherId = otherEntity.getId();

      // parentId
      if(parentId != null && parentId.equals(otherId)) {
        return true;
      }

      // superExecutionId
      if(superCaseExecutionId != null && superCaseExecutionId.equals(otherId)) {
        return true;
      }

    }
    return false;

  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("caseDefinitionId", caseDefinitionId);
    persistentState.put("businessKey", businessKey);
    persistentState.put("activityId", activityId);
    persistentState.put("parentId", parentId);
    persistentState.put("currentState", currentState);
    persistentState.put("previousState", previousState);
    return persistentState;
  }

  public CmmnModelInstance getCmmnModelInstance() {
    if(caseDefinitionId != null) {

      return Context.getProcessEngineConfiguration()
        .getDeploymentCache()
        .findCmmnModelInstanceForCaseDefinition(caseDefinitionId);

    } else {
      return null;

    }
  }

  public CmmnElement getCmmnModelElementInstance() {
    CmmnModelInstance cmmnModelInstance = getCmmnModelInstance();
    if(cmmnModelInstance != null) {

      ModelElementInstance modelElementInstance = cmmnModelInstance.getModelElementById(activityId);

      try {
        return (CmmnElement) modelElementInstance;

      } catch(ClassCastException e) {
        ModelElementType elementType = modelElementInstance.getElementType();
        throw new ProcessEngineException("Cannot cast "+modelElementInstance+" to CmmnElement. "
            + "Is of type "+elementType.getTypeName() + " Namespace "
            + elementType.getTypeNamespace(), e);
      }

    } else {
      return null;
    }
  }

  public ProcessEngineServices getProcessEngineServices() {
    return Context
        .getProcessEngineConfiguration()
        .getProcessEngine();
  }

  public <T extends CoreExecution> void performOperation(CoreAtomicOperation<T> operation) {
    Context.getCommandContext()
      .performOperation((CmmnAtomicOperation) operation, this);
  }

  public <T extends CoreExecution> void performOperationSync(CoreAtomicOperation<T> operation) {
    Context.getCommandContext()
      .performOperation((CmmnAtomicOperation) operation, this);
  }

}
