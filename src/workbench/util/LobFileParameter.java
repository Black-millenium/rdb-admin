/*
 * LobFileParameter.java
 *
 * This file is part of SQL Workbench/J, http://www.sql-workbench.net
 *
 * Copyright 2002-2015, Thomas Kellerer
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
package workbench.util;

import java.io.Closeable;

/**
 * @author Thomas Kellerer
 */
public class LobFileParameter {
  private Closeable dataStream;
  private String filename;
  private String encoding;
  private boolean binary;

  public LobFileParameter() {
  }

  public LobFileParameter(String fname, String enc, boolean isBinary) {
    setFilename(fname);
    setEncoding(enc);
    setBinary(isBinary);
  }

  @Override
  public String toString() {
    return "filename=[" + filename + "], binary=" + binary + ", encoding=" + encoding;
  }

  public void setDataStream(Closeable in) {
    this.dataStream = in;
  }

  public void close() {
    FileUtil.closeQuietely(dataStream);
  }

  public boolean isBinary() {
    return binary;
  }

  public final void setBinary(boolean flag) {
    binary = flag;
  }

  public String getFilename() {
    return filename;
  }

  public final void setFilename(String fname) {
    filename = fname;
  }

  public String getEncoding() {
    return encoding;
  }

  public final void setEncoding(String enc) {
    encoding = enc;
  }
}
