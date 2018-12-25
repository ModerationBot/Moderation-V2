package me.just.moderation;


import java.awt.*;
import java.time.OffsetDateTime;

public class Constants {

    public static final OffsetDateTime STARTUP = OffsetDateTime.now();
    public static String HELPMESSAGEREACTION = "";
    public static final String SUCCESS = "\u2705";
    public static final String LOADING = ":warning:";
    public static final String ERROR = ":x:";
    public static final String OWNER    = "**Just**#0001";
    public static final String VERSION  = "1.0";
    public static final String OWNERID  = "327129699904126976";
    public static final Color SUCCESSCOLOR = new Color(0, 155, 0);
    public static final Color ERRORCOLOR = new Color(155, 0, 0);
    public static final String PREFIX = "-";
    public static final String SERVERID = "";

    public static String MYSQLHOST = "localhost";
    public static String MYSQLPORT = "3306";
    public static String MYSQLUSERNAME = "ModerationBot";
    public static String MYSQLPASSWORD = "ModerationBot";
    public static String MYSQLDATABASE = "ModerationBot";
}
