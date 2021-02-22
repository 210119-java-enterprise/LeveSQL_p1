package orm.util;

import orm.annotations.Column;
import orm.annotations.Entity;
import orm.annotations.Id;
import orm.annotations.JoinColumn;
import orm.exceptions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * this will be a class used to facilitate database calls and operations
 * @param <T> generic type of objects to be determined at runtime
 */
public class Metamodel<T> {
    private Class <T> clazz;
    private IdField primaryKeyField;
    private List<ColumnField> columnFields;
    private List<ForeignKeyField> foreignKeyFields;

    private Method[] methods; // used to get the methods of the model class.  used so it can dynamically use the set methods! possibly the get methods later
    private ArrayList<ColumnField> resultFields; // planning to use this to hold result set of a SELECT
    private PreparedStatement pstmt;  // made here so i dont have to pass it in as a varaible every time
    private Connection conn; // pass in the connection to the metamodel class

    private ArrayList<Integer> updateFields; // needed another arrayList for update later

    /**
     * when metamodel is called initialize a lot of the fields that are gonna be used
     * @param clazz pass in a class to be determined at runtime
     * @param connectionPool pass in the pool of connections
     */
    public Metamodel(Class<T> clazz,ConnectionPooling connectionPool){
        this.clazz = clazz;
        this.columnFields = new LinkedList<>();
        this.resultFields = new ArrayList<>();
        getColumns();
        this.foreignKeyFields = new LinkedList<>();
        this.methods = clazz.getMethods();
        this.conn = connectionPool.getConnection();
        updateFields = new ArrayList<>();
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

    /**
     *  this method is used to validate and execute the select statement
     * @return returns an arrayList of object decided at runtime.  to hold the full result set
     * @throws SelectException if something goes wrong with the select run a custom exception for it
     */
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

    /**
     *  the beginning of the where clause
     * @param col holds the colomn name for which column of the table to use
     * @param condition  some sort of condition with which to compare the column name and the provided string
     * @param compareWith the string being looked for in the specified column
     * @return returns this metamodel with the updated information
     * @throws WhereClauseException  throws custom exception if where clause is attempted more than once
     * @throws SQLException generic sql exception incase anything goes wrong there
     */
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

    /**
     *
     * @param col holds the colomn name for which column of the table to use
     * @param condition  some sort of condition with which to compare the column name and the provided string
     * @param compareWith the string being looked for in the specified column
     * @return returns this metamodel with the updated information
     * @throws WhereClauseException  throws custom exception if where clause is attempted more than once
     * @throws SQLException generic sql exception incase anything goes wrong there
     */
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
     *
     *  the beginning of the where clause
     * @param col holds the colomn name for which column of the table to use
     * @param condition  some sort of condition with which to compare the column name and the provided string
     * @param compareWith the string being looked for in the specified column
     * @return returns this metamodel with the updated information
     * @throws WhereClauseException  throws custom exception if where clause is attempted more than once
     * @throws SQLException generic sql exception incase anything goes wrong there
     */
    public Metamodel<T> and(String col, WhereConditions condition, String compareWith) throws WhereClauseException, SQLException {
       if(!pstmt.toString().contains("where")){
           throw new WhereClauseException("cannot call and if no where clause");
       }
       //
       pstmt = conn.prepareStatement(pstmt.toString() + " and ");
        return where(col, condition,compareWith);
    }

    /**
     *  the beginning of the where clause
     * @param col holds the colomn name for which column of the table to use
     * @param condition  some sort of condition with which to compare the column name and the provided string
     * @param compareWith the string being looked for in the specified column
     * @return returns this metamodel with the updated information
     * @throws WhereClauseException  throws custom exception if where clause is attempted more than once
     * @throws SQLException generic sql exception incase anything goes wrong there
     */
    public Metamodel<T> or(String col, WhereConditions condition, String compareWith) throws SQLException, WhereClauseException {
        if(!pstmt.toString().contains("where")){
            throw new WhereClauseException("cannot call and if no where clause");
        }
        pstmt = conn.prepareStatement(pstmt.toString() + " or ");
        return where(col,condition, compareWith);
    }

    /**
     *
     * @param columns any number of columns can be inserted this method takes all the column names NOT the values
     * @return returns the metamodel with the updated information.
     */
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

    /**
     *
     * @param recordValues any number of strings can be passed in to give values to insert into a table
     * @return
     * @throws InsertionException  custom insert exception incase something happpens
     */
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

    /**
     * validated and runs the insertions command from the previous commands that would be used
     * @throws InsertionException throws a exception of there is some problem with being able to run this method
     * @throws SQLException standard sql exception for the executeUpdate statement
     */
    public void validateAndRunInsertion() throws InsertionException, SQLException {
        if(!pstmt.toString().startsWith("insert")){
            throw new InsertionException("validation and running happends after the insert command!!");
        }
        String pstmtString = pstmt.toString();
        // just wanna see whats in there
        System.out.println(pstmtString);
        pstmt = conn.prepareStatement(pstmtString.substring(0, pstmtString.length()-2));
        pstmt.executeUpdate();
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

    /**
     *
     * @throws DeleteException custom exception for delete statements
     * @throws SQLException default sql exception
     */
    public void validateAndRunDeletion() throws DeleteException, SQLException {
        if(!pstmt.toString().startsWith("delete")){
            throw new DeleteException("to run the delete command you must have delete first in the statement");
        }
        pstmt.executeUpdate();
    }

    /**
     *  this gets the columns that the user wants to replace another method will have to be used later to replace
     *  with actual values
     * @return return this with the new updated information
     */
    public Metamodel<T> updating(String... columns) throws SQLException, UpdateException {

        if(columns.length == 0){
            throw new UpdateException("You need at least 1 thing to update you cannot have an empty update");
        }

        pstmt = null;

        resultFields.clear();
        updateFields.clear();
        Entity entity = clazz.getAnnotation(Entity.class);
        String entityName = entity.tableName();

        int counter = 0;
       // pstmt = conn.prepareStatement("update " + entityName + " set ");
        ColumnField field = null;
        for(String c: columns){
            // find the column associated with the string passed in
            for(ColumnField col: columnFields){
                if(col.getColumnName().equals(c)){
                    field = col;
                    break;
                }
            }
            if(field != null){
                resultFields.add(field);
            }
            else{
                // used later to skip over some null values
                updateFields.add(counter);
            }
            counter++;

        }

        StringBuilder tempString = new StringBuilder("update " + entityName + " set ");

        for(ColumnField col: resultFields){
            tempString.append(col.getColumnName());
            tempString.append(" = ?, ");
        }
        pstmt = conn.prepareStatement(tempString.substring(0,tempString.length()-2));
        return this;
    }

    /**
     *
     * @param values any number of strings can be passed in for values to replace (update) existing records
     * @return return this metamodel
     * @throws UpdateException throw custom exception of something is called in the wrong order or incorrectly
     * @throws SQLException standard sql exception so as to run executeUpdate
     */
    public Metamodel<T> setValues(String...values) throws UpdateException, SQLException {
        String tempPSTMT = pstmt.toString();

        if(!tempPSTMT.startsWith("update")){
            throw new UpdateException("set has to be called after an update");
        }

        ArrayList<String> columnValues = new ArrayList<>();

        // skip over the ones where there was a null in the updating method

        System.out.println(values.length);
        for(int i = 0; i < values.length; i++){
            //System.out.println("what is this value: " + values[i]);
            if(updateFields.contains(i)){
                System.out.println( "How many times this get run? " + i);
                continue;
            }
            System.out.println(values[i]);
            columnValues.add(values[i]);
        }

        // now that you have the values
        for(int i = 0; i < resultFields.size(); i++){
            ColumnField field = resultFields.get(i);
            Class<?> type = field.getType();

            if(type == String.class){
                pstmt.setString(i+1,columnValues.get(i));
            }
            else if(type == int.class){
                pstmt.setInt(i+1,Integer.parseInt(columnValues.get(i)));
            }
            else if(type == double.class){
                pstmt.setDouble(i+1, Double.parseDouble(columnValues.get(i)));
            }
        }
        return this;
    }

    /**
     *
     * @throws UpdateException custom exception in order to make sure it is being run in the correct order, etc
     * @throws SQLException stand exception in order to run executeUpdate
     */
    public void validateAndRunUpdate() throws UpdateException, SQLException {
        if(!pstmt.toString().startsWith("update")){
            throw new UpdateException("can only be called after update which will also need a set");
        }
        System.out.println(pstmt.toString());
        pstmt.executeUpdate();
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

    /**
     * find method of a generic class by using the fieldname passed in
     * @param fieldName if the method has the same name as the fieldName passed in return it
     * @return returns a Method of a generic type
     */
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

    /**
     *
     * @param rs  the resultSet passed  in that will be parsed
     * @return return an arrayList mapping the results
     * @throws SQLException     the multitude of exceptions used in order to use sql commands, invoke generic methods, etc
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
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
