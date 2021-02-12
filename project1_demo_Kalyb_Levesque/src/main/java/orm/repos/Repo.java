package orm.repos;

import orm.util.SessionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedList;

public class Repo implements CRUDrepo {


    @Override
    public void save(Object newObj) {
        Connection conn = SessionFactory.getInstance().getConnection();

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

}
