# NioServer

NioServer is a multi-client stateful server written in Scala, using
Java NIO and delimited continuations.

## Blog

This is the code used in my "Java NIO" blog post at
http://jim-mcbeath.blogspot.com/2011/03/java-nio-and-scala-continuations.html

## Compiling and Running with Ant

The files are in the standard layout, with source files under <code>src/main/scala</code>.
To compile using <code>build.xml</code> and ant, you must set up a
symlink called <code>scala-current</code> that points to the main directory
of your installed copy of scala.  Once you have done that, you can use the
following command to compile the application and create a release directory
for it that includes a startup script:

    ant build relbin

You can run the test program with this command (with the appropriate
value for the version number <code>N.N.N</code>):

    release/nioserver-N.N.N/bin/nioserver

If you get an error about port in use, try changing the port value in
<code>src/main/scala/NioServer.scala</code> and recompiling.
Once the server has started without errors, telnet to it from another window:

    telnet localhost 1234

When you type into the telnet window and press enter, your text should be
echoed back to you.

## Compiling and Running without Ant

To compile without using ant, <code>cd</code> into <code>src/main/scala</code>
and run this command:

    scalac -P:continuations:enable *.scala

You can run the test server from the same directory with this command:

    scala NioServer

From this point on, see the instructions in the previous section starting
with what to do about a port-in-use error.

## Cleanup with Ant

You can delete the old files with this command:

    ant clean

The above command will delete the <code>build</code> folder, but not
the <code>releases</code> folder, which is where <code>ant relbin</code>
puts its results.  If you want to delete that folder you must do so manually.

## Cleanup without Ant

From the <code>src/main/scala</code> directory, use this command:

    rm *.class
