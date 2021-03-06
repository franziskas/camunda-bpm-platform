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

import java.io.Serializable;
import java.util.List;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentQueryImpl extends AbstractQuery<DeploymentQuery, Deployment> implements DeploymentQuery, Serializable {

  private static final long serialVersionUID = 1L;  
  protected String deploymentId;
  protected String name;
  protected String nameLike;

  public DeploymentQueryImpl() {
  }

  public DeploymentQueryImpl(CommandContext commandContext) {
    super(commandContext);
  }

  public DeploymentQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  public DeploymentQueryImpl deploymentId(String deploymentId) {
    ensureNotNull("Deployment id", deploymentId);
    this.deploymentId = deploymentId;
    return this;
  }
  
  public DeploymentQueryImpl deploymentName(String deploymentName) {
    ensureNotNull("deploymentName", deploymentName);
    this.name = deploymentName;
    return this;
  }

  public DeploymentQueryImpl deploymentNameLike(String nameLike) {
    ensureNotNull("deploymentNameLike", nameLike);
    this.nameLike = nameLike;
    return this;
  }
  
  //sorting ////////////////////////////////////////////////////////
  
  public DeploymentQuery orderByDeploymentId() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_ID);
  }
  
  public DeploymentQuery orderByDeploymenTime() {
    return orderBy(DeploymentQueryProperty.DEPLOY_TIME);
  }
  
  public DeploymentQuery orderByDeploymentName() {
    return orderBy(DeploymentQueryProperty.DEPLOYMENT_NAME);
  }
  
  //results ////////////////////////////////////////////////////////
  
  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentCountByQueryCriteria(this);
  }

  @Override
  public List<Deployment> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    return commandContext
      .getDeploymentManager()
      .findDeploymentsByQueryCriteria(this, page);
  }
  
  //getters ////////////////////////////////////////////////////////
  
  public String getDeploymentId() {
    return deploymentId;
  }
  
  public String getName() {
    return name;
  }

  public String getNameLike() {
    return nameLike;
  }
}
