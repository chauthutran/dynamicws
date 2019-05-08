package psi.ws.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.action.Action;
import psi.ws.action.ActionOutput;
import psi.ws.action.request.ActionWebServiceRequest;
import psi.ws.exception.ActionException;


public final class Util
{
    public static final String ENCODING_UTF8 = "UTF-8";
    public static final int REQUEST_TIMEOUT = 240000; // 4 min;
    
    public static final String CONFIG_FILE_COMMON_DATE = "config.json";
    public static final String CONFIG_FILE_ACTION_DATA = "config_actions.json";
    public static final String UTIL_JAVASCRIPT_LIB_FILE = "js/util.js";

    public static final String ACTION_ID_PAYLOAD = "PAYLOAD";
    
    
    public static final String REQUEST_CONTENT_TYPE_DHIS = "DHIS";
    

    public static final String REQUEST_TYPE_GET = "GET";
    public static final String REQUEST_TYPE_POST = "POST";
    public static final String REQUEST_TYPE_PUT = "PUT";
    public static final String REQUEST_TYPE_DELETE = "DELETE";

    // -------------------------------------------------------------------------
    // DHIS RELATED 
    // -------------------------------------------------------------------------
    
    // HTTPS GET/POST/PUT request
    public static void sendRequest( Action action ) throws ActionException
    {
        ActionWebServiceRequest request = ( ActionWebServiceRequest )action.getRequest();
        String url = request.getUrl();
        String sourceType = action.getType();
        String requestType = request.getRequestType();
        ActionOutput output = action.getOutput();
        
        try
        {
            System.out.println("\n === RequestURL : " + url );
            // Open HttpsURLConnection and Set Request Type.
            URL obj = new URL( url );
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // add Request header
            con.setRequestMethod( requestType );

            con.setRequestProperty( "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" );
            con.setRequestProperty( "Accept-Language", "en-US,en;q=0.5" );

            // Timeout <-- set to 180 sec / 3 min..
            con.setConnectTimeout( Util.REQUEST_TIMEOUT );
            con.setReadTimeout( Util.REQUEST_TIMEOUT );

            if ( sourceType.equals( Action.ACTION_TYPE_DHIS ) )
            {
                con.setRequestProperty( "Content-Type", "application/json; charset=utf-8" );

                String userpass = request.getConfiguration().getUsername() + ":" + request.getConfiguration().getPassword();
                String basicAuth = "Basic " + new String( new Base64().encode( userpass.getBytes() ) );
                con.setRequestProperty( "Authorization", basicAuth );
            }

            // Send post request
            con.setDoOutput(true);
            try
            {
                // 3. Body Message Received Handle
                if ( action.getInput() != null && !requestType.equals( Util.REQUEST_TYPE_GET ) )
                { 
                     BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
                     bw.write( action.getInput().getInputStr() );
                     bw.flush();
                     bw.close();
                }
            }
            catch ( Exception ex )
            {
               throw new ActionException( "ERROR ON Util.sendRequestHTTP, REQUESTING -  " + ex.getMessage() );
            }
            
            try
            {
                // 4. Send and get Response <-- ACTUAL SENDING/REQUESTING!!!!!
                output.setResponseCode( con.getResponseCode() );
            }
            catch ( Exception ex )
            {   
                throw new ActionException( "ERROR ON Util.sendRequestHTTP, SERVER NOT KNOWN CASE - " + ex.getMessage() );
            }

            try
            {
                // 5. Message content retrieve
                if ( output.getResponseCode() <= 400 )
                {
                    output.setOutputMsg( readInputStream( con.getInputStream() ) );
                }
                else
                {
                    output.setOutputMsg( readInputStream( con.getErrorStream() ) );
                }
            }
            catch ( Exception ex )
            {
                throw new ActionException( "ERROR ON Util.sendRequestHTTP, DATA READ -  " + ex.getMessage() );
            }

        }
        catch ( Exception ex )
        {
            throw new ActionException( "Failed during sendRequestHTTP: " + ex.getMessage() );
        }
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    public static String readInputStream( InputStream stream ) throws Exception
    {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader in = new BufferedReader( new InputStreamReader( stream, Util.ENCODING_UTF8 ) ))
        {
            String line;

            while ( (line = in.readLine()) != null )
            {
                builder.append( line ); // + "\r\n"(no need, json has no line
                                        // breaks!)
            }

            in.close();
        }

        return builder.toString();
    }
    
    // -------------------------------------------------------------------------
    // Parse some parametters in action configuration file
    // -------------------------------------------------------------------------
    
    /** @value A JSON string or A string 
     * value : 
     * {[%1_ClientCreate]%.response.importSummaries[0].reference}.json
     *          --> Result  [
                 {
                      "realStr": {[1_ClientCreate].response.importSummaries[0].reference}
                      "param": 1_ClientCreate,
                      "key": response.importSummaries[0].reference
                 }
             ]
     * OR
     *       [   
     *          {
                    "attribute": "w75KJ2mc4zz",
                    "value": "{[request].params.firstname}"
                }
             ]
             --> result : 
             [
                 {
                      "realStr":  {[request].params.firstname},
                      "param": request,
                      "key": params.firstname
                 }
             ]
     * **/
    public static JSONArray parseData( String value, String regExp )
    {
        JSONArray params = new JSONArray();   
        Pattern pattern = Pattern.compile( regExp );
        Matcher matcher = pattern.matcher( value );
        while( matcher.find() ) 
        {
            String match = matcher.group();
            
            // Get parameter name which is in "[xxx]". Ex. {[request].firstName}, get "request"
            String parameter = matcher.group( 1 );
            
            // Get key which is in "[xxx]". Ex. {[request].firstName}, get "firstName"
            String key = matcher.group( 2 );

            JSONObject param = new JSONObject();
            param.put( "realStr",  match );
            param.put( "param", parameter );
            param.put( "key", key );
            params.put(  param );
        }
        
        return params;
    }
    
    public static void respondMsgOut( ActionOutput output, HttpServletResponse response )
        throws Exception
    {
        response.setContentType( "application/json" );
        response.setStatus( output.getResponseCode() );
        
        PrintWriter out = response.getWriter();
        out.print( output.getOutputMsg() );
        out.flush();
    }
}
