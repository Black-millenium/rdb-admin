/*
 * MemoryWatcher.java
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

import workbench.resource.Settings;

/**
 * @author Thomas Kellerer
 */
public class MemoryWatcher {
  // the maxMemory() will not change during the lifetime of the JVM
  // so I can spare some CPU cycles by not calling maxMemory() constantly
  public static final long MAX_MEMORY = Runtime.getRuntime().maxMemory();
  private static final long MIN_FREE_MEMORY = Settings.getInstance().getIntProperty("workbench.memorywatcher.minmemory", 10) * 1024 * 1024;

  public synchronized static boolean isMemoryLow(boolean doGC) {
    long free = getFreeMemory();
    if (free < MIN_FREE_MEMORY && doGC) {
      // As we have not yet hit an OutOfMemoryException, running gc()
      // can actually free some memory. This will slow down e.g.
      // data retrieval when the memory is filled up, but at least
      // we can prevent the OOME to a certain extent
      System.gc();
      free = Runtime.getRuntime().freeMemory();
    }
    return (free < MIN_FREE_MEMORY);
  }

  public static long getFreeMemory() {
    long free = Runtime.getRuntime().freeMemory();
    long total = Runtime.getRuntime().totalMemory();

    // freeMemory reports the amount of memory that is free
    // in the totalMemory. But the total memory can actually
    // expand to maxMemory. So we need to add the difference
    // between max and total to the currently free memory
    return free + (MAX_MEMORY - total);
  }
}
