package sbs20.filenotes;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeHelper {

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public DateTimeHelper() {
        Context context = ServiceManager.getInstance().getContext();
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat)android.text.format.DateFormat
                .getDateFormat(context);

        SimpleDateFormat simpleTimeFormat = (SimpleDateFormat)android.text.format.DateFormat
                .getTimeFormat(context);

        this.dateFormat = new SimpleDateFormat(simpleDateFormat.toLocalizedPattern());
        this.timeFormat = new SimpleDateFormat(simpleTimeFormat.toLocalizedPattern());
    }

    public String formatDate(Date date) {
        return this.dateFormat.format(date);
    }

    public String formatTime(Date date) {
        return this.timeFormat.format(date);
    }
}
