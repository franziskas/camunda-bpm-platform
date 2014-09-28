package org.camunda.bpm.engine.test.variables;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.delegate.SerializedObjectVariableValue;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.AbstractProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.variable.VariableType;
import org.camunda.spin.DataFormats;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

public class HistoricVariableDataFormatTest extends AbstractProcessEngineTestCase {

  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/variables/oneTaskProcess.bpmn20.xml";

  protected static final String JSON_FORMAT_NAME = DataFormats.jsonTree().getName();

  @Override
  protected void initializeProcessEngine() {
    ProcessEngineConfigurationImpl engineConfig =
        (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");

    engineConfig.setDefaultSerializationFormat(JSON_FORMAT_NAME);

    processEngine = engineConfig.buildProcessEngine();
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSelectHistoricVariableInstances() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertNotNull(historicVariable.getTypedValue());
    assertNull(historicVariable.getErrorMessage());

    assertTrue(historicVariable.storesCustomObjects());
    assertEquals(Object.class.getSimpleName(), historicVariable.getValueTypeName());

    SimpleBean historyValue = (SimpleBean) historicVariable.getTypedValue();
    assertEquals(bean.getStringProperty(), historyValue.getStringProperty());
    assertEquals(bean.getIntProperty(), historyValue.getIntProperty());
    assertEquals(bean.getBooleanProperty(), historyValue.getBooleanProperty());
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSelectHistoricSerializedValues() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    SerializedObjectVariableValue historicValue =
        historyService.createHistoricVariableInstanceQuery().singleResult().getSerializedValue();
    assertNotNull(historicValue);

    Map<String, Object> config = historicValue.getConfig();
    assertEquals(2, config.size());
    assertEquals(JSON_FORMAT_NAME, config.get(VariableType.SPIN_TYPE_DATA_FORMAT_ID));
    assertEquals(bean.getClass().getCanonicalName(), config.get(VariableType.SPIN_TYPE_CONFIG_ROOT_TYPE));

    String variableAsJson = (String) historicValue.getTypedValue();
    JSONAssert.assertEquals(bean.toExpectedJsonString(), variableAsJson, true);
  }

  @Deployment(resources = ONE_TASK_PROCESS)
  public void testSelectHistoricSerializedValuesUpdate() throws JSONException {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    SimpleBean bean = new SimpleBean("a String", 42, false);
    runtimeService.setVariable(instance.getId(), "simpleBean", bean);

    if (ProcessEngineConfiguration.HISTORY_FULL.equals(processEngineConfiguration.getHistory())) {

      HistoricVariableUpdate historicUpdate = (HistoricVariableUpdate)
          historyService.createHistoricDetailQuery().variableUpdates().singleResult();

      assertTrue(historicUpdate.storesCustomObjects());
      assertEquals(Object.class.getSimpleName(), historicUpdate.getValueTypeName());

      SerializedObjectVariableValue serializedValue = historicUpdate.getSerializedValue();
      assertNotNull(serializedValue);

      Map<String, Object> config = serializedValue.getConfig();
      assertEquals(2, config.size());
      assertEquals(JSON_FORMAT_NAME, config.get(VariableType.SPIN_TYPE_DATA_FORMAT_ID));
      assertEquals(bean.getClass().getCanonicalName(), config.get(VariableType.SPIN_TYPE_CONFIG_ROOT_TYPE));

      String variableAsJson = (String) serializedValue.getTypedValue();
      JSONAssert.assertEquals(bean.toExpectedJsonString(), variableAsJson, true);
    }


  }



}
