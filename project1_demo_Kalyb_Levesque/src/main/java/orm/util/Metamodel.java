package orm.util;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.JoinColumn;
import orm.exceptions.DeleteException;
import orm.exceptions.InsertionException;
import orm.exceptions.SelectException;
import orm.exceptions.WhereClauseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;

public class Metamodel<T> {
    private Class <T> clazz;
    private IdField primaryKeyField;
    private List<ColumnField> columnFields;
    private List<ForeignKeyField> foreignKeyFields;

    private Method[] methods; // used to get the methods of the model class.  used so it can dynamically use the set methods! possibly the get methods later
    private ArrayList<ColumnField> resultFields; // planning to use this to hold result set of a SELECT
    private PreparedStatement pstmt;  // made here so i dont have to pass it in as a varaible every time
    private Connection conn; // pass in the connection to the metamodel class

//    public static <T> Metamodel<T> of(Class<T> clazz){
//        if (clazz.getAnnotation(Entity.class) == null) {
//            throw new IllegalStateException("Cannot create Metamodel object! Provided class, " + clazz.getName()
//                    + "is not annotated with @Entity");
//        }
//            return new Metamodel<>(clazz);
//
//    }

    public Metamodel(Class<T> clazz,ConnectionPooling connectionPool){
        this.clazz = clazz;
        this.columnFields = new LinkedList<>();
        this.resultFields = new ArrayList<>();
        getColumns();
        this.foreignKeyFields = new LinkedList<>();
        this.methods = clazz.getMethods();
        this.conn = connectionPool.getConnection();
    }

    public String getClassName(){
        return clazz.getName();
    }

    public String getSimpleClassName(){
        return clazz.getSimpleName();
    }

    /**
     *  the select statement where I can take any number of strings, even no strings
     * @param columns  can have any number of strings
     * @return returns this object, but now it has changed fields like the prepared statement
     */
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
            String delimeter;

            // check the columns passed in!
            for(int i = 0; i < columns.length; i++){
                // if it is not the last one of the passed in strings add a comma and add it to the
                // list of columns that will be searched for
                if(i < columns.length-1){
                    delimeter = ", ";
                } else{
                    delimeter = ""; // not sure if its needed but might so that problems dont happen later
                }

                // find the column field that is equal to the string passed in
                for(ColumnField col: columnFields){
                    if(col.getColumnName().equals(columns[i])){
                        placeholders.append(columns[i] + delimeter);
                        resultFields.add(col);
                        break;
                    }
                }

            }
            //System.out.println(placeholders.toString());
            pstmt = conn.prepareStatement("select " + placeholders.toString() + " from " + entityName);

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

    public Metamodel<T> initialWhere(String col, WhereConditions condition, String compareWith) throws WhereClauseException, SQLException {

        /*
            do some validation before starting the actual where clause
         */
        if(pstmt.toString().contains("where")){
            throw new WhereClauseException("cannot have two where clauses! use something else like 'and' or  'or'");
        }
        // added a where to the prepared statement
        pstmt = conn.prepareStatement(pstmt.toString() + " where ");
        // the initial where clause needs no additional logic
        return where(col,condition,compareWith);
    }
    public Metamodel<T> where(String col, WhereConditions condition, String compareWith) throws SQLException, WhereClauseException {
         ColumnField column = null;

        String ps = pstmt.toString();

        switch (condition){
            case EQUALS:
                    pstmt = conn.prepareStatement(ps + col + " = ?");
                    // find the corresponding column is the list of columns
                    for(ColumnField c : columnFields){
                        if(c.getColumnName().equals(col)){
                            column = c;
                            break;
                        }
                    }
                    break;
            case LESS_THAN:
                pstmt = conn.prepareStatement(ps + col + " < ?");
                for(ColumnField c : columnFields){
                    if(c.getColumnName().equals(col)){
                        column = c;
                        break;
                    }
                }
                break;
            case NOT_EQUALS:
                pstmt = conn.prepareStatement(ps + col + " <> ?");

                for(ColumnField c : columnFields){
                    if(c.getColumnName().equals(col)){
                        column = c;
                        break;
                    }
                }
                break;
            case GREATER_THAN:
                pstmt = conn.prepareStatement(ps + col + " > ?");

                for(ColumnField c : columnFields){
                    if(c.getColumnName().equals(col)){
                        column = c;
                        break;
                    }
                }
                break;
            case LESS_THAN_OREQUAL:
                pstmt = conn.prepareStatement(ps + col + " <= ?");

                for(ColumnField c : columnFields){
                    if(c.getColumnName().equals(col)){
                        column = c;
                        break;
                    }
                }
                break;
            case GREATER_THAN_OREQUAL:
                pstmt = conn.prepareStatement(ps + col + " >= ?");

                for(ColumnField c : columnFields){
                    if(c.getColumnName().equals(col)){
                        column = c;
                        break;
                    }
                }
                break;
            default:
                // maybe add an excpetion here later for someone not adding one of the above
        }

        Class<?> type = column.getType();

        if(type == String.class){
            pstmt.setString(1,compareWith);
        }
        else if(type == int.class){
            pstmt.setInt(1,Integer.parseInt(compareWith));
        }
        else if(type == double.class){
            pstmt.setDouble(1,Double.parseDouble(compareWith));
        }
        return this;
    }
    /**
     * add the and logic at the end of the statement if there is a where clause already inside the statement
     * @return returns the object
     * @throws WhereClauseException  in case the where clause has an issue
     * @throws SQLException because i am reassigning the prepared statement this is needed
     */
    public Metamodel<T> and(String col, WhereConditions condition, String compareWith) throws WhereClauseException, SQLException {
       if(!pstmt.toString().contains("where")){
           throw new WhereClauseException("cannot call and if no where clause");
       }
       //
       pstmt = conn.prepareStatement(pstmt.toString() + " and ");
        return where(col, condition,compareWith);
    }
    public Metamodel<T> or(String col, WhereConditions condition, String compareWith) throws SQLException, WhereClauseException {
        if(!pstmt.toString().contains("where")){
            throw new WhereClauseException("cannot call and if no where clause");
        }
        pstmt = conn.prepareStatement(pstmt.toString() + " or ");
        return where(col,condition, compareWith);
    }

    public Metamodel<T> insertion(String... columns){
        pstmt = null;
        resultFields.clear();

        try{
            Entity entity = clazz.getAnnotation(Entity.class);
            String entityName = entity.tableName();
            ArrayList<String> columnsFilter = new ArrayList<>();
            StringBuilder record = new StringBuilder();
            String delimeter;

            for(int i = 0; i < columns.length; i++){
                for(ColumnField col: columnFields){
                    if(col.getColumnName().equals(columns[i])){
                        columnsFilter.add(columns[i]);
                        resultFields.add(col);
                    }
                }
            }

            for(int i = 0; i < columnsFilter.size(); i++){
                delimeter = (i <columnsFilter.size()-1) ? ", " : "";

                record.append(columnsFilter.get(i) + delimeter);

            }

            pstmt = conn.prepareStatement("insert into " + entityName + "("
                        + record.toString() + ") values ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
    public Metamodel<T> insertionValues(String... recordValues) throws InsertionException {
        if(recordValues.length != resultFields.size()){
            throw new InsertionException();
        }

        if(!pstmt.toString().contains("insert")){
            throw new InsertionException("There is no insert statement in the prepared statement!!");
        }

        StringBuilder record = new StringBuilder();
        String delimeter;

        for(int i = 0; i < recordValues.length; i++){
            delimeter = (i < recordValues.length-1) ? ", " : "";
            record.append("?" + delimeter);

        }

        try {
            String temp = pstmt.toString();
            pstmt = conn.prepareStatement(temp + "(" + record.toString() + "), ");

            // dynamically fill in all the ? with the correct value
            for(int i = 0; i < resultFields.size();i++){
                Class<?> type = resultFields.get(i).getType();
                if(type == String.class) {
                    pstmt.setString(i + 1, recordValues[i]);
                } else if(type == int.class){
                    pstmt.setInt(i + 1,Integer.parseInt(recordValues[i]));
                }else if(type == Double.class){
                    pstmt.setDouble(i + 1,Double.parseDouble(recordValues[i]));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return this;
    }

    public int validateAndRunInsertion() throws InsertionException, SQLException {
        if(!pstmt.toString().startsWith("insert")){
            throw new InsertionException("validation and running happends after the insert command!!");
        }
        String pstmtString = pstmt.toString();
        // just wanna see whats in there
        System.out.println(pstmtString);
        pstmt = conn.prepareStatement(pstmtString.substring(0, pstmtString.length()-2));
        return pstmt.executeUpdate();
    }

    /**
     * start with the basic layout of the delete command  where is in another function so this will default
     * to delete all
     * @return return the metamodel with the below updated information
     * @throws SQLException incase something wrong happens in the prepared statement
     */
    public Metamodel<T> deletion() throws SQLException {
        pstmt = null;
        resultFields.clear();
        Entity entity = clazz.getAnnotation(Entity.class);
        String entityName = entity.tableName();
        pstmt = conn.prepareStatement("delete from " + entityName);

        return this;
    }
    public int validateAndRunDeletion() throws DeleteException, SQLException {
        if(!pstmt.toString().startsWith("delete")){
            throw new DeleteException("to run the delete command you must have delete first in the statement");
        }
        return pstmt.executeUpdate();
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
        //List<ColumnField> columnFields = new ArrayList<>();
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
            } catch (InvocationTargetException | SQLException e) {
                    // have to add nothing here in the sqlException to fix an error
                //    System.out.println("is this where i have a problem?");
            //    e.printStackTrace();
            }

            //System.out.println(resultFields.size());
            // now the columns
            for(ColumnField col: resultFields){
            //    System.out.println("Col in result " + col);
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
