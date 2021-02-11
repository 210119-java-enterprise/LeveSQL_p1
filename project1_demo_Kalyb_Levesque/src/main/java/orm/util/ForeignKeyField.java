package orm.util;

import orm.annotations.JoinColumn;

import java.lang.reflect.Field;

public class ForeignKeyField {
    private Field field;

    public ForeignKeyField(Field field){
        if(field.getAnnotation(JoinColumn.class) == null){
            throw new IllegalStateException("Cannot create ForeignKeyField object! provided field, "
                    + "is not annotated with @JoinColumn");
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
        return field.getAnnotation(JoinColumn.class).columnName();
    }
}
