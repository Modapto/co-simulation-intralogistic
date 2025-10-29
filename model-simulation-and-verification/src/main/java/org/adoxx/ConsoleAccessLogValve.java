package org.adoxx;

import org.apache.catalina.valves.AccessLogValve;
import java.io.CharArrayWriter;

public class ConsoleAccessLogValve extends AccessLogValve {
    @Override
    public void log(CharArrayWriter message) {
        System.out.println(message.toString());
    }
}
