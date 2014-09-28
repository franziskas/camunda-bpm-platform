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
package org.camunda.bpm.engine.impl.core.variable;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.VariableValue;

/**
 * @author Daniel Meyer
 *
 */
public class VariableMapImpl implements VariableMap, Serializable {

  private static final long serialVersionUID = 1L;

  protected Map<String, VariableValue> variables = new HashMap<String, VariableValue>();

  public VariableMapImpl(Map<String, Object> map) {
    putAll(map);
  }

  public VariableMapImpl() {
  }

  // VariableMap implementation //////////////////////////////

  public VariableMap variable(String name, Object value) {
    put(name, value);
    return this;
  }

  public VariableMap variable(String name, VariableValue value) {
    variables.put(name, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name, Class<T> type) {
    Object object = get(name);
    if(object == null) {
      return null;
    }
    else if (type.isAssignableFrom(object.getClass())) {
      return (T) object;

    } else {
      throw new ClassCastException("Cannot cast variable named '"+name+"' with value '"+object+"' to type '"+type+"'.");
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends VariableValue> T getTypedValue(String name) {
    return (T) variables.get(name);
  }

  // java.uitil Map<String, Object> implementation ////////////////////////////////////////

  public int size() {
    return variables.size();
  }

  public boolean isEmpty() {
    return variables.isEmpty();
  }

  public boolean containsKey(Object key) {
    return variables.containsKey(key);
  }

  public boolean containsValue(Object value) {
    for (VariableValue varValue : variables.values()) {
      if(value == varValue.getValue()) {
        return true;
      } else if(value != null && value.equals(varValue.getValue())) {
        return true;
      }
    }
    return false;
  }

  public Object get(Object key) {
    VariableValue variableValue = variables.get(key);

    if(variableValue != null) {
      return variableValue.getValue();
    }
    else {
      return null;
    }
  }

  public Object put(String key, Object value) {

    VariableValue variableValue = Variables.untypedValue(value);

    VariableValue prevValue = variables.put(key, variableValue);

    if(prevValue != null) {
      return prevValue.getValue();
    }
    else {
      return null;
    }
  }

  public Object remove(Object key) {
    VariableValue prevValue = variables.remove(key);

    if(prevValue != null) {
      return prevValue.getValue();
    }
    else {
      return null;
    }
  }

  public void putAll(Map<? extends String, ? extends Object> m) {
    for (java.util.Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public void clear() {
    variables.clear();
  }

  public Set<String> keySet() {
    return variables.keySet();
  }

  public Collection<Object> values() {

    // NOTE: cannot naively return List of values here. A proper implementation must return a
    // Collection which is backed by the actual variable map to allow modifications through
    // iterator.remove() etc.

    return new AbstractCollection<Object>() {

      public Iterator<Object> iterator() {

        // wrapped iterator. Must be local to the iterator() method
        final Iterator<java.util.Map.Entry<String, VariableValue>> iterator = variables.entrySet().iterator();

        return new Iterator<Object>() {
          public boolean hasNext() {
            return iterator.hasNext();
          }
          public Object next() {
            return iterator.next().getValue();
          }
          public void remove() {
            iterator.remove();
          }
        };
      }

      public int size() {
        return variables.size();
      }

    };
  }

  public Set<java.util.Map.Entry<String, Object>> entrySet() {

    // NOTE: cannot naively return Set of entries here. A proper implementation must
    // return a Set which is backed by the actual variable map to allow modifications
    // through iterator.remove() etc.

    return new AbstractSet<Map.Entry<String,Object>>() {

      public Iterator<java.util.Map.Entry<String, Object>> iterator() {

        return new Iterator<Map.Entry<String,Object>>() {

          // wrapped iterator. Must be local to the iterator() method
          final Iterator<java.util.Map.Entry<String, VariableValue>> iterator = variables.entrySet().iterator();

          public boolean hasNext() {
            return iterator.hasNext();
          }

          public java.util.Map.Entry<String, Object> next() {

            final java.util.Map.Entry<String, VariableValue> underlyingEntry = iterator.next();

            // return wrapper backed by the underlying entry
            return new Entry<String, Object>() {
              public String getKey() {
                return underlyingEntry.getKey();
              }
              public Object getValue() {
                return underlyingEntry.getValue().getValue();
              }
              public Object setValue(Object value) {
                VariableValue variableValue = Variables.untypedValue(value);
                return underlyingEntry.setValue(variableValue);
              }
            };
          }

          public void remove() {
            iterator.remove();
          }
        };
      }

      public int size() {
        return variables.size();
      }

    };
  }

}
