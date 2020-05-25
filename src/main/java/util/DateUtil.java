package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String getCurrentSimpleTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss ");
        return format.format(new Date());
    }
}
