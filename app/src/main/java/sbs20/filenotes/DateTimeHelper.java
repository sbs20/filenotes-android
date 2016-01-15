package sbs20.filenotes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeHelper {

    private FilenotesApplication application;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    public DateTimeHelper(FilenotesApplication application) {
        this.application = application;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat)android.text.format.DateFormat
                .getDateFormat(application.getBaseContext());

        SimpleDateFormat simpleTimeFormat = (SimpleDateFormat)android.text.format.DateFormat
                .getTimeFormat(application.getBaseContext());

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
