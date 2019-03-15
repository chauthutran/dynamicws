package psi.ws.main;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.service.DHISApiService;
import psi.ws.service.DataStore;
import psi.ws.util.AppConfigUtil;
import psi.ws.util.Util;


public class RunService
    extends HttpServlet
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    
    // -------------------------------------------------------------------------
    // POST method
    // -------------------------------------------------------------------------

    /**
     * 
     {
        "actions": [
            {
                    "actionId": "ClientGet",
                     "payload": {
                            "teiId": "VlrQUh7HzjP",
                            "programId": "A7SRy7lpk1x"
                     },
                     "actionType": "DhisApi",
                     "actionEval": "if ( !output.trackedEntityInstance ) ‘{ERROR}-Client Not Found’;"
            }
             
        ]
    }
     * 
     * **/
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        DataStore dataStore = null;

        try
        {  
           // STEP 1. Get username and password to access the server by reading config.json file
           JSONObject configData = AppConfigUtil.readConfigFile( request.getServletContext() );
           String serverName = configData.getString( "ACCESS_SERVER_NAME" );
           String username = configData.getString( "ACCESS_SERVER_USERNAME" );
           String password = configData.getString( "ACCESS_SERVER_PASSWORD" );
           
           // STEP 2. Get request data from client
           JSONObject receivedMsgBody = Util.getJsonFromInputStream( request.getInputStream() );
           JSONArray actionListConfig = receivedMsgBody.getJSONArray( "actions" );
           
           dataStore = DHISApiService.run( actionListConfig, serverName, username, password );
           Util.respondMsgOut( response, dataStore );
        }
        catch ( Exception ex )
        {
            System.out.println( "Exception: " + ex.toString() );
            Util.respondMsgOut( response, dataStore );
        }
    }
   
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        DataStore dataStore = null;

        try
        {  
           // STEP 1. Get username and password to access the server by reading config.json file
           JSONObject configData = AppConfigUtil.readConfigFile( request.getServletContext() );
           String serverName = configData.getString( "ACCESS_SERVER_NAME" );
           String username = configData.getString( "ACCESS_SERVER_USERNAME" );
           String password = configData.getString( "ACCESS_SERVER_PASSWORD" );
           
           // STEP 2. Get request data from client
           JSONObject receivedMsgBody = Util.getJsonFromInputStream( request.getInputStream() );
           JSONArray actionListConfig = receivedMsgBody.getJSONArray( "actions" );

System.out.println("\n\n ====  1 ");
//           JSONArray actionListConfig = RunService.generateActionsConfig();
System.out.println("\n\n ==== 2 actionListConfig : " + actionListConfig.toString() );

           dataStore = DHISApiService.run( actionListConfig, serverName, username, password );
           
           Util.respondMsgOut( response, dataStore );
           System.out.println( dataStore.output );
        }
        catch ( Exception ex )
        {
            System.out.println( "Exception: " + ex.toString() );
            Util.respondMsgOut( response, dataStore );
        }
    }
    
}
