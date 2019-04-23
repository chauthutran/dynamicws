package psi.ws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class AppConfigUtil
{
    // -------------------------------------------------------------------------
    // Getting username / password to access server from config.json file
    // -------------------------------------------------------------------------

    public static JSONObject readConfigFile( String fileName, ServletContext serverContext )
        throws ServletException
    {
        JSONObject configJson = AppConfigUtil.jsonConfigLoad( fileName, serverContext );
        return configJson;
    }

    public static JSONObject jsonConfigLoad( String fileName, ServletContext servletContext )
    {
        File configFile = null;
        JSONObject configJson = null;
        String configFilePath = servletContext.getRealPath( "/" + fileName );
        
        try
        {
            configFile = new File( configFilePath );

            if ( configFile.isFile() )
            {
                String configJsonStr = readFile( configFilePath );
                configJsonStr = configJsonStr.substring(configJsonStr.indexOf("{"));
                configJson = new JSONObject( configJsonStr.trim() );
            }
       
        }
        catch ( Exception ex )
        {
            System.out.println( "Config file name with '" + configFilePath + "' not found." );
            ex.printStackTrace();
            // throw ex;
        }
        
        return configJson;
    }
    
    
    public static File getJsFile( String fileName, ServletContext servletContext )
    {
        File configFile = null;

        try
        {
            String configFilePath = servletContext.getRealPath( "/" + fileName );
            //Create file for reading the script file
            configFile = new File( configFilePath );
        }
        catch ( Exception ex )
        {
            System.out.println( "Util JS file name with '" + fileName + "' not found." );
            ex.printStackTrace();
            // throw ex;
        }
        
        return configFile;
    }
    
    
    // -------------------------------------------------------------------------
    // Supportive Methods
    // -------------------------------------------------------------------------

    private static String readFile( String path )
        throws IOException
    {   
        String jsonText = null;
        try {
            jsonText = IOUtils.toString(new FileInputStream(new File(path)));
            int i = jsonText.indexOf("{");
            jsonText = jsonText.substring(i);
//            JSONObject jsonFile = new JSONObject(jsonText);
//            System.out.println("Input JSON data: "+ jsonFile.toString());
//            Object result = jsonFile.get("result");
//            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonText;
        
        
//        byte[] encoded = Files.readAllBytes( Paths.get( path ) );
//        return new String( encoded );
    }
}
