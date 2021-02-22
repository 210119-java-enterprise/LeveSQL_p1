# LeveSQL_p1
one of the first things needed is going to be properties taken from a .properties file
so the first thing will be initializing the properties then trying to load the properties from the file.  the user will have to give a file path to where the .properties file is located.

after the properties are given they will need to be passed to the ConnectionPool object in order for connection pooling to be used in order to access the database.

.Create() will need to be invoked after passing the properties in.  I recommend:
 ....... new ConnectionPooling(<name of reference to Properties>).Create();
  
  I also reccomment creating a ArrayList for future use in holding the results from a select statement
  after than you can access the Metamodel object in order to be able to access the database commands for now it has basic CRUD functionality
  
  Metamodel<<name of model>> example = new Metamodel<>(<name of model>, <variable holding the connection pool>);
  
 now with access to the Metamodel you can use:
 example.insertion...
 example.deletion...
 etc.
 
 to insert:
 example.insertion(<any number of strings representing columnNames>).insertValues(<any number of strings representing what data you want inserted>).validateAndRunInsertion();

to delete:
 example.deletion().validateAndRunDeletion(); // can also use where clauses to choose specific records
 
 to update:
  example.updating(<any number of strings representing columnNames>).setValues(<any number of strings representing what data you want updated>).initialWhere(<columns>,<a WhereCondition holding equals, not equals, less than,etc>,<data to compare with>).validateAndRunUpdate();
  
  
 to select:
 you are going to want to use that arrayList I mentioned above and set it equal to the all this
 
 result = example.selection().initialWhere(SAME AS ABOVE EXAMPLE FOR WHERE).validateAndRunSelection();
 
 .selection() will grab everything but if you want just specific columns just say .selection(<column><column><etc>...);
 
  
  
  lastly the user will have access to the information!
  
  // the results is an array list but to get a single record I reccomned making a reference to the same object used to create the metamodel
  for(int i = 0; i < result.size(); i++){
    job = (Job) result.remove(i);
    // now you can use this job to access the parts of the record through getters
    job.getJob()
    job.getSalary()
    etc
  }
