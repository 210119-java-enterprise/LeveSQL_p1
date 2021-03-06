package orm.util;

import orm.annotations.Column;
import orm.annotations.Id;


import java.lang.reflect.Field;

public class ColumnField {
    private Field field;

    public ColumnField(Field field){
        if(field.getAnnotation(Column.class) == null){
            throw new IllegalStateException("Cannot create ColumnField object! Provided field, " + getName() + "is not annotated with @Column");
        }
        this.field = field;
    }

    public String getName(){
        return field.getName();
    }

    public Class<?> getType(){
        return field.getType();
    }

    public String getColumnName(){
        return field.getAnnotation(Column.class).columnName();
    }

}
