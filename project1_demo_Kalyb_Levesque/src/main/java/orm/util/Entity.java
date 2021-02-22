package orm.util;

import orm.annotations.Id;

import java.lang.reflect.Type;

public class Entity {
    private Type tableName;

    public Entity(Type tableName){
        if(tableName.getTypeName() == null){
            throw new IllegalStateException("Cannot create Entity object! Provided field, " + getName() + "is not annotated with @Id");
        }
        this.tableName = tableName;
    }

    public String getName(){
        return tableName.getTypeName();
    }

//    public Class getType(){
//        return tableName;
//    }

//    public String getColumnName(){
//        return tableName.getAnnotation(Id.class).columnName();
//
//    }
}
