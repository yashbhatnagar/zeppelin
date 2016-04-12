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
package org.apache.zeppelin.helium;

import org.apache.zeppelin.interpreter.InterpreterContext;
import org.apache.zeppelin.resource.ResourceSet;

/**
 * Zeppelin Application base
 */
public abstract class Application {
  private final ResourceSet args;
  private final ApplicationContext context;

  public Application(ResourceSet args, ApplicationContext context) throws ApplicationException {
    this.args = args;
    this.context = context;
  }

  public ResourceSet args() {
    return args;
  }

  public ApplicationContext context() {
    return context;
  }

  /**
   * This method can be invoked multiple times before unload(),
   * Either just after application selected or when paragraph re-run after application load
   */
  public abstract void run() throws ApplicationException;


  /**
   * this method is invoked just before application is removed
   */
  public abstract void unload() throws ApplicationException;
}