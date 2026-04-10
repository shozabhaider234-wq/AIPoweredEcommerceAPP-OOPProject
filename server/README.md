# Instructions for local setup
The tool for building .java files to .class files, we are using, is gradle.
When you ran `git pull`, it was downloaded in this newer commit so 
you can start using it directly.
To start the local server, first make sure your `java` version is >= 21, 
`cd` into the project directory where you have `gradlew` file directly listed
and then run:

*NOTE: Replace `./gradlew` with `gradlew.bat` if you are on windows.*

`./gradlew bootRun`

**BUT WAIT!** It uses the default initialization values, for MySQL Database,
with `user=root` and `password=1234`.
If any of these is different for your MySQL Server, you can override them using:

user override ->
`./gradlew bootRun --args='--spring.datasource.user=other_user_with_root_privileges'`

password override ->
`./gradlew bootRun --args='--spring.datasource.password=secret123'`

### UNNECESSARY (probably useful guide):  
The user you provide must be the one with root privileges (super-control)
because when the server starts, it emits a `CREATE DATABASE aiecomerceapp` (implicitly) on behalf of
the user/password combination you provide. If the user has no access for creating a database, it will fail.
Your user is likely to be the same, but password may vary.

### STILL FAILS?

Make sure you have you sql server up and running in the background. 
If you see in the error logs `jdbc`, `hibernate` or other `*ConnectionExceptions`, 
that is exactly it.