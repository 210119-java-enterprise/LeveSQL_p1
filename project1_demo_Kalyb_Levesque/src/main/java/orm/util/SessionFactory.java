package orm.util;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SessionFactory {
//    package com.revature.util;

//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;


    /**
     *  make a connection to the database, constructor should take information from the properties file
     *  then be able to use that to get a connection
     */

        private static SessionFactory sessionFactory = new SessionFactory();


        private Properties props = new Properties();


        /**
         *      load and register the driver
         */
        static {
            try{
                Class.forName("org.postgresql.Driver");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        /**
         *      try to load properties from the properties file: such as the database url, database username, and database password
         */
        private SessionFactory(){
            try{
                props.load(new FileReader("Project0/src/main/resources/application.properties"));
            }catch (IOException e){
                e.printStackTrace();
            }

        }


        /**
         *
         * @return return the instance of the connection.  static so only once can exist
         */
        public static SessionFactory getInstance(){
            return sessionFactory;
        }


        /**
         *
         * @return get the connection to the datebase using the properties from the properties file
         */
        public Connection getConnection(){
            //    System.out.println("Trying to get connection");
            Connection conn = null;
            try{
                // creating the connection
                conn = DriverManager.getConnection(
                        props.getProperty("url"),
                        props.getProperty("admin-usr"),
                        props.getProperty("admin-pw")
                );
            } catch (SQLException e){
                e.printStackTrace();
            }

            return conn;
        }

    }

