package psi.ws.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import psi.ws.exception.ActionException;

// "goTo" : ( [OUTPUT].response.status == \"SUCCESS\" ) ? {[2_ClientGet]} : {[5_END]}
public class ActionGoTo
{
    private static final String REGEXP_ACTIONNAME = "\\{\\s*\\[(\\w+)\\]\\s*}";
    
    private String goTo;
    private int index;
    private ActionJSEngine actionJsEngine;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ActionGoTo( int index, ActionJSEngine actionJsEngine  )
    {
        this.index = index;
        this.actionJsEngine = actionJsEngine;
    }
    
    public void setGoToDefination( String goTo )
    {
        this.goTo = goTo;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public String getNextActionName( JSONArray actionConfigList, ActionOutput output ) throws ActionException
    {
        if( this.goTo != null )
        {
            String currentActionName = actionConfigList.getJSONObject( this.index ).getString( "name" );
            return this.getNextActionName( currentActionName, output );
        }
        else if( this.index < actionConfigList.length() )
        { 
            int next = this.index + 1;
            if ( next < actionConfigList.length() )
            {
                return actionConfigList.getJSONObject( next ).getString( "name" );
            }
        }
        
        return null;
    }
    
    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * @script ( [OUTPUT].response.status == \"SUCCESS\" ) ? {[2_ClientGet]} : {[5_END]}
     * @return "2_ClientGet" OR "5_END"
     * @throws ActionException 
     * **/
    private String getNextActionName( String currentActionName, ActionOutput output ) throws ActionException
    {   
        
        String script = this.goTo;
        Pattern pattern = Pattern.compile( REGEXP_ACTIONNAME );
        Matcher matcher = pattern.matcher( this.goTo );
        while( matcher.find() ) 
        {
            String match = matcher.group(0);
            String actionName = matcher.group(1);
            script = script.replace( match, "\"" + actionName + "\"" );
        }  
        
        script = script.replaceAll( "\\" + Action.CONFIG_PARAM_SIGN_START + "OUTPUT" + "\\" + Action.CONFIG_PARAM_SIGN_END, currentActionName );
        actionJsEngine.runScript( "temp", "''", false, "temp = " + script );
        return actionJsEngine.getStringValue( "temp" );
    }
    
    
}
