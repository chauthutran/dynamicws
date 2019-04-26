package psi.ws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import psi.ws.exception.ActionException;

public class AppConfigUtil
{
    // -------------------------------------------------------------------------
    // Getting username / password to access server from config.json file
    // -------------------------------------------------------------------------

    public static JSONObject readConfigFile( String fileName, ServletContext serverContext ) throws ActionException
    {
        JSONObject configJson = AppConfigUtil.jsonConfigLoad( fileName, serverContext );
        return configJson;
    }

    public static JSONObject jsonConfigLoad( String fileName, ServletContext servletContext ) throws ActionException
    {
        File configFile = null;
        JSONObject configJson = null;
        String configFilePath = servletContext.getRealPath( "/" + fileName );
        configFile = new File( configFilePath );

        if ( configFile.isFile() )
        {
            String configJsonStr = readFile( configFilePath );
            configJsonStr = configJsonStr.substring(configJsonStr.indexOf("{"));
            configJson = new JSONObject( configJsonStr.trim() );
        }
        
        return configJson;
    }
    
    
    public static FileReader getJsFileReader( String fileName, ServletContext servletContext ) throws ActionException
    {
        String configFilePath = servletContext.getRealPath( "/" + fileName );
        File jsLibFile = new File( configFilePath );
        try
        {
            return new FileReader( jsLibFile );
        }
        catch ( FileNotFoundException e )
        {
            throw new ActionException( "The file " + fileName + " not found" );
        }
    }
    
    
    // -------------------------------------------------------------------------
    // Supportive Methods
    // -------------------------------------------------------------------------

    private static String readFile( String path ) throws ActionException
    {   
        String jsonText = null;
        try
        {
            jsonText = IOUtils.toString( new FileInputStream( new File( path ) ) );
            int i = jsonText.indexOf( "{" );
            jsonText = jsonText.substring( i );
        }
        catch ( IOException e )
        {
            throw new ActionException( "The file " + path + " not found" );
        }
        return jsonText;
    }
}
