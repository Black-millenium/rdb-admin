/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package jline;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;

/**
 * An {@link InputStream} implementation that wraps a {@link ConsoleReader}.
 * It is useful for setting up the {@link System#in} for a generic
 * console.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class ConsoleReaderInputStream extends SequenceInputStream {
  private static InputStream systemIn = System.in;

  public ConsoleReaderInputStream(final ConsoleReader reader) {
    super(new ConsoleEnumeration(reader));
  }

  public static void setIn() throws IOException {
    setIn(new ConsoleReader());
  }

  public static void setIn(final ConsoleReader reader) {
    System.setIn(new ConsoleReaderInputStream(reader));
  }

  /**
   * Restore the original {@link System#in} input stream.
   */
  public static void restoreIn() {
    System.setIn(systemIn);
  }

  private static class ConsoleEnumeration implements Enumeration {
    private final ConsoleReader reader;
    private ConsoleLineInputStream next = null;
    private ConsoleLineInputStream prev = null;

    public ConsoleEnumeration(final ConsoleReader reader) {
      this.reader = reader;
    }

    public Object nextElement() {
      if (next != null) {
        InputStream n = next;
        prev = next;
        next = null;

        return n;
      }

      return new ConsoleLineInputStream(reader);
    }

    public boolean hasMoreElements() {
      // the last line was null
      if ((prev != null) && (prev.wasNull == true)) {
        return false;
      }

      if (next == null) {
        next = (ConsoleLineInputStream) nextElement();
      }

      return next != null;
    }
  }

  private static class ConsoleLineInputStream extends InputStream {
    private final ConsoleReader reader;
    protected boolean wasNull = false;
    private String line = null;
    private int index = 0;
    private boolean eol = false;

    public ConsoleLineInputStream(final ConsoleReader reader) {
      this.reader = reader;
    }

    public int read() throws IOException {
      if (eol) {
        return -1;
      }

      if (line == null) {
        line = reader.readLine();
      }

      if (line == null) {
        wasNull = true;
        return -1;
      }

      if (index >= line.length()) {
        eol = true;
        return '\n'; // lines are ended with a newline
      }

      return line.charAt(index++);
    }
  }
}
