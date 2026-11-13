package hr.algebra.dal.sql;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;

/**
 *
 * @author dbele
 */
public final class DataSourceSingleton {
   

    private static final String PATH = "/config/db.properties";

    private static final String SERVER_NAME = "SERVER_NAME";
    private static final String DATABASE_NAME = "DATABASE_NAME";
    private static final String USER = "USER";
    private static final String PASSWORD = "PASSWORD";
    
    private static final Properties PROPERTIES = new Properties();
    
    static {
        try (InputStream is = DataSourceSingleton.class.getResourceAsStream(PATH)){
            
            PROPERTIES.load(is);
        } catch (IOException ex) {
            System.getLogger(DataSourceSingleton.class.getName())
                    .log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }


    private DataSourceSingleton() {
    }

    private static DataSource instance;

    public static synchronized DataSource getInstance() {
        if (instance == null) {
            instance = createInstance();
        }
        return instance;
    }

    private static DataSource createInstance() {
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setServerName(PROPERTIES.getProperty(SERVER_NAME));
        dataSource.setDatabaseName(PROPERTIES.getProperty(DATABASE_NAME));
        dataSource.setUser(PROPERTIES.getProperty(USER));
        dataSource.setPassword(PROPERTIES.getProperty(PASSWORD));
        return dataSource;
    }
    
}
