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
package org.apache.zeppelin.interpreter.dev;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.zeppelin.helium.*;
import org.apache.zeppelin.interpreter.*;
import org.apache.zeppelin.interpreter.InterpreterResult.Code;
import org.apache.zeppelin.interpreter.remote.RemoteInterpreterEventClient;
import org.apache.zeppelin.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run this server for development mode.
 */
public class ZeppelinApplicationDevServer extends ZeppelinDevServer {
  final Logger logger = LoggerFactory.getLogger(ZeppelinApplicationDevServer.class);

  private final String className;
  private final ResourceSet resourceSet;
  private Application app;
  private InterpreterOutput out;

  public ZeppelinApplicationDevServer(final String className, ResourceSet resourceSet) throws
      Exception {
    this(ZeppelinDevServer.DEFAULT_TEST_INTERPRETER_PORT, className, resourceSet);
  }

  public ZeppelinApplicationDevServer(int port, String className, ResourceSet resourceSet) throws
      Exception {
    super(port);
    this.className = className;
    this.resourceSet = resourceSet;
    setLogger();
  };

  void setLogger() {
    ConsoleAppender console = new ConsoleAppender(); //create appender
    //configure the appender
    String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    console.setLayout(new PatternLayout(PATTERN));
    console.setThreshold(Level.DEBUG);
    console.activateOptions();
    //add appender to any Logger (here is root)
    org.apache.log4j.Logger.getRootLogger().addAppender(console);
  }


  @Override
  public InterpreterResult interpret(String st, InterpreterContext context) {
    if (app == null) {
      logger.info("Create instance " + className);
      try {
        Class<?> appClass = ClassLoader.getSystemClassLoader().loadClass(className);
        Constructor<?> constructor = appClass.getConstructor(
            ResourceSet.class, ApplicationContext.class);
        app = (Application) constructor.newInstance(resourceSet, getApplicationContext(context));
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        return new InterpreterResult(Code.ERROR, e.getMessage());
      }
    }

    try {
      logger.info("Run " + className);
      app.context().out.setType(InterpreterResult.Type.ANGULAR);
      app.run();
    } catch (ApplicationException e) {
      logger.error(e.getMessage(), e);
      return new InterpreterResult(Code.ERROR, e.getMessage());
    }
    return new InterpreterResult(Code.SUCCESS, "");
  }

  ApplicationContext getApplicationContext(InterpreterContext interpreterContext) {
    return new ApplicationContext(
        interpreterContext.getNoteId(),
        interpreterContext.getParagraphId(),
        new HeliumAppAngularObjectRegistry(
            interpreterContext.getAngularObjectRegistry(),
            interpreterContext.getNoteId(),
            interpreterContext.getParagraphId()),
        interpreterContext.out);
  }

  @Override
  protected InterpreterOutput createInterpreterOutput(
      final String noteId, final String paragraphId) {
    if (out == null) {
      final RemoteInterpreterEventClient eventClient = getEventClient();
      try {
        out = new InterpreterOutput(new InterpreterOutputListener() {
          @Override
          public void onAppend(InterpreterOutput out, byte[] line) {
            eventClient.onInterpreterOutputAppend(noteId, paragraphId, new String(line));
          }

          @Override
          public void onUpdate(InterpreterOutput out, byte[] output) {
            eventClient.onInterpreterOutputUpdate(noteId, paragraphId, new String(output));
          }
        }, this);
      } catch (IOException e) {
        return null;
      }
    }

    return out;
  }
}
