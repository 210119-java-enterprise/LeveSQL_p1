package orm.util;

import orm.annotations.Column;
import orm.annotations.Id;

import java.lang.reflect.Field;

public class IdField {
    private Field field;

    public IdField(Field field){
        if(field.getAnnotation(Id.class) == null){
            throw new IllegalStateException("Cannot create IdField object! Provided field, " + getName() + "is not annotated with @Id");
        }
    }

    public String getName(){
        return field.getName();
    }

    public Class getType(){
        return field.getType();
    }

    public String getColumnName(){
        return field.getAnnotation(Id.class).columnName();

    }
}
