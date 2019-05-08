package psi.ws.action;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.configuration.Configuration;
import psi.ws.exception.ActionException;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionSet
{
    
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    
    private Configuration configuration;
    private ActionJSEngine actionJSEngine;

    private JSONObject requestData;
    private List<Action> ranActions;
    private JSONObject ranActionsByName; // list of actions by name
    

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionSet( HttpServletRequest httpRequest, HttpServletResponse httpResponse, String actionKey )
    {
        try
        {
            this.httpRequest = httpRequest;
            this.httpResponse = httpResponse;
            
            this.ranActions = new ArrayList<Action>();
            this.ranActionsByName = new JSONObject();

            
            // 1. Configuration Class instantiate
            this.configuration = new Configuration( this.httpRequest, actionKey );
            
            this.requestData = retrieveInputStreamData();

            // 2. jsEngine
            actionJSEngine = new ActionJSEngine( this.httpRequest );

            // 3. Add "index" property for each action configuration
            resolveConfigActionList();

        }
        catch ( ActionException ex )
        {
            writeData( httpResponse, ex );
        }
        catch ( ScriptException ex )
        {
            writeData( httpResponse, ex );
        }
    }

    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------
    
    public void run()
    {
        try
        {
            runActions();
        }
        catch ( ActionException ex )
        {
            writeData( httpResponse, ex );
        }

    }

    public List<Action> getRanActions()
    {
        return this.ranActions;
    }
    
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void resolveConfigActionList()
    {
        JSONArray configActionList = this.configuration.getConfigActionList();
        for ( int i = 0; i < configActionList.length(); i++ )
        {
            JSONObject configAction = configActionList.getJSONObject( i );

            // Add "index" property for each action configuration.
            // It is used when the current action doesn't have goTo defination
            // and we need to get the next action
            configAction.put( "index", i );
        }

    }

    private String getFirstActionName()
        throws ActionException
    {
        JSONObject firstAction = this.configuration.getConfigActionList().getJSONObject( 0 );
        return firstAction.getString( "name" );
    }

    private Action getLastActionInRanList()
    {
        if ( this.ranActions.size() > 0 )
        {
            int len = this.ranActions.size();
            return this.ranActions.get( len - 1 );
        }

        return null;
    }

    private Action createAction( String actionName ) throws ActionException
    {
        // Get configuration action by name
        JSONObject configAction = JSONUtil.getJsonObject( this.configuration.getConfigActionList(), "name", actionName );
        
        // Create Action object
        return new Action( configAction
            , this.requestData
            , ranActions
            , ranActionsByName
            , actionJSEngine
            , this.configuration );
    }
    
    private void runActions()
        throws ActionException
    {
        String nextActionName = getFirstActionName();
        while ( nextActionName != null )
        {
            // Create the action object
            Action action = this.createAction( nextActionName );
            
            // Run action
            action.run( ranActionsByName );
            
            // Get next action
            nextActionName = action.getGoTo().getNextActionName( this.configuration.getConfigActionList(),
                action.getOutput() );  
        }

        try
        {
            writeData( httpResponse, null );
        }
        catch ( Exception e )
        {
            throw new ActionException( "Fail to write result data" );
        }

    }

    private void writeData( HttpServletResponse response, Exception e )
    {
        try
        {
            JSONObject resultData = new JSONObject();
            if( e != null )
            {
                resultData.put( "finalErrorMsg", e.getMessage() );
            }
            else
            {
                Action lastAction = this.getLastActionInRanList();
                if( lastAction != null )
                {
                    resultData.put( "finalSuccessMsg", lastAction.getOutput().getOutputJson() );
                }
            }
            
            JSONArray processed = new JSONArray();
            for( int i = 0; i < this.ranActions.size(); i++ )
            {
                Action action = this.ranActions.get( i );
                JSONObject output = action.getOutput().getOutputJson();
                output.put( "actionName", action.getName() );
                processed.put( output);
            }
            resultData.put( "details", processed );

            ActionOutput output = new ActionOutput( resultData.toString(), 200 );
            Util.respondMsgOut( output, response );
        }
        catch ( Exception ex )
        {
            System.out.println( "Error when to output data" );
        }

    }

    private JSONObject retrieveInputStreamData() throws ActionException
    {
        JSONObject requestData = new JSONObject();
        
        try
        {
            requestData = JSONUtil.getJsonFromInputStream( httpRequest.getInputStream() );
        }
        catch( Exception ex )
        {
            throw new ActionException( "Fail to get data from request" );
        }
        
        return requestData;
        
    }
    
}
