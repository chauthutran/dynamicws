package psi.ws.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateUtil
{
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd hh:mm:ss";
    
    public static Date convertDate( String dateStr, String dateFormatStr ) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat( dateFormatStr ); 
        Date date = formatter.parse( dateStr );
        
        return date;
    }
    
    public static Date convertDate( String dateStr ) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat( DateUtil.DEFAULT_DATE_FORMAT ); 
        Date date = formatter.parse( dateStr );
        
        return date;
    }
    
    public static String getCurrentDateTime()
    {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat( DateUtil.DEFAULT_DATE_TIME_FORMAT ); 
        return formatter.format( now );
    }
    
}


