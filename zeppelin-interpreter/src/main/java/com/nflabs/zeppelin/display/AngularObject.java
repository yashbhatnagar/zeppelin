/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nflabs.zeppelin.display;

import java.util.Map;

/**
 *
 *
 * @param <T>
 */
public class AngularObject<T> {

  /**
   *
   */
  public static enum AngularObjectType {
    STRING,
    MAP,
    FUNCTION
  };

  private String name;
  private T object;
  private transient AngularObjectListener listener;
  private AngularObjectType type;

  protected AngularObject(String name, T o,
      AngularObjectListener listener) {
    this.name = name;
    this.listener = listener;
    object = o;
    type = checkType();
  }

  public AngularObjectType getType() {
    return type;
  }

  public AngularObjectType checkType() {
    if (object == null) {
      return AngularObjectType.STRING;
    } else if (object instanceof Map) {
      return AngularObjectType.MAP;
    } else if (object instanceof String) {
      return AngularObjectType.STRING;
    }
    return AngularObjectType.STRING;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof AngularObject) {
      return name.equals(((AngularObject) o).name);
    } else {
      return false;
    }
  }

  public Object get() {
    return object;
  }

  public void emit(){
    if (listener != null) {
      listener.updated(this);
    }
  }

  public void set(T o) {
    set(o, true);
  }

  public void set(T o, boolean emit) {
    object = o;
    if (emit) {
      emit();
    }
  }

  public void setListener(AngularObjectListener listener) {
    this.listener = listener;
  }

  public AngularObjectListener getListener() {
    return listener;
  }
}
