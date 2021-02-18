import orm.model.User;
import orm.repos.Repo;
import orm.util.*;
import sun.rmi.transport.Connection;

import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Driver {


    public static void main(String[] args) throws SQLException {
        Configuration cfg = new Configuration();
            cfg.addAnnotatedClass(User.class);

//            for(Metamodel<?> metamodel : cfg.getMetamodels()){
//                System.out.printf("Printing metamodel for class: %s\n", metamodel.getClassName());
//                //Entity entity = metamodel.getClass();
//                IdField idField = metamodel.getPrimaryKey();
//                List<ColumnField> columnFields= metamodel.getColumns();
//                List<ForeignKeyField> foreignKeyFields = metamodel.getForeignKeys();
//
//                System.out.printf("\tFound a primary key field named %s of type %s, which maps to the column with the name: %s\n",
//                        idField.getName(), idField.getType(), idField.getColumnName());
//
//                for(ColumnField columnField: columnFields){
//                    System.out.printf("\tFound a column key field named %s of type %s, which maps to the column with the name: %s\n",
//                            columnField.getName(), columnField.getType(), columnField.getColumnName());
//                }
//
//                for (ForeignKeyField foreignKeyField: foreignKeyFields){
//                    System.out.printf("\tFound a foreign key field named %s of type %s, which maps to the column with the name: %s\n",
//                            foreignKeyField.getName(), foreignKeyField.getType(), foreignKeyField.getColumnName());
//                }
//                System.out.println();
//            }

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
            // want to test connection to the database
        // this will create the connection pool I have connectionPool with an intial size of 10
//        ConnectionPool connecitonPool = ConnectionPooling.Create(
//                props.getProperty("url"),
//                props.getProperty("admin-usr"),
//                props.getProperty("admin-pw")
//        );

        ConnectionPooling connecitonPool = new ConnectionPooling(props);
        //connecitonPool.
        Repo repo = new Repo();
        //repo.findRecordBy(props,"first_name");
        //System.out.println(repo.findRecordBy(props,"first_name"));

        User user = repo.findRecordBy(connecitonPool,props,"first_name");
        System.out.println(user);



    }
}
