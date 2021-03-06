# NioServer

NioServer is a multi-client stateful server written in Scala, using
Java NIO and delimited continuations.

## Blog

This project holds the code used in my "Java NIO" series of blog posts.
<table border=1>
<tr><th>Tag</th><th>Blog Post</th><th>Post Date</th></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-continuations">
blog-continuations</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/03/java-nio-and-scala-continuations.html">
Java NIO and Scala Continuations</a></td><td>March 22, 2011</td></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-decoding">
blog-decoding</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/03/java-nio-for-character-decoding-in.html">
Java NIO for Character Decoding in Scala</a></td><td>March 28, 2011</td></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-coroutines">
blog-coroutines</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/04/java-nio-and-scala-coroutines.html">
Java NIO and Scala Coroutines</a></td><td>April 2, 2011</td></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-write">
blog-write</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/04/java-nio-for-writing.html">
Java NIO for Writing</a></td><td>April 8, 2011</td></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-complete">
blog-complete</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/04/java-nio-complete-scala-server.html">
Java NIO Complete Scala Server</a></td><td>April 15, 2011</td></tr>

<tr><td><a href="https://github.com/jimmc/nioserver/tree/blog-executor">
blog-executor</a></td>
<td><a href="http://jim-mcbeath.blogspot.com/2011/07/multithread-coroutine-scheduler.html">
Multithread Coroutine Scheduler</a></td><td>July 19, 2011</td></tr>

</table>

## Compiling and Running with Ant

The files are in the standard layout, with source files under <code>src/main/scala</code>.
To compile using <code>build.xml</code> and ant, you must set up a
symlink called <code>scala-current</code> that points to the main directory
of your installed copy of scala.  Once you have done that, you can use the
following command to compile the application and create a release directory
for it that includes a startup script:

    ant build relbin

The following command (with the appropriate
value for the version number <code>N.N.N</code>)
will run EchoServer:

    release/nioserver-N.N.N/bin/nioserver

This command will run ThreeQuestionsServer:

    release/nioserver-N.N.N/bin/3Qserver

If you get an error about port in use, try changing the port value in
<code>src/main/scala/NioServer.scala</code> and recompiling.
Once the server has started without errors, telnet to it from another window:

    telnet localhost 1234

When you type into the telnet window and press enter, your text should be
echoed back to you.

## Compiling and Running without Ant

To compile without using ant, <code>cd</code> into <code>src/main/scala</code>
and run this command:

    scalac -P:continuations:enable *.scala net/jimmc/*/*.scala

You can run the test server from the same directory with this command:

    scala EchoServer

Or you can run the ThreeQuestionsServer with this command:

    scala ThreeQuestionsServer

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

    rm *.class net/jimmc/*/*.class
