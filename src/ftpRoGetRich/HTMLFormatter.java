/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ftpRoGetRich;

import java.util.Date;
import java.util.logging.*;
import java.util.logging.LogRecord;

/**
 *
 * @author koalacurioso
 */
public class HTMLFormatter extends Formatter {
        // This method is called for every log records
        public String format(LogRecord rec) {
            StringBuffer buf = new StringBuffer(1000);
            // Bold any levels >= WARNING
            if (rec.getLevel().intValue() >= Level.WARNING.intValue()) {
                buf.append("<b>");
                buf.append(rec.getLevel());
                buf.append("</b>");
            } else {
                buf.append(rec.getLevel());
            }
            buf.append(' ');
            buf.append(rec.getMillis());
            buf.append(' ');
            buf.append(formatMessage(rec));
            buf.append('\n');
            return buf.toString();
        }
    
        // This method is called just after the handler using this
        // formatter is created
        public String getHead(Handler h) {
            return "<HTML><HEAD>"+(new Date())+"</HEAD><BODY><PRE>\n";
        }
    
        // This method is called just after the handler using this
        // formatter is closed
        public String getTail(Handler h) {
            return "</PRE></BODY></HTML>\n";
        }
}