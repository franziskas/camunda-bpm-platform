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
package org.camunda.bpm.engine.rest.dto.history;

import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;

/**
 * @author Danny Gräf
 */
public class UserOperationLogQueryDto extends AbstractQueryDto<UserOperationLogQuery> {

  public static final String TIMESTAMP = "timestamp";

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processInstanceId;
  protected String executionId;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String taskId;
  protected String userId;
  protected String operationId;
  protected String operationType;
  protected String entityType;
  protected String property;
  protected Date afterTimestamp;
  protected Date beforeTimestamp;

  public UserOperationLogQueryDto(MultivaluedMap<String, String> queryParameters) {
    super(queryParameters);
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return TIMESTAMP.equals(value);
  }

  @Override
  protected UserOperationLogQuery createNewQuery(ProcessEngine engine) {
    return engine.getHistoryService().createUserOperationLogQuery();
  }

  @Override
  protected void applyFilters(UserOperationLogQuery query) {
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (taskId != null) {
      query.taskId(taskId);
    }
    if (userId != null) {
      query.userId(userId);
    }
    if (operationId != null) {
      query.operationId(operationId);
    }
    if (operationType != null) {
      query.operationType(operationType);
    }
    if (entityType != null) {
      query.entityType(entityType);
    }
    if (property != null) {
      query.property(property);
    }
    if (afterTimestamp != null) {
      query.afterTimestamp(afterTimestamp);
    }
    if (beforeTimestamp != null) {
      query.beforeTimestamp(beforeTimestamp);
    }
  }

  @Override
  protected void applySortingOptions(UserOperationLogQuery query) {
    if (TIMESTAMP.equals(sortBy)) {
      query.orderByTimestamp();
    }
    if (SORT_ORDER_ASC_VALUE.equals(sortOrder)) {
      query.asc();
    }
    if (SORT_ORDER_DESC_VALUE.equals(sortOrder)) {
      query.desc();
    }
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam("taskId")
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  @CamundaQueryParam("userId")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  @CamundaQueryParam("operationId")
  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  @CamundaQueryParam("operationType")
  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  @CamundaQueryParam("entityType")
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  @CamundaQueryParam("property")
  public void setProperty(String property) {
    this.property = property;
  }

  @CamundaQueryParam(value = "afterTimestamp", converter = DateConverter.class)
  public void setAfterTimestamp(Date after) {
    this.afterTimestamp = after;
  }

  @CamundaQueryParam(value = "beforeTimestamp", converter = DateConverter.class)
  public void setBeforeTimestamp(Date before) {
    this.beforeTimestamp = before;
  }
}
