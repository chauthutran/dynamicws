package psi.ws.configuration;


public class MongodbConfiguration
{
    private String username;
    private String password;
    private String clusterUrl;
    private String dbName;
    private String collectionName;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public MongodbConfiguration( String username, String password, String clusterUrl, String dbName, String collectionName )
    {
        this.username = username;
        this.password = password;
        this.clusterUrl = clusterUrl;
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getDbName()
    {
        return dbName;
    }

    public String getCollectionName()
    {
        return collectionName;
    }

    public String getClusterUrl()
    {
        return clusterUrl;
    }
   
}
