package orm.repos;

import orm.model.User;
import orm.util.ConnectionPool;
import orm.util.ConnectionPooling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Repo implements CRUDrepo {

    /**
     * need to generify it!!!
     * @param props
     * @param where
     * @return
     */
    public User findRecordBy(ConnectionPool conn, Properties props, String where){
        User record = null;

        try {
//            ConnectionPool connectionPool = ConnectionPooling.Create( props.getProperty("url"),
//                    props.getProperty("admin-usr"),
//                    props.getProperty("admin-pw"));

        //            String sql = "SELECT * FROM proj1_test " + "Where proj1_test.first_name = ?";
            PreparedStatement pstmt = conn.getConnection().prepareStatement("SELECT * FROM proj1_test ");
//                               + "Where proj1_test.first_name = ?");
           // pstmt.setString(1,where);
            ResultSet rs = pstmt.executeQuery();
           // record = mapResultSet(rs).pop();
            record = mapResultSet(rs).pop();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return record;
    }

    // basically insert
    @Override
    public void save(Object newObj) {
//        Connection conn = SessionFactory.getInstance().getConnection();

        // insert the fields of the object into the table
    }

//    public LinkedList<Object> findAllFromTableGiven(String table){
//        Connection conn = SessionFactory.getInstance().getConnection();
//
//    }
//
//    private LinkedList<Object> mapResultSet(ResultSet rs,Object provided){
//        LinkedList<Object> tests = new LinkedList<>();
//        while(rs.next()){
//
//        }
//    }

    /**
     * display all records for CRUD read operation
     * @return
     */
    public static <T> List<T> displayRecords(){
        List<T> studentsList = new ArrayList();
        try{
            // get the connection to acesss the database


        }catch (Exception e){
            e.printStackTrace();
        }

        return studentsList;
    }

    /**
     *
     * @param where
     * @param <T>
     * @return
     */
    public static <T> T findRecord(T where){
        T findObject = null;



        return findObject;
    }

    /**
     *
     */
    public static void deleteAllRecords(){
        // get the connection pool

        // figure out how to get the entity table name so you can
        // generically sql delete from that table

    }

    public static void updateRecord(){

    }



    // implement this later!!!!

    /**
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    private LinkedList<User> mapResultSet(ResultSet rs) throws SQLException{

        LinkedList<User> users = new LinkedList<>();

        while(rs.next()){
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setEmailAddress(rs.getString("email_address"));
            users.add(user);

        }

        return users;
    }


    /**
     *
     * @param rs
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T> LinkedList<T> mapResultSetGeneric(ResultSet rs) throws SQLException{

        LinkedList<T> gen = new LinkedList<>();

        while(rs.next()){
            // gotta find how to generify this one
//            T genericModel = new User();
//            user.setId(rs.getInt("id"));
//            user.setFirstName(rs.getString("first_name"));
//            user.setLastName(rs.getString("last_name"));
//            user.setEmailAddress(rs.getString("email_address"));
//            gen.add(user);

        }

        return gen;
    }
}
