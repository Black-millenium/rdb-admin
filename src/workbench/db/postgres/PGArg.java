/*
 * PGArg.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2013, Thomas Kellerer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * To contact the author please send an email to: support@sql-workbench.net
 *
 */
package workbench.db.postgres;

/**
 * @author Thomas Kellerer
 */
class PGArg {
  public final PGType argType;

  ;
  public final ArgMode argMode;
  PGArg(PGType type, String mode) {
    argType = type;
    if ("inout".equalsIgnoreCase(mode)) {
      argMode = ArgMode.inout;
    } else if ("out".equalsIgnoreCase(mode)) {
      argMode = ArgMode.out;
    } else if ("return".equalsIgnoreCase(mode)) {
      argMode = ArgMode.returnValue;
    } else {
      argMode = ArgMode.in;
    }
  }

  static enum ArgMode {
    in,
    out,
    inout,
    returnValue;
  }

}
