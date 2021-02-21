package orm.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Configuration {
    private String url;
    private String username;
    private String password;
    private List<Metamodel<Class<?>>> metamodelList;

//    public Configuration addAnnotatedClass(Class annotatedClass){
//
//        if(metamodelList == null){
//            metamodelList = new LinkedList<>();
//        }
//
//        metamodelList.add(Metamodel.of(annotatedClass));
//
//        return this;
//    }

    public List<Metamodel<Class<?>>> getMetamodels(){
        return (metamodelList == null) ? Collections.emptyList() : metamodelList;
    }
}
