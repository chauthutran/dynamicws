package psi.ws.configuration;

public class WebServiceConfiguration
{
    private String server;
    private String username;
    private String password;
    
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public WebServiceConfiguration( String server, String username, String password )
    {
        this.server = server;
        this.username = username;
        this.password = password;
    }

    
    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getServer()
    {
        return server;
    }

    public String getUsername()
    {
        return username;
    }
    
    public String getPassword()
    {
        return password;
    }
    
}
