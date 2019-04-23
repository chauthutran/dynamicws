package psi.ws.action;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.util.AppConfigUtil;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionSet
{
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private JSONArray configActionsList;
    
    private ScriptEngine jsEngine;
    private Action actionsInQueue;
    private JSONObject actionList;
    
    private String serverName;
    private String username;
    private String password;
    
    
    public ActionSet( HttpServletRequest request, HttpServletResponse response, String actionKey ) throws IOException, Exception
    {
        this.httpRequest = request;
        this.httpResponse = response;
        this.actionList = new JSONObject();
        
        // STEP 1. Load common configuration
        JSONObject configData = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_COMMON_DATE, request.getServletContext() );
        this.serverName = configData.getString( "ACCESS_SERVER_NAME" );
        this.username = configData.getString( "ACCESS_SERVER_USERNAME" );
        this.password = configData.getString( "ACCESS_SERVER_PASSWORD" );
        
        // STEP 2. GET config_action from 'config_actions.json' definition
        JSONObject configActionList = AppConfigUtil.readConfigFile( Util.CONFIG_FILE_ACTION_DATA,
            request.getServletContext() );
        this.configActionsList = configActionList.getJSONObject( "actions").getJSONArray( actionKey );
        
        // STEP 2. Create file and reader instance for reading the script file
        File jsLibFile = AppConfigUtil.getJsFile( Util.UTIL_JAVASCRIPT_LIB_FILE, request.getServletContext() );
        this.jsEngine = new ScriptEngineManager().getEngineByName( "JavaScript" );
        //Pass the script file to the engine
        Reader jsFileReader = new FileReader( jsLibFile );
        this.jsEngine.eval( jsFileReader );
        
        if( this.configActionsList.length() > 0 )
        {
            // STEP 4. Put first action in the Queue
            JSONObject firstAction = this.configActionsList.getJSONObject( 0 );
            firstAction.put( "currentIndex", 0 );
            this.actionsInQueue = new Action( this.httpRequest, firstAction, null, this.actionList, this.jsEngine, this.serverName, this.username, this.password );
            
            // STEP 5. Add "currentIndex" and "input" properties ( if any ) for action configuration
            resolveConfigActionList();
        }
        
       
    }
    
    public void run()
    {   
        try
        {
            // STEP 1. Run action
            this.actionList.put( this.actionsInQueue.getName(), this.actionsInQueue );
          System.out.println("\n\n === \n Name : " + actionsInQueue.getName() );
          System.out.println(" Input : " + actionsInQueue.getInput().getInputStr() );
          System.out.println("Output 1 : " + actionsInQueue.getOutput().getOutputMsg() );
            actionsInQueue.run( this.actionList );
            System.out.println("Output 2 : " + actionsInQueue.getOutput().getOutputMsg() );
                  
            // STEP 2. Put the next action in Queue
            String nextActionName = actionsInQueue.getGoTo().getNextActionName( this.configActionsList, actionsInQueue.getOutput()  );
            
            if( nextActionName != null )
            {
                JSONObject nextActionConfig = JSONUtil.getJsonObject( this.configActionsList, "name", nextActionName ); 
                Action nextAction = new Action( httpRequest, nextActionConfig, actionsInQueue, this.actionList, this.jsEngine, this.serverName, this.username, this.password );
                actionsInQueue = nextAction;
                this.run();
            }
            else
            {
                Util.respondMsgOut( actionsInQueue.getOutput(), httpResponse );
            } 
        }
        catch( Exception ex )
        {
           ex.printStackTrace();
        }
    }
    
    // -------------------------------------------------------------------------
    // Suppotive methods
    // -------------------------------------------------------------------------
    
    private void resolveConfigActionList()
    {
        for( int i = 1; i < this.configActionsList.length(); i ++ )
        {
            JSONObject configAction = this.configActionsList.getJSONObject( i );
            
            // Add "currentIndex" property for each action configuration. 
            // It is used when the current action doesn't have goTo defination and we need to get the next action
            configAction.put( "currentIndex", i );
        }
        
    }

}
