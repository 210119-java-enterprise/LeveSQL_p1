import orm.exceptions.DeleteException;
import orm.exceptions.InsertionException;
import orm.exceptions.SelectException;
import orm.exceptions.WhereClauseException;
import orm.model.User;
import orm.repos.Repo;
import orm.util.*;
import sun.rmi.transport.Connection;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Driver {


    public static void main(String[] args) throws SQLException {

        Properties props = new Properties();

            // tried to keep block of code inside
        try{
            Class.forName("org.postgresql.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // user needs to provide the filePath of the properties file
        try{
            props.load(new FileReader("project1_demo_Kalyb_Levesque/src/main/resources/application.properties"));
        }catch (IOException e){
            e.printStackTrace();
        }


        ConnectionPooling connectionPool = new ConnectionPooling(props).Create();

        // the meta model will be the interconnected piece between custom ORM and user
        // user will have to make a arrayList to hold the objects resulted
        Metamodel<User> user = new Metamodel<>(User.class, connectionPool);
        ArrayList resultSelection = new ArrayList<>();


        try {
            user.insertion("first_name", "last_name").insertionValues("testing","123").validateAndRunInsertion();
        } catch (InsertionException e) {
            e.printStackTrace();
        }
        try {
            user.deletion().initialWhere("first_name",WhereConditions.EQUALS, "testing").validateAndRunDeletion();
        } catch (WhereClauseException | DeleteException e) {
            e.printStackTrace();
        }
        try {
            resultSelection = user.selection().validateAndRunSelection();
        } catch (SelectException e) {
            e.printStackTrace();
        }

        // try to run the select statement with the strings passed in for column names
//        try {
//            resultSelection = user
//                    .selection().initialWhere("first_name",WhereConditions.EQUALS,"blah")
//                    .or("last_name",WhereConditions.EQUALS,"levesque")
//                    .validateAndRunSelection();
//        } catch (SelectException | WhereClauseException e) {
//            e.printStackTrace();
//        }
        // get a reference to the user object model
        // then when you remove the object from the list of objects you can assign the
        // reference and use it to access the get methods
        User u;

        for(int i = 0; i < resultSelection.size(); i++){
            u = (User) resultSelection.remove(i);
            System.out.println(u);
        }
    }
}
