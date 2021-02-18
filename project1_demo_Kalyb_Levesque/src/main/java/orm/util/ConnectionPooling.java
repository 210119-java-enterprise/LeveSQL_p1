package orm.util;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConnectionPooling implements ConnectionPool{
    private String url;
    private String user;
    private String password;
    private List<Connection> connectionPool;
    private List<Connection> usedConnections = new ArrayList<>();
    private static int INITIAL_POOL_SIZE = 10;

    private static Properties props;

    public ConnectionPooling(Properties properties) {
        props = properties;
        try {
            Create();
        } catch (SQLException e) {
            System.out.println("Something happened trying to Create!!");
            e.printStackTrace();
        }
    }
    public ConnectionPooling(String url, String user, String password, List<Connection> connectionPool) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.connectionPool = connectionPool;
    }

    public ConnectionPooling Create() throws SQLException {
        List<Connection> pool = new ArrayList<>(INITIAL_POOL_SIZE);
        for(int i = 0; i < INITIAL_POOL_SIZE; i++){
            pool.add(createConnection());
        }
        return new ConnectionPooling(props.getProperty("url"),props.getProperty("admin-usr"),props.getProperty("admin-pw"),pool);
    }

    /**
     *      load and register the driver
     */
//    static {
//        try{
//            Class.forName("org.postgresql.Driver");
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//    }



    // create a connection using the driverManager
    public static Connection createConnection( ) throws SQLException{
        return DriverManager.getConnection(props.getProperty("url"),props.getProperty("admin-usr"),props.getProperty("admin-pw"));
    }

    @Override
    public Connection getConnection() {
        Connection conn = connectionPool.remove(connectionPool.size()-1);
        usedConnections.add(conn);
        return conn;
    }

    @Override
    public boolean releaseConnection(Connection connection) {
        connectionPool.add(connection);
        return usedConnections.remove(connection);

    }

    public int getSize(){
        return connectionPool.size() + usedConnections.size();
    }

//    /**
//     *      try to load properties from the properties file: such as the database url, database username, and database password
//     */
//    private ConnectionPooling(){
//        try{
//            props.load(new FileReader("Project0/src/main/resources/application.properties"));
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }
    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
