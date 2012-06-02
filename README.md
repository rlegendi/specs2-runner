specs2-runner
=============

This is a Specs2 extension for the ScalaTest plug-in for ScalaIDE.

Specs 2 is a redesigned version of Specs using functional components (see [a great introduction](http://www.youtube.com/watch?v=lMyNRUuEvNU) by Eric Torreborre).

Building on your own
--------------------

It is a simple Sbt project, you can buid it by the following command:

	$ sbt package

You can find the generated Jar file in the `target` folder.

Importing the project into Eclipse
----------------------------------

First, you need to [install sbteclipse][sbteclipse], it requires sbt 0.11.3 at least.

Then generate the required `.project` and `.classpath` files for Eclipse by the following command:

	$ sbt eclipse
	[info] Loading global plugins from ...
	[info] Set current project to specs2-runner 
	[info] About to create Eclipse project files for your project(s).
	[info] Resolving org.scala-lang#scala-library;2.9.1 ...
	[info] Resolving org.scalatest#scalatest-finders_2.9.1;1.0.1 ...
	[info] Resolving org.scalatest#scalatest_2.9.1;2.0.M1 ...
	[info] Resolving org.scalatest#spec-runner_2.9.1;0.2.0 ...
	[info] Resolving org.specs2#specs2_2.9.1;1.11-SNAPSHOT ...
	[info] Resolving org.specs2#specs2-scalaz-core_2.9.1;6.0.1 ...
	[info] Resolving junit#junit;4.10 ...
	[info] Resolving org.hamcrest#hamcrest-core;1.1 ...
	[info]  [SUCCESSFUL ] org.specs2#specs2_2.9.1;1.11-SNAPSHOT!specs2_2.9.1.jar (5073ms)
	[info] Done updating.
	[info] Successfully created Eclipse project files for project(s): specs2-runner

Note that the generated files contain absolute paths, that is the reason why they got ignored in the `.gitighore` file.

License
-------

  [sbteclipse]: https://github.com/typesafehub/sbteclipse

