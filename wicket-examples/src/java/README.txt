
To run this example, you must first set up database tables by typing:

   ant createTables
   
But before you do this you should set the hibernate.properties value 
for hibernate.connection.url to point to an absolute database location:

   hibernate.connection.url jdbc:hsqldb:/<absolute-path>/guestbook2

If you run "ant createTables" and then the GuestBook2 example from 
different folders using the relative path that is in the hibernate
properties file by default, then the database set up by the ant target
will not be accessible from within the app server and you will get 
an error from hibernate.
