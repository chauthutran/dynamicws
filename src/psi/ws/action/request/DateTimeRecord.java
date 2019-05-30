package psi.ws.action.request;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

public class DateTimeRecord {

        DateTime startDT;
        DateTime endDT;
        String name = "";
        
        // ===================================

        public DateTimeRecord()
        {
                setStartDT();
        }
        
        public DateTimeRecord( String name )
        {
                this();
                this.name = name;
        }
        
        // ===================================
        
        public String getName()
        {
                return this.name;
        }
        
        public void setName( String nameInput )
        {
                this.name = nameInput;
        }
        
        public void appendToName( String inputStr )
        {
                this.name += inputStr;
        }       
        
        public void setStartDT( DateTime startDT )
        {
                this.startDT = startDT;
        }
        
        public void setEndDT( DateTime endDT )
        {
                this.endDT = endDT;
        }

        public void setStartDT()
        {
                this.startDT = DateTime.now();
        }

        public void setEndDT()
        {
                this.endDT = DateTime.now();
        }

        // - - - - - - - - - - 
        
        public String getRunTime( DateTime startDT, DateTime endDT )
        {
                String output = "";
                
                try
                {
                        long diffTime = endDT.getMillis() - startDT.getMillis();
                        double div = 1000.0;
                        double diffTimeF = ( diffTime / div );
                        
                        output = String.format( "%.1f", diffTimeF ) + "s";
                }
                catch(Exception ex)
                {
//                        Util.output( "ERROR - on getRunTime: "+ ex.getMessage() );
                }
                
                return output;
        }
        
        public String getRunTime()
        {
                return getRunTime( this.startDT, this.endDT );          
        }

        public String getRunTimeAfterEndingIt()
        {
                return getRunTime( this.startDT, DateTime.now() );              
        }
        
        public void addTimeMark( JSONObject dtRecordJson )
        {
                if ( !this.name.isEmpty() && dtRecordJson != null )
                {
                        dtRecordJson.put(  this.name, getRunTimeAfterEndingIt() );
                }       
        }

        public void addTimeMark_WtCount( JSONObject dtRecordJson )
        {
                if ( !this.name.isEmpty() && dtRecordJson != null )
                {                       
                        dtRecordJson.put( dtRecordJson.length() + ". " + this.name, getRunTimeAfterEndingIt() );
                }       
        }

}
