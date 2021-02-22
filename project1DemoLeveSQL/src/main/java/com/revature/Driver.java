package com.revature;

import com.revature.models.DemoUser;
import com.revature.models.Job;
import orm.exceptions.DeleteException;
import orm.exceptions.InsertionException;
import orm.exceptions.SelectException;
import orm.util.ConnectionPooling;
import orm.util.Metamodel;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class Driver {
    public static void main(String[] args) {
        Properties props = new Properties();
        ConnectionPooling pool = null;
        ArrayList result = new ArrayList();
        try{
            props.load(new FileReader("src/main/resources/application.properties"));
            pool = new ConnectionPooling(props).Create();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        DemoUser user = new DemoUser();
        Job job = new Job();
        Metamodel<DemoUser> demoUser = new Metamodel<>(DemoUser.class,pool);
        Metamodel<Job> demoJob = new Metamodel<>(Job.class,pool);

        try {
            demoUser.insertion("first_name","last_name","email_address").insertionValues("kalyb","levesque","dummy@dumdum.com").validateAndRunInsertion();
            demoUser.deletion().validateAndRunDeletion();
            result = demoUser.selection().validateAndRunSelection();
        } catch (InsertionException | SQLException | SelectException | DeleteException e) {
            e.printStackTrace();
        }

        // the selection statement returns a list of objects
        // the for loop will print out the fields except the primary key

        for(int i = 0; i < result.size(); i++){
            user = (DemoUser) result.remove(i);
            System.out.println(user.getFirstName() + " "
                        +user.getLastName() + " "
                        +user.getEmail());
        }

        try {
            demoJob.insertion("job","salary")
                    .insertionValues("IT","30000").validateAndRunInsertion();
            //demoJob.deletion().validateAndRunDeletion();
            result = demoJob.selection().validateAndRunSelection();
        } catch (InsertionException | SQLException | SelectException  e) {
            e.printStackTrace();
        }

        for(int i = 0; i < result.size(); i++){
            job = (Job) result.remove(i);
            System.out.println(job.getJob() + " "
                    +job.getSalary());
        }
    }
}
