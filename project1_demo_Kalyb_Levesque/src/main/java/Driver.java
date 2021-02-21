import orm.exceptions.SelectException;
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

        // try to run the select statement with the strings passed in for column names
        try {
            resultSelection = user.selection("last_name").validateAndRunSelection();
        } catch (SelectException e) {
            e.printStackTrace();
        }
        // get a reference to the user object model
        // then when you remove the object from the list of objects you can assign the
        // reference and use it to access the get methods
        User u;
        for(int i = 0; i < resultSelection.size(); i++){
            u = (User) resultSelection.remove(i);
        //    System.out.println(u.getEmailAddress());
        //    System.out.println(u.getLastName() + " " + u.getId());
        }
    }
}
