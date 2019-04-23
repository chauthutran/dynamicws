package psi.ws.main;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import psi.ws.action.ActionSet;
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
    //

    protected void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        try
        {
//            // STEP 1. Get username and password to access the server by reading
//            // 'config.json' file
//            JSONObject configCommonData = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_COMMON_DATE,
//                request.getServletContext() );
//            String serverName = configCommonData.getString( "ACCESS_SERVER_NAME" );
//            String username = configCommonData.getString( "ACCESS_SERVER_USERNAME" );
//            String password = configCommonData.getString( "ACCESS_SERVER_PASSWORD" );
            
            // STEP 2. GET config_action from 'config_actions.json' definition
            JSONObject configActionList = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_ACTION_DATA,
                request.getServletContext() );

            // STEP 3. Run actions
            if ( request.getPathInfo() != null && request.getPathInfo().split( "/" ).length >= 2 )
            {
                String[] queryPathList = request.getPathInfo().split( "/" );
                String key = queryPathList[1];

                // Create file and reader instance for reading the script file
                File jsFile = AppConfigUtil.getJsFile( Util.UTIL_JAVASCRIPT_LIB_FILE, request.getServletContext() );
               
//                if ( key.equals( Util.ACTION_TYPE_ISSUEVOUCHER ) )
//                {
//                    JSONArray actionList = configActionList.getJSONObject( "actions").getJSONArray( key );
                    ActionSet actionSet = new ActionSet( request, response, key );

                    actionSet.run();
//                }

            }

        }
        catch ( Exception ex )
        {
            System.out.println( "Exception: " + ex.toString() );
            // Util.respondMsgOut( response, dataStore );
//            dataStore.responseCode = 400;
//            dataStore.outMessage = ex.getMessage();
        }
    }

    // protected void doPost( HttpServletRequest request, HttpServletResponse
    // response )
    // throws ServletException, IOException
    // {
    // DataStore dataStore = new DataStore();
    //
    // try
    // {
    // // STEP 1. Get username and password to access the server by reading
    // 'config.json' file
    // JSONObject configCommonData = AppConfigUtil.readConfigFile(
    // Util.CONFIG_FILE_COMMON_DATE, request.getServletContext() );
    // String serverName = configCommonData.getString( "ACCESS_SERVER_NAME" );
    // String username = configCommonData.getString( "ACCESS_SERVER_USERNAME" );
    // String password = configCommonData.getString( "ACCESS_SERVER_PASSWORD" );
    // JSONObject payload = JSONUtil.getJsonFromInputStream(
    // request.getInputStream() );
    //
    // // STEP 2. GET config_action from 'config_actions.json' definition
    // JSONObject configActionsList = AppConfigUtil.readConfigFile(
    // Util.CONFIG_FILE_ACTION_DATA,
    // request.getServletContext() );
    //
    // // STEP 3. Run actions
    // if ( request.getPathInfo() != null && request.getPathInfo().split( "/"
    // ).length >= 2 )
    // {
    // String[] queryPathList = request.getPathInfo().split( "/" );
    // String key = queryPathList[1];
    //
    // //Create file and reader instance for reading the script file
    // File jsFile = AppConfigUtil.getJsFile( Util.UTIL_JAVASCRIPT_LIB_FILE,
    // request.getServletContext() );
    // ActionConfig actionConfig = new ActionConfig( configActionsList, key,
    // jsFile );
    //
    // if ( key.equals( Util.ACTION_TYPE_ISSUEVOUCHER ) )
    // {
    // for ( int i = 0; i < actionConfig.getLeng(); i++ )
    // {
    // dataStore = actionConfig.runAction( i, payload, serverName, username,
    // password );
    // }
    // }
    //
    // }
    //
    // // Util.respondMsgOut( response, dataStore );
    // System.out.println( dataStore.output );
    // }
    // catch ( Exception ex )
    // {
    // System.out.println( "Exception: " + ex.toString() );
    // // Util.respondMsgOut( response, dataStore );
    // dataStore.responseCode = 400;
    // dataStore.outMessage = ex.getMessage();
    // }
    // }

}
