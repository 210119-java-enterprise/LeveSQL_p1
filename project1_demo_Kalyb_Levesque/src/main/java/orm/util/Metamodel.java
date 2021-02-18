package orm.util;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.JoinColumn;
import orm.exceptions.SelectException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Metamodel<T> {
    private Class <T> clazz;
    private IdField primaryKeyField;
    private List<ColumnField> columnFields;
    private List<ForeignKeyField> foreignKeyFields;

    private Method[] methods; // used to get the methods of the model class.  used so it can dynamically use the set methods! possibly the get methods later
    private ArrayList<ColumnField> resultFields; // planning to use this to hold result set of a SELECT
    private PreparedStatement pstmt;
    private Connection conn; // pass in the connection to the metamodel class

    public static <T> Metamodel<T> of(Class<T> clazz){
        if (clazz.getAnnotation(Entity.class) == null) {
            throw new IllegalStateException("Cannot create Metamodel object! Provided class, " + clazz.getName()
                    + "is not annotated with @Entity");
        }
            return new Metamodel<>(clazz);

    }

    public Metamodel(Class<T> clazz){
        this.clazz = clazz;
        this.columnFields = new LinkedList<>();
        getColumns();
        this.foreignKeyFields = new LinkedList<>();
        this.methods = clazz.getMethods();

        try {
            this.conn = ConnectionPooling.createConnection();
        } catch (SQLException e) {
            System.out.println("Something happened in the metaModel setting the connection");
            e.printStackTrace();
        }
    }

    public String getClassName(){
        return clazz.getName();
    }

    public String getSimpleClassName(){
        return clazz.getSimpleName();
    }

    public Metamodel selection(String... columns){
        pstmt = null; // to make sure any previous things dont mess it up
        resultFields.clear();

        try{
            Entity entity = clazz.getAnnotation(Entity.class);
            String entityName = entity.tableName();

            if (columns.length == 0) {
                pstmt = conn.prepareStatement("select * from " + entityName);

                // the below is replacing a for loop
                resultFields.addAll(columnFields);
                return this;
            }

            StringBuilder placeholders = new StringBuilder();
            String delim;



        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    // this method is used to validate and execute
    public ArrayList<T> validateAndRunSelection() throws SelectException {
        // check if it is a select statement
        if(!pstmt.toString().startsWith("select")){
            throw new SelectException("You're Prepared statement does not have a select at the start!!");
        }

        ArrayList<T> results = new ArrayList<>();
        try{
            ResultSet rs = pstmt.executeQuery();
            results = mapResultSet(rs);
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return results;
    }

    public IdField getPrimaryKey(){
        Field[] fields = clazz.getDeclaredFields();
        for(Field field: fields){
            Id primaryKey = field.getAnnotation(Id.class);
            if(primaryKey != null){
                return new IdField(field);
            }
        }
        throw new RuntimeException("Did not find a field annotated with @id in: " + clazz.getName());
    }

    public List<ColumnField> getColumns(){
        List<ColumnField> columnFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields){
            Column column = field.getAnnotation(Column.class);
            if(column != null){
                columnFields.add(new ColumnField(field));
            }
        }

        if(columnFields.isEmpty()){
            throw new RuntimeException("No columns found in: " + clazz.getName());
        }
        return columnFields;
    }

    public List<ForeignKeyField> getForeignKeys(){
        List<ForeignKeyField> foreignKeyFields = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for(Field field: fields){
            JoinColumn column = field.getAnnotation(JoinColumn.class);
            if(column != null){
                foreignKeyFields.add(new ForeignKeyField(field));
            }
        }
        return foreignKeyFields;
    }


    private Method getMethodUsingFieldName(String fieldName){
        // go through all the methods in the model and try to find one that has
        // the same name as the one passed in
        for (Method currMethod : methods){
            if(currMethod.getName().equals(fieldName)){
                return currMethod;
            }
        }
        // default to null if there were no methods with the name passed in
        return null;
    }

    private ArrayList<T> mapResultSet(ResultSet rs) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException {
        T result;
        IdField primaryKey = getPrimaryKey();
        ArrayList<T> results = new ArrayList<>();

        while(rs.next()){
            result = clazz.newInstance();
            char[] primeKey = primaryKey.getName().toCharArray();
            // changed the first letter to upperCase because set methods will have "set" then a uppercase word
            primeKey[0] = Character.toUpperCase(primeKey[0]);
            // value of will print string equal to "null"
            // wheareas the toString will throw a null pointer if the argument is null
            String tempPrimaryKey = String.valueOf(primeKey);
            Method setPrimaryKey = getMethodUsingFieldName("set" + tempPrimaryKey);

            try{
                // usually primary key is integer
                int pk = rs.getInt(primaryKey.getColumnName());
                setPrimaryKey.invoke(result,pk);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            // now the columns
            for(ColumnField col: columnFields){
                char[] charCol = col.getName().toCharArray();
                charCol[0] = Character.toUpperCase(charCol[0]);
                String tempString = "set" + String.valueOf(charCol);
                Method setColumn = getMethodUsingFieldName(tempString);

                // the types could be different for different columns
                Class<?> type = col.getType();

                // just using the most used ones I think of.  I also added time since we have used
                // it a couple times in demos
                if(type == String.class){
                    setColumn.invoke(result,rs.getString(col.getColumnName()));
                }else if(type == int.class){
                    setColumn.invoke(result,rs.getInt(col.getColumnName()));
                }else if(type == double.class){
                    setColumn.invoke(result,rs.getDouble(col.getColumnName()));
                }else if(type == Time.class){
                    setColumn.invoke(result,rs.getTime(col.getColumnName()));
                }
                else{
                    System.out.println("UH OH! we were not prepared for something like that type!");
                }
            }
            // now the foreign keys
            for(ForeignKeyField foreignKey: foreignKeyFields){
                char[] charFK = foreignKey.getName().toCharArray();
                charFK[0] = Character.toUpperCase(charFK[0]);
                String tempString = "set" + String.valueOf(charFK);
                Method setColumn = getMethodUsingFieldName(tempString);

                try{
                    setColumn.invoke(result,rs.getInt(foreignKey.getColumnName()));
                }catch (SQLException | InvocationTargetException e){
                    // just have this here because a table CAN have a foreign key but does not NEED to
                }
            }
            results.add(result);

        }
        return results;
    }

    // thought about making a method to do the few lines of conversion needed to change the
    // first letter to an uppercase in preperation for the set method.  but decided not worth it
//    private String makeFirstLetterUpper(String string){
//
//    }

}
