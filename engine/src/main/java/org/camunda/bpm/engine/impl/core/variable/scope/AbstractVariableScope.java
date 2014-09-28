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
package org.camunda.bpm.engine.impl.core.variable.scope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.javax.el.ELContext;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * @author Daniel Meyer
 * @author Roman Smirnov
 * @author Sebastian Menski
 *
 */
public abstract class AbstractVariableScope implements Serializable, VariableScope {

  private static final long serialVersionUID = 1L;

  // TODO: move this?
  protected ELContext cachedElContext;

  protected abstract CoreVariableStore getVariableStore();

  public abstract AbstractVariableScope getParentVariableScope();

  public VariableMapImpl getVariables() {
    VariableMapImpl variableMap = new VariableMapImpl();
    collectVariables(variableMap, null);
    return variableMap;
  }

  /**
   * If parameter 'variableNames' is null, all variables are collected.
   *
   * @param resultVariables
   * @param variableNames
   */
  protected void collectVariables(VariableMapImpl resultVariables, Collection<String> variableNames) {
    VariableMapImpl localVariables = getVariablesLocal();
    for (String varName : localVariables.keySet()) {
      if(!resultVariables.containsKey(varName)
         && (variableNames == null || variableNames.contains(varName))) {
        resultVariables.put(varName, localVariables.getTypedValue(varName));
      }
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    // Do not propagate to parent if all variables in 'variableNames' are already collected!
    if(parentScope != null && (variableNames == null || !resultVariables.keySet().equals(variableNames))) {
      parentScope.collectVariables(resultVariables, variableNames);
    }
  }

  public Object getVariable(String variableName) {
    CoreVariableInstance variableInstance = getVariableInstance(variableName);
    if (variableInstance != null && variableInstance.getTypedValue() != null) {
      return variableInstance.getTypedValue().getValue();
    }
    else {
      return null;
    }
  }

  public Object getVariableLocal(String variableName) {
    CoreVariableInstance variableInstance = getVariableInstanceLocal(variableName);
    if (variableInstance != null && variableInstance.getTypedValue() != null) {
      return variableInstance.getTypedValue();
    }
    else {
      return null;
    }
  }

  public CoreVariableInstance getVariableInstance(String variableName) {
    CoreVariableInstance variableInstance = getVariableInstanceLocal(variableName);
    if (variableInstance!=null) {
      return variableInstance;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      return parentScope.getVariableInstance(variableName);
    }
    return null;
  }

  public CoreVariableInstance getVariableInstanceLocal(String name) {
    return getVariableStore().getVariableInstance(name);
  }

  public Map<String, CoreVariableInstance> getVariableInstancesLocal() {
    return getVariableStore().getVariableInstances();
  }

  public boolean hasVariables() {
    if (!getVariableStore().isEmpty()) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariables();
  }

  public boolean hasVariablesLocal() {
    return !getVariableStore().isEmpty();
  }

  public boolean hasVariable(String variableName) {
    if (hasVariableLocal(variableName)) {
      return true;
    }
    AbstractVariableScope parentScope = getParentVariableScope();
    return parentScope != null && parentScope.hasVariable(variableName);
  }

  public boolean hasVariableLocal(String variableName) {
    return getVariableStore().containsVariableInstance(variableName);
  }

  protected Set<String> collectVariableNames(Set<String> variableNames) {
    AbstractVariableScope parentScope = getParentVariableScope();
    if (parentScope!=null) {
      variableNames.addAll(parentScope.collectVariableNames(variableNames));
    }
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variableNames.add(variableInstance.getName());
    }
    return variableNames;
  }

  public Set<String> getVariableNames() {
    return collectVariableNames(new HashSet<String>());
  }

  public VariableMapImpl getVariablesLocal() {
    VariableMapImpl variables = new VariableMapImpl();
    for (CoreVariableInstance variableInstance: getVariableStore().getVariableInstancesValues()) {
      variables.put(variableInstance.getName(), variableInstance.getTypedValue());
    }
    return variables;
  }

  public Set<String> getVariableNamesLocal() {
    return getVariableStore().getVariableNames();
  }

  public void setVariables(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariable(variableName, variables.get(variableName));
      }
    }
  }

  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    if (variables!=null) {
      for (String variableName : variables.keySet()) {
        setVariableLocal(variableName, variables.get(variableName));
      }
    }
  }

  public void removeVariables() {
    Set<String> variableNames = new HashSet<String>(getVariableStore().getVariableNames());
    for (String variableName: variableNames) {
      removeVariable(variableName);
    }
  }

  public void removeVariablesLocal() {
    List<String> variableNames = new ArrayList<String>(getVariableNamesLocal());
    for (String variableName: variableNames) {
      removeVariableLocal(variableName);
    }
  }

  public void removeVariables(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariable(variableName);
      }
    }
  }

  public void removeVariablesLocal(Collection<String> variableNames) {
    if (variableNames != null) {
      for (String variableName : variableNames) {
        removeVariableLocal(variableName);
      }
    }
  }

  public void setVariable(String variableName, Object value) {
    VariableValue variableValue = Variables.untypedValue(value);
    setVariable(variableName, variableValue, getSourceActivityVariableScope());
  }

  protected void setVariable(String variableName, VariableValue value, AbstractVariableScope sourceActivityVariableScope) {
    if (hasVariableLocal(variableName)) {
      setVariableLocal(variableName, value, sourceActivityVariableScope);
      return;
    }
    AbstractVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityVariableScope==null) {
        parentVariableScope.setVariable(variableName, value);
      } else {
        parentVariableScope.setVariable(variableName, value, sourceActivityVariableScope);
      }
      return;
    }
    setVariableLocal(variableName, value, sourceActivityVariableScope);
  }

  public void setVariableLocal(String variableName, VariableValue value, AbstractVariableScope sourceActivityExecution) {
    getVariableStore().createOrUpdateVariable(variableName, value, sourceActivityExecution);
  }

  public void setVariableLocal(String variableName, Object value) {
    VariableValue variableValue = Variables.untypedValue(value);
    getVariableStore().createOrUpdateVariable(variableName, variableValue, getSourceActivityVariableScope());

  }

  public void removeVariable(String variableName) {
    removeVariable(variableName, getSourceActivityVariableScope());
  }

  protected void removeVariable(String variableName, AbstractVariableScope sourceActivityExecution) {
    if (getVariableStore().containsVariableInstance(variableName)) {
      removeVariableLocal(variableName);
      return;
    }
    AbstractVariableScope parentVariableScope = getParentVariableScope();
    if (parentVariableScope!=null) {
      if (sourceActivityExecution==null) {
        parentVariableScope.removeVariable(variableName);
      } else {
        parentVariableScope.removeVariable(variableName, sourceActivityExecution);
      }
    }
  }

  public void removeVariableLocal(String variableName) {
    removeVariableLocal(variableName, getSourceActivityVariableScope());
  }

  protected AbstractVariableScope getSourceActivityVariableScope() {
    return this;
  }

  protected void removeVariableLocal(String variableName, AbstractVariableScope sourceActivityExecution) {
    getVariableStore().removeVariableInstance(variableName, sourceActivityExecution);
  }

  public ELContext getCachedElContext() {
    return cachedElContext;
  }
  public void setCachedElContext(ELContext cachedElContext) {
    this.cachedElContext = cachedElContext;
  }


}
