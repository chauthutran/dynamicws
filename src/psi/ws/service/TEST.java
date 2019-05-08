package psi.ws.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import org.json.JSONArray;
import org.json.JSONObject;

import psi.ws.action.Action;
import psi.ws.action.ActionGoTo;
import psi.ws.action.ActionInput;
import psi.ws.action.ActionRequest;
import psi.ws.action.request.ActionWebServiceRequest;
import psi.ws.util.Util;

import com.jayway.jsonpath.JsonPath;


public class TEST
{

    public static void main( String[] args ) throws FileNotFoundException, ScriptException
    {
//        // RUN javascript code   
//        JSONObject data = new JSONObject();
//        JSONObject response = new JSONObject();
//        response.put( "status", "SUCCESS" );
//        response.put( "reference", "referenceId" );
//        data.put( "response", response );
//       
//        try
//        {
//
//           // JSON.stringify
//          String script = "{OUTPUT}.trackedEntityInstance = 'fadsfa';";
//          String output = data.toString();
//          script = script.replaceAll( "\\{OUTPUT\\}", "output" );
//          script = "var f = { data: '" + output + "',run: function(){ var output = JSON.parse(this.data); " + script + " this.data = JSON.stringify(output); } }; f"; 
//          System.out.println("\n actionVal : " + script );
//
//
//        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//        ScriptObjectMirror obj = (ScriptObjectMirror)engine.eval(script);
//        System.out.println("obj.run = " + obj.callMember("run") );
//        System.out.println("obj.data = " + obj.getMember("data"));
//                     
//            
//        }
//        catch ( ScriptException e )
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
       
        

//      JSONObject data = new JSONObject();
//      JSONObject response = new JSONObject();
//      response.put( "status", "SUCCESS" );
//      response.put( "trackedEntityInstance", "referenceId" );
//      data.put( "response", response );
//      data.put( "uid", "testUID");
//      
////  JsonPath.read(response, "$.trackedEntityInstance");
//    
////String script = "{OUTPUT}.trackedEntityInstance = 'fadsfa';";
////script = script.replaceAll( "\\{OUTPUT\\}", "output" );
//      System.out.println(data.toString());
////String script = "var f = { data: '" + data.toString() + "',run: function( valuePath ){ var output = JSON.parse(this.data); var variables = output.split(".");for( var i=0;i<variables.length;i ++ ){ output = this.data[variables[i]]; this.data = output; } };f"; 
////    String script = "var f = { data: '" + data.toString() + "',run: function(variables){ var output = JSON.parse(this.data);var variables = output.split('.'); for( var i=0;i<variables.length;i ++ ){ output = this.data[variables[i]]; this.data = output } } }; f"; 
//      String script = "var f = { data: '" + data.toString() + "',run: function(variables){ this.data = variables; } }; f"; 
//
//  ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//  ScriptObjectMirror obj;
//try
//{
//    obj = (ScriptObjectMirror)engine.eval( script );
//    System.out.println("obj.run = " + obj.callMember("run", "response.trackedEntityInstance") );
//    System.out.println("obj.data = " + obj.getMember("data"));
//}
//catch ( ScriptException e )
//{
//    // TODO Auto-generated catch block
//    e.printStackTrace();
//}
  

        
//        // Get parameters from another action
//        
//        JSONObject store = new JSONObject();
//        JSONObject data = new JSONObject();
//        
//        JSONObject book1 = new JSONObject();
//        book1.put( "category", "reference" );
//        book1.put( "author", "Nigel Rees" );
//        book1.put( "title", "Sayings of the Century" );
//        book1.put( "price", 8.95 );
//
//        JSONObject book2 = new JSONObject();
//        book2.put( "category", "fiction" );
//        book2.put( "author", "Evelyn Waugh" );
//        book2.put( "title", "Sword of Honour" );
//        book2.put( "price", 12.99 );
//        
//        JSONArray books = new JSONArray();
//        books.put( book1 );
//        books.put( book2 );
//        
//        JSONObject bicycle = new JSONObject();
//        bicycle.put( "color", "red" );
//        bicycle.put( "price", 19.95 );
//        
//        data.put( "book", books );
//        data.put( "bicycle", bicycle );
//        
//        store.put( "store", data );
//        store.put( "expensive", 10 );
//        
//        String response = JsonPath.read(store.toString() , "$.store.book[0]").toString();
//        System.out.println( response );
        
//        
//       JSONObject store = new JSONObject();
//      JSONObject data = new JSONObject();
//      
//      JSONObject book1 = new JSONObject();
//      book1.put( "category", "reference" );
//      book1.put( "author", "Nigel Rees" );
//      book1.put( "title", "Sayings of the Century" );
//      book1.put( "price", 8.95 );
//
//      JSONObject book2 = new JSONObject();
//      book2.put( "category", "fiction" );
//      book2.put( "author", "Evelyn Waugh" );
//      book2.put( "title", "Sword of Honour" );
//      book2.put( "price", 12.99 );
//      
//      JSONArray books = new JSONArray();
//      books.put( book1 );
//      books.put( book2 );
//      
//      JSONObject bicycle = new JSONObject();
//      bicycle.put( "color", "red" );
//      bicycle.put( "price", 19.95 );
//      
//      data.put( "book", books );
//      data.put( "bicycle", bicycle );
//      
//      store.put( "store", data );
//      store.put( "expensive", 10 );
////      
//        String url = "/api/trackedEntityInstances/{actionId_1.expensive}/{actionId_2.store.book[0].category}";
//        String resultURL = url;
//        Pattern pattern = Pattern.compile( "\\{(\\w+)(\\..[^\\}]+)+\\}" );
//        Matcher matcher = pattern.matcher( url );
//        // check all occurance
//        while (matcher.find()) {
//            int groupCount = matcher.groupCount();
//            
////          System.out.println("matcher.group(" + i + ") " + matcher.group(i));
//            
//            String match = matcher.group();
//            
//            // Get actionId
//            String actionId = matcher.group( 1 );
//            
//            // Get jsonPatch
//            String jsonPatch = match.replace( actionId + ".", "" ).replace("{","").replace("}", "");
//System.out.println(" match : " + match );
//System.out.println(" jsonPatch : " + jsonPatch );
//            
//            // Get value from actionId and jsonPath
//            String value = JsonPath.read( store.toString() , "$." + jsonPatch ).toString();
//System.out.println(" value : " + value );
//            resultURL = resultURL.replace( match, value );
//            
//        }
//        
//       System.out.println("resultURL : " + resultURL );
        
        
        
        
        
        
//        
//       ///////////////////////////////////////////////// 
//        
//        String result = null;
//
//        try
//        {
//            
//
//          JSONObject data = new JSONObject();
//          JSONObject response = new JSONObject();
//          response.put( "status", "SUCCESS" );
//          response.put( "trackedEntityInstance", "referenceId" );
//          data.put( "response", response );
//          data.put( "uid", "testUID");
//          
//    //  JsonPath.read(response, "$.trackedEntityInstance");
//        
//    //String script = "{OUTPUT}.trackedEntityInstance = 'fadsfa';";
//    //script = script.replaceAll( "\\{OUTPUT\\}", "output" );
//          System.out.println(data.toString());
//    //String script = "var f = { data: '" + data.toString() + "',run: function( valuePath ){ var output = JSON.parse(this.data); var variables = output.split(".");for( var i=0;i<variables.length;i ++ ){ output = this.data[variables[i]]; this.data = output; } };f"; 
////        String script = "var f = { data: '" + data.toString() + "',run: function(variables){ var output = JSON.parse(this.data);var variables = output.split('.'); for( var i=0;i<variables.length;i ++ ){ output = this.data[variables[i]]; this.data = output } } }; f"; 
//          String script = "var f = { data: '" + data.toString() + "',run: function(){ var yesterday = Util.getLastNDate(1); yesterday = Util.formatDateObj_DbDateTime(yesterday); this.data = yesterday } }; f"; 
//    
//      ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
//
//    //Create file and reader instance for reading the script file
//    File file = new File("c:\\Work\\DHIS\\Softwares\\Apache Software Foundation\\apache-tomcat-8.0.9\\webapps\\dynamicws\\js\\util.js");
//    Reader reader = new FileReader(file);
//      //Pass the script file to the engine
//      engine.eval(reader);
//      
//      ScriptObjectMirror obj;
//    
//        obj = (ScriptObjectMirror)engine.eval( script );
//        System.out.println("obj.run = " + obj.callMember("run") );
//        System.out.println("obj.data = " + obj.getMember("data"));
//        
//
//        System.out.println("Java Program Output");
//        
//    }
//    catch ( ScriptException e )
//    {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//       /////////////////////
        
       

////        ActionRequest request = new ActionDhisRequest( "serverName", "link" );
////        System.out.println( request.getClass().getSimpleName());
//
//        String regExp = "\\{\\s*%%(\\w+)%%\\s*}";
//        String goToStr = "( %%OUTPUT%%.status == \"SUCCESS\" ) ? {%%2_ClientGet%%} : {%%5_END%%}";
//        
//        String script = goToStr;
//        Pattern pattern = Pattern.compile( regExp );
//        Matcher matcher = pattern.matcher( goToStr );
//        while( matcher.find() ) 
//        {
//            String match = matcher.group(0);
//            String actionName = matcher.group(1);
//            script = script.replace( match, "\"" + actionName + "\"" );
//        }     
//        JSONObject response = new JSONObject();
//        response.put( "status", "SUCCESS" );
//        
//        String result = null;
//        try
//        {
//            script = script.replaceAll( "%%OUTPUT%%", "output" );
//            script = "var f = { next: '\',"
//                + " run: function(){ var output = JSON.parse('" + response.toString() + "'); var nextAction = " + script + ";"
//                + " this.next = nextAction } }; f";
//
//            ScriptEngine engine = new ScriptEngineManager().getEngineByName( "JavaScript" );
//            ScriptObjectMirror obj = (ScriptObjectMirror) engine.eval( script );
//            obj.callMember( "run" );
//            result = obj.getMember( "next" ).toString();
//            
//        }
//        catch ( ScriptException e )
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
//        String output = "[OUTPUT].response.status &&  [OUTPUT].success ";
//          String  script = output.replaceAll( "\\" + Action.CONFIG_PARAM_SIGN_START + "OUTPUT" + "\\" + Action.CONFIG_PARAM_SIGN_END, "output" );
//          System.out.println( "script 1 : " + script );
////            script = "var f = { data: '" + output
////                + "',run: function(){ var output = JSON.parse(this.data); " + script
////                + " this.data = JSON.stringify(output); } }; f";
////            
////            ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
////            
////                ScriptObjectMirror obj = (ScriptObjectMirror) jsEngine.eval( script );
////  System.out.println(" script : " + script );
////            obj.callMember( "run" );
////            System.out.println( obj.getMember( "data" ).toString() );
        
//        String actionEval = " action_2_ClientGet.tested = 'fadsf';";
//
//        ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
//        jsEngine.eval("var action_2_ClientGet = {}" );
//        jsEngine.eval(actionEval);
//    
////        ScriptObjectMirror obj = (ScriptObjectMirror) jsEngine.eval( actionEval );
//        
//        
//        System.out.println( jsEngine. );
        
//        String outputStr = "var action_2_ClientGet = {}";
//        String actionEval = "";
//        
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("js");
//        
////        engine.eval("var clientGet = ''" );
////      engine.eval( "clientGet = 'fadsf';" );
//      
//        engine.eval("var _1_ClientCreate = {\"status\":\"SUCCESS\"}" );
//        engine.eval( "_1_ClientCreate.tested = 'fadsf';" );
//        engine.eval( "_1_ClientCreate_goTo = ( _1_ClientCreate.status == \"SUCCESS\" ) ? \"2_ClientGet\" : \"5_END\";" );
//
////        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
////        Object a = bindings.get("a");
////        Object b = bindings.get("b");
////        System.out.println("a = " + a);
////        System.out.println("b = " + b);
//
////        Object result = engine.eval("c = a + b;"); 
////        Object result = engine.eval("c = a + b;");
//        
////        Object data = bindings.get( "clientGet" );
////        System.out.println( data.getClass().getName() );
//      
//      Object data1 = engine.eval("JSON.stringify(_1_ClientCreate)");
//      Object data2 = engine.eval("_1_ClientCreate_goTo");
//      
//        System.out.println(  data1 );
//        System.out.println(  data2 );
        
        
        
    
        
        
        }
        
    }
