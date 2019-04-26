package psi.ws.action;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.util.JSONUtil;
import psi.ws.util.Util;

public class ActionSet
{
    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;
    private Configuration configuration;
    private ActionJSEngine actionJSEngine;

    private List<Action> ranActions;
    private JSONObject ranActionsByName; // list of actions by name
    

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionSet( HttpServletRequest request, HttpServletResponse response, String actionKey )
    {
        try
        {
            this.httpRequest = request;
            this.httpResponse = response;
            this.ranActions = new ArrayList<Action>();
            this.ranActionsByName = new JSONObject();

            // 1. Configuration Class instantiate
            configuration = new Configuration( request, actionKey );

            // 2. jsEngine
            actionJSEngine = new ActionJSEngine( request );

            // 3. Add "index" property for each action configuration
            resolveConfigActionList();

        }
        catch ( ActionException ex )
        {
            writeErrorMsg( ex, response );
        }
        catch ( ScriptException ex )
        {
            writeErrorMsg( ex, response );
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
            writeErrorMsg( ex, this.httpResponse );
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


    private void runActions()
        throws ActionException
    {
        String server = this.configuration.getServer();
        String username = this.configuration.getUsername();
        String password = this.configuration.getPassword();

        String nextActionName = getFirstActionName();
        while ( nextActionName != null )
        {
            // Get next action by name
            JSONObject configAction = JSONUtil.getJsonObject( this.configuration.getConfigActionList(), "name",
                nextActionName );

            // Get lastAction in ran action list
            Action lastAction = getLastActionInRanList();

            // Create the action object
            Action action = new Action( this.httpRequest, configAction, lastAction, this.ranActionsByName,
                actionJSEngine, server, username, password );

            // Run action
            action.run( ranActionsByName );
            
            // Put the ran action in list
            this.ranActions.add( action );
            this.ranActionsByName.put( action.getName(), action );

            // Get next action
            nextActionName = action.getGoTo().getNextActionName( this.configuration.getConfigActionList(),
                action.getOutput() );  
        }

        try
        {
            Util.respondMsgOut( getLastActionInRanList().getOutput(), httpResponse );
        }
        catch ( Exception e )
        {
            throw new ActionException( "Fail to write result data" );
        }

    }

    private void writeErrorMsg( Exception e, HttpServletResponse response )
    {
        try
        {
            JSONObject errData = new JSONObject();
            errData.put( "errMsg", e.getMessage() );

            ActionOutput output = new ActionOutput( errData.toString(), 200 );
            Util.respondMsgOut( output, response );
        }
        catch ( Exception ex )
        {
            System.out.println( "Error when to output data" );
        }

    }

}
