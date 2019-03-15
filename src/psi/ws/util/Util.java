package psi.ws.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.service.DataStore;

import com.sun.istack.internal.logging.Logger;


public final class Util
{
    public static boolean DEBUG_FLAG = false;

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final int REQUEST_TIMEOUT = 240000; // 4 min;
    
    public static final String REQUEST_CONTENT_TYPE_DHIS = "DHIS";
    public static final String REQUEST_CONTENT_TYPE_FOCUSONE = "FOCUSONE";
    public static final String REQUEST_CONTENT_TYPE_LOCAL_SMS_JSON = "LOCAL_SMS_JSON";
    public static final String REQUEST_CONTENT_TYPE_LOCAL_SMS_POST_FORM = "LOCAL_SMS_POST_FORM";
    

    public static final String REQUEST_TYPE_GET = "GET";
    public static final String REQUEST_TYPE_POST = "POST";
    public static final String REQUEST_TYPE_PUT = "PUT";
    public static final String REQUEST_TYPE_DELETE = "DELETE";

    public static String ACCESS_SERVER_USERNAME_PROFILER = "";
    public static String ACCESS_SERVER_PASSWORD_PROFILER = "";

    
    public static String REQUEST_ACTION_TYPE_DHIS = "DhisApi";
    public static String REQUESTION_DHIS_ACTION_ID_REQUESTCUSTOM = "RequestCustom";
    public static String REQUESTION_DHIS_ACTION_ID_CLIENTGET = "ClientGet";
    public static String REQUESTION_DHIS_ACTION_ID_CLIENTUPDATE = "ClientUpdate";
    public static String REQUESTION_DHIS_ACTION_ID_CLIENTCREATE = "ClientCreate";

    // -------------------------------------------------------------------------
    // DHIS RELATED 
    // -------------------------------------------------------------------------

    public static void sendRequest( String requestType, String url, JSONObject jsonData, Map<String, Object> params,
        String sourceType, DataStore dataStore, String username, String password )
        throws Exception, IOException
    {
        try
        {
            Util.output( "Util.sendRequest [REQUEST][" + requestType + "] URL: " + url );
            if ( jsonData != null )
                Util.output( "Util.sendRequest [REQUEST] JSON: " + jsonData.toString() );

            dataStore.clearContent();

            // HTTPS is also handled by HTTP - 'HttpsURLConnection' extends
            // HttpURLConnection'
            Util.sendRequestHTTP( dataStore, requestType, url, jsonData, params, sourceType, username, password );
            Util.output( "Util.sendRequest [RESPONSE]: " + dataStore.output );

            if ( dataStore.responseCode >= 400 )
            {
                dataStore.errorMsg = Util.getErrorMsgContent( dataStore.output );

                throw new Exception( dataStore.errorMsg );
            }
        }
        catch ( Exception ex )
        {
            // Util.output( "Util.sendRequest RESPONSEINFO: " +
            // dataStore.toString() );

            String errMsg = "SendRequest, responseCode: " + dataStore.responseCode + ", Msg: " + ex.getMessage();
            Util.outputErr( "" + errMsg );

            // dataStore.output = errMsg; // This should be obsolete later once
            // all are organized.
            // dataStore.errorMsg = errMsg;
            // For collectionMsg, it should not add errorMsg, but put the
            // errorMsg separately.

            throw ex;
        }
    }

    // HTTPS GET/POST/PUT request
    private static void sendRequestHTTP( DataStore dataStore, String requestType, String url, JSONObject jsonData,
        Map<String, Object> params, String sourceType, String username, String password )
        throws Exception, IOException
    {
        try
        {
            System.out.println("\n === requestUrl : " + url );    
            
            dataStore.data = jsonData;
            // dataStore.sendStr = bodyMessage;

            // 2. Open HttpsURLConnection and Set Request Type.
            URL obj = new URL( url );
            // Since HttpsURLConnection extends HttpURLConnection, we can use
            // this for both HTTP & HTTPS?
            // HttpsURLConnection con = (HttpsURLConnection)
            // obj.openConnection();
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // add Request header
            con.setRequestMethod( requestType );

            // con.setRequestProperty( "User-Agent",
            // "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11"
            // );
//            con.setRequestProperty( "User-Agent", "ConnectApp/" + VERSION_NO + " CFNetwork/711.1.16 Darwin/14.0.0" );

            con.setRequestProperty( "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8" );
            con.setRequestProperty( "Accept-Language", "en-US,en;q=0.5" );

            // Timeout <-- set to 180 sec / 3 min..
            con.setConnectTimeout( Util.REQUEST_TIMEOUT );
            con.setReadTimeout( Util.REQUEST_TIMEOUT );

            if ( sourceType.equals( Util.REQUEST_CONTENT_TYPE_DHIS ) )
            {
                con.setRequestProperty( "Content-Type", "application/json; charset=utf-8" );

                String userpass = username + ":" + password;
                String basicAuth = "Basic " + new String( new Base64().encode( userpass.getBytes() ) );
                con.setRequestProperty( "Authorization", basicAuth );
            }
            else if ( sourceType.equals( Util.REQUEST_CONTENT_TYPE_FOCUSONE ) )
            {
                con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" );
            }
            else if ( sourceType.equals( Util.REQUEST_CONTENT_TYPE_LOCAL_SMS_JSON ) )
            {
                con.setRequestProperty( "Content-Type", "application/json; charset=utf-8" );
            }
            else if ( sourceType.equals( Util.REQUEST_CONTENT_TYPE_LOCAL_SMS_POST_FORM ) )
            {
                con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            }
            else
            {
                con.setRequestProperty( "Content-Type", "text/plain; charset=utf-8" );
            }

            // Moved out from Step 3.
            con.setDoOutput( true );

            try
            {
                // 3. Body Message Received Handle
                if ( jsonData != null && jsonData.length() > 0 )
                {
                    // Send post request
                    // con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream( con.getOutputStream() );

                    byte[] jsonDataBytes = jsonData.toString().getBytes( Util.ENCODING_UTF8 );
                    wr.write( jsonDataBytes );

                    // wr.writeBytes( jsonData.toString() ); // This one does
                    // ISO-8859-1
                    wr.flush();
                    wr.close();
                }
                else if ( params != null && !params.isEmpty() )
                {
                    // con.setDoOutput(true);
                    DataOutputStream wr = new DataOutputStream( con.getOutputStream() );

                    if ( requestType.equals( Util.REQUEST_TYPE_GET ) )
                    {
                        // Should be part of the url?
                        // con.set
                    }
                    else if ( requestType.equals( Util.REQUEST_TYPE_POST ) )
                    {

                        // Need to make sure of this.. TEsting...
                        StringBuilder postData = new StringBuilder();
                        for ( Map.Entry<String, Object> param : params.entrySet() )
                        {
                            if ( postData.length() != 0 )
                                postData.append( '&' );

                            postData.append( URLEncoder.encode( param.getKey(), Util.ENCODING_UTF8 ) );
                            postData.append( '=' );
                            postData
                                .append( URLEncoder.encode( String.valueOf( param.getValue() ), Util.ENCODING_UTF8 ) );
                        }

                        byte[] postDataBytes = postData.toString().getBytes( Util.ENCODING_UTF8 );

                        wr.write( postDataBytes );
                        // con.setRequestProperty("Content-Length",
                        // String.valueOf(postDataBytes.length));
                    }

                    wr.flush();
                    wr.close();
                }
            }
            catch ( Exception ex )
            {
                // wr.write does ACTUAL REQUESTING!!!
                Util.outputErr( "ERROR ON Util.sendRequestHTTP, REQUESTING - " + ex.getMessage() );
                throw ex;
            }

            try
            {
                // 4. Send and get Response <-- ACTUAL SENDING/REQUESTING!!!!!
                dataStore.responseCode = con.getResponseCode();
            }
            catch ( Exception ex )
            {
                Util.outputErr( "ERROR ON Util.sendRequestHTTP, SERVER NOT KNOWN CASE - " + ex.getMessage() );
                dataStore.responseCode = 520;

                throw ex;
            }

            try
            {
                // 5. Message content retrieve
                // if ( dataStore.responseCode == HttpURLConnection.HTTP_OK )
                if ( dataStore.responseCode < 400 )
                {
                    dataStore.output = readInputStream( con.getInputStream() );
                }
                else
                {
                    dataStore.output = readInputStream( con.getErrorStream() );
                }
            }
            catch ( Exception ex )
            {
                Util.outputErr( "ERROR ON Util.sendRequestHTTP, DATA READ - " + ex.getMessage() );
                throw ex;
            }

        }
        catch ( Exception ex )
        {
            Util.outputErr( "Failed during sendRequestHTTP: " + ex.getMessage() );
            // responseMsgTemp.append( "-- Failed during sendRequestHTTP" );

            throw ex;
        }
    }
    

    // -------------------------------------------------------------------------
    // Outputing Related 
    // -------------------------------------------------------------------------


    // Convert InputStream to String
    public static JSONObject getJsonFromInputStream( InputStream is )
        throws Exception
    {
        JSONObject jsonData = new JSONObject(); // Default blank Json

        try
        {
            String contentStr = Util.readInputStream( is );

            if ( !contentStr.isEmpty() )
            {
                jsonData = new JSONObject( contentStr );
            }
        }
        catch ( IOException e )
        {
            Util.outputErr( "Failed on getJsonFromInputStream" );
            e.printStackTrace();
            throw e;
        }

        return jsonData;
    }


    private static String getErrorMsgContent( String input )
    {
        String output = "";

        // System.out.println( "FROM getErrorMsgContent(), INPUT: " + input );

        try
        {
            JSONObject jsonMsg = new JSONObject( input );

            // string =
            // {"httpStatus":"Internal Server Error","httpStatusCode":500,"status":"ERROR","message":"Invalid format: \"WRONG DATE FORMAT\""}
            if ( jsonMsg.has( "message" ) )
            {
                output = jsonMsg.get( "message" ).toString();

                output += " " + Util.getImportSummaryConflicts( jsonMsg );
            }
        }
        catch ( Exception ex )
        {
            Util.outputErr( "FAILED on getErrorMsgContent, inputStr: " + input );

            // If failed to retrieve message or convert to json, simply get it
            // as string
            output = input;
        }

        return JSONUtil.jsonStrFormat( output );
    }
    

    public static void responseMsgSummarize( DataStore dataStore, String progressMsgs, String clientId,
        int responseCodeOverride )
    {
        // if override responseCode is passed, submit it..
        if ( responseCodeOverride != 0 )
            dataStore.responseCode = responseCodeOverride;

        dataStore.outDataJson.put( "msg", JSONUtil.jsonStrFormat( progressMsgs ) );
        JSONObject dataJson = JSONUtil.getJsonObject_Create( dataStore.outDataJson, "data" );
        dataJson.put( "clientId", clientId );

        dataStore.outMessage = dataStore.outDataJson.toString();
    }

    public static void updateResponseMsg( DataStore dataStore, String progressMsgs )
    {
        updateResponseMsg( dataStore, progressMsgs, 0 );
    }

    public static void updateResponseMsg( DataStore dataStore, String progressMsgs, int responseCodeOverride )
    {
        // if override responseCode is passed, submit it..
        if ( responseCodeOverride != 0 )
            dataStore.responseCode = responseCodeOverride;

        String existingMsg = JSONUtil.getJSONStrVal( dataStore.outDataJson, "msg" );
        dataStore.outDataJson.put( "msg", existingMsg + JSONUtil.jsonStrFormat( progressMsgs ) );
        // JSONObject data = Util.getJsonObject_Create( dataStore.outDataJson,
        // "data" );

        dataStore.outMessage = dataStore.outDataJson.toString();
    }
    

    public static String getResultRefId( JSONObject rec ) throws Exception
    {
        String referenceId = "";

        if ( rec != null && rec.has( "status" ) && rec.getString( "status" ).equals( "SUCCESS" ) )
        {
            // Here is difference..
//            JSONObject importCount = rec.getJSONObject( "importCount" );
//            int imported = JSONUtil.getJsonValInt( importCount, "imported", 0 );
//            int updated = JSONUtil.getJsonValInt( importCount, "updated", 0 );

            // NOTE: SCHEDULED event does not give 'imported' & 'updated' count
            // aboe 0.
            // NOTE: In "importSummaries" case, it shows up as 0 for 'imported'
            // if ( imported >= 1 || updated >= 1 ) //|| summaryTypeCase )
            // //summaryType.equals( "importSummaries" ) )

            referenceId = JSONUtil.getJSONStrVal( rec, "reference" );
        }

        if ( referenceId.isEmpty() )
            throw new Exception( "referenceId not available" );

        return referenceId;
    }
    
    // -------------------------------------------------------------------------
    //  Response Msg Related 
    // -------------------------------------------------------------------------
            
    public static void processResponseMsg( DataStore dataStore, String importSummaryCase )
    {
        if ( dataStore.responseCode != 200 )
        {
            // If error occured, display the output as it is (received from
            // DHIS).
            // Set return Msg
            dataStore.outMessage = dataStore.output;
        }
        else
        {
            // Set return Msg
            dataStore.referenceId = Util.outputImportResult( dataStore.output, importSummaryCase );
            dataStore.outMessage = "{ \"id\": \"" + dataStore.referenceId + "\" }";
        }
    }

    public static void setResponseHeaderCommon( HttpServletResponse response )
    {
        // No caching header
        response.setHeader( "Cache-Control", "no-cache, no-store, must-revalidate" ); // HTTP
                                                                                      // 1.1.
        response.setHeader( "Pragma", "no-cache" ); // HTTP 1.0.
        response.setDateHeader( "Expires", 0 ); // Proxies.

//        if ( Util.WS_DEV3 )
//        {
//            // NOTE: ADDED FOR WHITE LISTING, CORS (DISABLING CORS), ONLY BE
//            // ALLOWED FOR 'eRefWSDev3'!!!
            response.setHeader( "Access-Control-Allow-Origin", "*" );
            response.setHeader( "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept" );
//        }
    }

    // ArrayList<ExecutorService>
    public static void respondMsgOut( HttpServletResponse response, DataStore dataStore,
        ArrayList<ExecutorService> exeSvcList )
    {
        try
        {
            if ( exeSvcList != null && exeSvcList.size() > 0 )
                Util.terminateExecutorServiceAll( exeSvcList );

            if ( dataStore == null )
                dataStore = new DataStore();

            // No caching header
            setResponseHeaderCommon( response );

            response.setContentType( "application/json;charset=" + Util.ENCODING_UTF8 );
            response.setStatus( dataStore.responseCode );

            try (PrintWriter out = response.getWriter())
            {
                if ( dataStore.outputJson != null )
                {
                    out.print( dataStore.outputJson );
                }
                else
                {
                    out.print( JSONUtil.getJsonFormattedStr( dataStore.outMessage ) );
                }

                out.flush();
            }

        }
        catch ( Exception ex )
        {
            Util.outputErr( "\n=== Error FORM respondMsgOut === \n" );
        }
    }

    public static void respondMsgOut( HttpServletResponse response, JSONObject content )
    {
        try
        {
            setResponseHeaderCommon( response );

            response.setContentType( "application/json;charset=" + Util.ENCODING_UTF8 );
            response.setStatus( 200 );

            try (PrintWriter out = response.getWriter())
            {
                if ( content != null )
                {
                    out.print( content );
                }

                out.flush();
            }

        }
        catch ( Exception ex )
        {
            Util.outputErr( "\n=== Error FORM respondMsgOut === \n" );
        }
    }

    public static void respondMsgOut( HttpServletResponse response, DataStore dataStore )
    {
        respondMsgOut( response, dataStore, null );
    }

    // ArrayList<ExecutorService>
    public static void respondMsgOutStr( HttpServletResponse response, int statusCode, String contentType,
        String contentStr )
    {
        try
        {
            setResponseHeaderCommon( response );

            response.setStatus( statusCode ); // dataStore.responseCode );

            response.setContentType( contentType );

            try (PrintWriter out = response.getWriter())
            {
                out.print( contentStr );
                out.flush();
            }
        }
        catch ( Exception ex )
        {
            Util.outputErr( "\n=== Error FORM respondMsgOut === \n" );
        }
    }

    public static String outputImportResult( String output, String summaryType )
    {
        String referenceId = "";
        JSONObject rec = null;

        try
        {
            if ( summaryType.isEmpty() )
                throw new Exception( "summaryType empty is not supported type anymore." );

            JSONObject recTemp = new JSONObject( output );
            // boolean summaryTypeCase = false;

            // 'importSummaries' vs 'importSummary'

            JSONObject response = JSONUtil.getJsonObject( recTemp, "response" );

            if ( response != null )
            {
                String responseType = JSONUtil.getJSONStrVal( response, "responseType" );

                if ( responseType.equals( "ImportSummary" ) )
                {
                    rec = response;
                }
                else if ( responseType.equals( "ImportSummaries" ) )
                {
                    JSONArray importSummaries = JSONUtil.getJsonArray( response, "importSummaries" );

                    if ( importSummaries != null && importSummaries.length() > 0 )
                    {
                        rec = importSummaries.getJSONObject( 0 );
                    }
                }
            }

            referenceId = Util.getResultRefId( rec );
        }
        catch ( Exception ex )
        {
            Util.outputErr( "ERROR - on Util.outputImportResult(): " + ex.getMessage() );
        }

        return referenceId;
    }
    
    public static String getImportSummaryConflicts( JSONObject jsonData )
    {
        String msg = "";

        try
        {
            if ( jsonData.has( "response" ) )
            {
                JSONObject responseJson = jsonData.getJSONObject( "response" );

                if ( responseJson.has( "conflicts" ) )
                {
                    msg = getErrMsgFromConflicts( responseJson );
                }
                else if ( responseJson.has( "importSummaries" ) )
                {
                    JSONObject importSummraryJson = responseJson.getJSONArray( "importSummaries" ).getJSONObject( 0 );

                    msg += JSONUtil.getJSONStrVal( importSummraryJson, "description" ) + " ";

                    msg += getErrMsgFromConflicts( importSummraryJson );
                }
            }
        }
        catch ( Exception ex )
        {
            Util.outputErr( "FAILED during getImportSummaryConflicts()" );
        }

        return msg;
    }
    
    public static String getErrMsgFromConflicts( JSONObject responseJson )
    {
        String msg = "";

        if ( responseJson.has( "conflicts" ) )
        {
            JSONArray conflictsJson = responseJson.getJSONArray( "conflicts" );

            for ( int i = 0; i < conflictsJson.length(); i++ )
            {
                JSONObject conflictJson = conflictsJson.getJSONObject( i );

                msg += ((msg.isEmpty()) ? "" : ", ") + JSONUtil.getJsonObjValuesStr( conflictJson );
            }
        }

        return msg;
    }   
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------
    
    private static String readInputStream( InputStream stream )
        throws Exception
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
    // Output methods
    // -------------------------------------------------------------------------
    
    public static void setDebugFlag( boolean flag )
    {
        DEBUG_FLAG = flag;
    }
    
    public static void output( String msg )
    {
        Util.output( Util.class, msg, Util.DEBUG_FLAG );
        // System.out.println( msg );
    }

    public static void output( String msg, boolean bShow )
    {
        Util.output( Util.class, msg, bShow );
        // System.out.println( msg );
    }

    public static void output( Class<?> arg1, String msg, boolean bShow )
    {
        if ( bShow )
            Logger.getLogger( arg1 ).log( Level.INFO, "\n = = = " + msg + System.getProperty( "line.separator" ) );
    }

    public static void outputErr( String msg )
    {
        Util.output( Util.class, msg, true );
    }

    public static void outputDebug( String msg )
    {
        Util.output( Util.class, msg, true );
    }

    
    // -------------------------------------------------------------------------
    // Get server information
    // -------------------------------------------------------------------------
    
    public static String getServletName( HttpServletRequest request )
        throws ServletException
    {
        String servletName = "";

        servletName = request.getContextPath().split( "/" )[1];

        if ( servletName.isEmpty() )
            throw new ServletException( "FAILED to get servlet name" );

        return servletName;
    }

    public static void terminateExecutorServiceAll( ArrayList<ExecutorService> exeSvcList )
    {
        try
        {
            if ( exeSvcList == null )
            {
                Util.output( "exeSvcList is null", true );
                // throw new Exception( "exeSvcList is null" );
            }

            // Util.output( "terminateExecutorServiceAll, exeSvcList.size(): " +
            // String.valueOf( exeSvcList.size() ), true );

            // is ExecutorService exists, terminate them a..
            if ( exeSvcList != null && exeSvcList.size() > 0 )
            {
                for ( ExecutorService exeSvc : exeSvcList )
                {
                    try
                    {
                        exeSvc.shutdown();

                        // TODO: Should use finally?
                        try
                        {
                            if ( !exeSvc.awaitTermination( 50, TimeUnit.MILLISECONDS ) )
                            {
                                exeSvc.shutdownNow();
                            }
                        }
                        catch ( InterruptedException e )
                        {
                            exeSvc.shutdownNow();
                        }
                    }
                    catch ( Exception ex )
                    {

                    }
                }
            }
        }
        catch ( Exception ex )
        {
            Util.outputErr( "ERROR - Failed on terminateExecutorServiceAll(), " + ex.getMessage() );
        }
    }
}
