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
package org.camunda.bpm.engine.impl.variable.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.variable.value.ObjectVariableValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class JavaObjectSerializer extends AbstractObjectSerializer {

  public static final String NAME = "serializable";
  public static final String SERIALIZATION_DATA_FORMAT = "application/x-java-serialized-object";

  public JavaObjectSerializer() {
    super(SERIALIZATION_DATA_FORMAT);
  }

  public String getName() {
    return NAME;
  }

  protected Object getObject(byte[] bytes, ValueFields valueFields) {

    Object deserializedObject = null;

    if(bytes != null) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = null;
      try {

        ois = new ClassloaderAwareObjectInputStream(bais);
        deserializedObject = ois.readObject();

        if (valueFields instanceof VariableInstanceEntity) {
          if (Context.getCommandContext() == null) {
            throw new ProcessEngineException("Unable to deserizable variable instance outside of a command context");
          }

          Context
            .getCommandContext()
            .getSession(DeserializedObjectsSession.class)
            .addDeserializedObject(deserializedObject, bytes, (VariableInstanceEntity) valueFields);
        }

      } catch (Exception e) {
        throw new ProcessEngineException("Couldn't deserialize object in variable '"+valueFields.getName()+"'", e);

      } finally {
        IoUtil.closeSilently(ois);
        IoUtil.closeSilently(bais);
      }
    }

    return createTypedValue(deserializedObject);
  }

  protected byte[] getBytes(ObjectVariableValue value, ValueFields valueFields) {
    byte[] byteArray = null;

    if(value != null) {
      byteArray = serialize(value, valueFields);
    }

    if(valueFields.getByteArrayValue() == null) {
      if(valueFields instanceof VariableInstanceEntity) {
        Context
          .getCommandContext()
          .getSession(DeserializedObjectsSession.class)
          .addDeserializedObject(value.getValue(), byteArray, (VariableInstanceEntity)valueFields);
      }
    }

    return byteArray;
  }

  protected boolean canSerializeObject(Object value) {
    return value instanceof Serializable;
  }

  public static byte[] serialize(Object value, ValueFields valueFields) {
    if(value == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream ois = null;
    try {
      ois = new ObjectOutputStream(baos);
      ois.writeObject(value);
    } catch (Exception e) {
      throw new ProcessEngineException("Couldn't serialize value '"+value+"' in variable '"+valueFields.getName()+"'", e);
    } finally {
      IoUtil.closeSilently(ois);
    }
    return baos.toByteArray();
  }

  protected static class ClassloaderAwareObjectInputStream extends ObjectInputStream {

    public ClassloaderAwareObjectInputStream(InputStream in) throws IOException {
      super(in);
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      return ReflectUtil.loadClass(desc.getName());
    }

  }
}
