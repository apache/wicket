
Wicket 0.9-alpha

Welcome to the early access preview of Wicket.

Although I believe that Wicket overall represents a step forward for
the J2EE presentation layer, the following limitations currently
exist:

 * There is not yet a concrete plan for clustering support.  It should
   be easy to do sticky session clustering, but failover clustering will
   require some thought and planning and probably outside help.

 * The User's Guide, which will be published at CafePress.com is not
   yet complete (about 9 out of 12 chapters are ready to go)

 * Some components and features have not yet been tested

 * The PetStore example has not yet been started due to time constraints
 
 * The GuestBook2 application does not yet automatically create database
   tables.  This results in some runtime errors when starting jetty 
   according to QUICK_START.txt.  This can be resolved by manually running 
   the createTables ant target for the GuestBook2 example.
  
 * JavaDoc API is good in most important places, but lacking somewhat 
   in other places

 * Table navigation is not quite finished.  Paging should work (although
   that has not been tested), but scrolling of paging links is not yet 
   implemented.  This is not a major project, just requires some focus.

 * The following planned components and features do not yet exist: 
   FileUploader, DateChooser, Tree, JavaScript client-side validation

 * License agreements for supporting JAR files in the lib folder have 
   not yet been assembled.  Your rights to use and distribute these JAR 
   files will depend on the individual license to each file.  Please 
   see the corresponding open source project for details for now.  
   We may need to assemble the appropriate licenses in some form before 
   final release.

I am interested in any and all feedback on Wicket, although constructive
feedback is most definitely preferred!  

Enjoy!

     Jonathan Locke (jonl@muppetlabs.com)

