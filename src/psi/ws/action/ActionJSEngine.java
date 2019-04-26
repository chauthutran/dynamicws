package psi.ws.action;

import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import psi.ws.exception.ActionException;
import psi.ws.util.AppConfigUtil;
import psi.ws.util.Util;

public class ActionJSEngine
{
    private ScriptEngine jsEngine;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ActionJSEngine( HttpServletRequest request )
        throws ActionException, ScriptException
    {
        setUpJSEngine( request );
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public void addVariable( String name, JSONObject value )
        throws ActionException
    {
        try
        {
            this.jsEngine.eval( "var " + name + " = " + value.toString() + ";" );
        }
        catch ( ScriptException e )
        {
            throw new ActionException( "Error when to run actionEval script" );
        }
    }

    public void runScript( String variableName, String initValue, boolean isNewVariable, String script )
        throws ActionException
    {
        try
        {
            // Add/Set variables with name as actionName and value as outputStr
            // so that we can reuse this in any action definition
            if ( isNewVariable )
            {
                this.jsEngine.eval( "var " + variableName + " = " + initValue + ";" );
            }
            else
            {
                this.jsEngine.eval( variableName + " = " + initValue + ";" );
            }

            script = script.replaceAll( "\\" + Action.CONFIG_PARAM_SIGN_START + "OUTPUT" + "\\"
                + Action.CONFIG_PARAM_SIGN_END, variableName );

            this.jsEngine.eval( script );
        }
        catch ( ScriptException e )
        {
            throw new ActionException( "Error when to run actionEval script" );
        }
    }

    public JSONObject getJSONValue( String variableName )
        throws ActionException
    {
        try
        {
            String value = this.jsEngine.eval( "JSON.stringify(" + variableName + ")" ).toString();
            return new JSONObject( value );
        }
        catch ( ScriptException e )
        {
            throw new ActionException( "Error when to get JSON variable name " + variableName );
        }
    }

    public String getStringValue( String variableName )
        throws ActionException
    {
        try
        {
            return this.jsEngine.eval( variableName ).toString();
        }
        catch ( ScriptException e )
        {
            throw new ActionException( "Error when to get String variable name " + variableName );
        }
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void setUpJSEngine( HttpServletRequest request )
        throws ActionException, ScriptException
    {
        // Create file and reader instance for reading the script file
        Reader jsFileReader = AppConfigUtil
            .getJsFileReader( Util.UTIL_JAVASCRIPT_LIB_FILE, request.getServletContext() );
        this.jsEngine = new ScriptEngineManager().getEngineByName( "JavaScript" );

        // Pass the script file to the engine
        this.jsEngine.eval( jsFileReader );
        this.jsEngine.eval( "var temp = '';" );
    }

}
