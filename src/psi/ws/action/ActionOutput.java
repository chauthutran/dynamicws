package psi.ws.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.json.JSONObject;

public class ActionOutput
{
    private int responseCode = 200;

    private String actionEval;
    private String outputMsg = "";
    ScriptEngine jsEngine;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    
    public ActionOutput()
    {
        super();
    }
    
    public ActionOutput( ScriptEngine jsEngine )
    {
        super();
        this.jsEngine = jsEngine;
    }

    // -------------------------------------------------------------------------
    // Getters && Setters
    // -------------------------------------------------------------------------

    public int getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode( int responseCode )
    {
        this.responseCode = responseCode;
    }

    public String getOutputMsg()
    {
        return this.outputMsg;
    }

    public void setOutputMsg( String outputMsg )
    {
        this.outputMsg = outputMsg;
    }

    public String getActionEval()
    {
        return actionEval;
    }

    public void setActionEval( String actionEval )
    {
        this.actionEval = actionEval;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public JSONObject getOutputJson()
    {
        return new JSONObject( this.outputMsg );
    }
    
    public void evalOutput() throws FileNotFoundException
    {
        if( !this.outputMsg.isEmpty() && this.actionEval != null )
        {
            String result = null;

            try
            {
                String script = this.actionEval;
                script = script.replaceAll( "%%OUTPUT%%", "output" );
                script = "var f = { data: '" + this.outputMsg
                    + "',run: function(){ var output = JSON.parse(this.data); " + script
                    + " this.data = JSON.stringify(output); } }; f";
                  
//                ScriptEngine engine = new ScriptEngineManager().getEngineByName( "JavaScript" );
//                //Pass the script file to the engine
//                Reader jsFileReader = new FileReader( this.jsEngine );
//                engine.eval( jsFileReader );
                
                ScriptObjectMirror obj = (ScriptObjectMirror) this.jsEngine.eval( script );
                obj.callMember( "run" );
                result = obj.getMember( "data" ).toString();

            }
            catch ( ScriptException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            this.outputMsg = result;
        }
    }
}
