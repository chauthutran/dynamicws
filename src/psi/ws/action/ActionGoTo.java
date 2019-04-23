package psi.ws.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.json.JSONArray;

// "goTo" : ( %%OUTPUT%%.response.status == \"SUCCESS\" ) ? {%%2_ClientGet%%} : {%%5_END%%}
public class ActionGoTo
{
    private static final String REGEXP_ACTIONNAME = "\\{\\s*%%(\\w+)%%\\s*}";
    
    private String goTo;
    private int currentIndex;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public ActionGoTo( String goTo )
    {
        this.goTo = goTo;
    }
    
    public ActionGoTo( int currentIndex )
    {
        this.currentIndex = currentIndex;
    }
    
    public void setGoToDefination( String goTo )
    {
        this.goTo = goTo;
    }
    
    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    public String getNextActionName( JSONArray actionConfigList, ActionOutput output )
    {
        if( this.goTo != null )
        {
            String nextActionName = this.getNextActionName( output );
            for( int i = 0; i< actionConfigList.length(); i++ )
            {
                String searchedName = actionConfigList.getJSONObject( i ).getString( "name" );
                if( searchedName.equals( nextActionName ) )
                {
                    return searchedName;
                }
            }
        }
        else if( this.currentIndex < actionConfigList.length() )
        { 
            int next = this.currentIndex + 1;
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
     * @script ( %%OUTPUT%%.response.status == \"SUCCESS\" ) ? {%%2_ClientGet%%} : {%%5_END%%}
     * @return "2_ClientGet" OR "5_END"
     * **/
    private String getNextActionName( ActionOutput output )
    {
        String nextActionName = null;
        
        String script = this.goTo;
        Pattern pattern = Pattern.compile( REGEXP_ACTIONNAME );
        Matcher matcher = pattern.matcher( this.goTo );
        while( matcher.find() ) 
        {
            String match = matcher.group(0);
            String actionName = matcher.group(1);
            script = script.replace( match, "\"" + actionName + "\"" );
        }     
        try
        {
            script = script.replaceAll( "%%OUTPUT%%", "output" );
            script = "var f = { next: '\',"
                + " run: function(){ var output = JSON.parse('" + output.getOutputMsg() + "'); var nextAction = " + script + ";"
                + " this.next = nextAction } }; f";

            ScriptEngine engine = new ScriptEngineManager().getEngineByName( "JavaScript" );
            ScriptObjectMirror obj = (ScriptObjectMirror) engine.eval( script );
            obj.callMember( "run" );
            nextActionName = obj.getMember( "next" ).toString();
 
        }
        catch ( ScriptException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return nextActionName;
    }
    
    
}
