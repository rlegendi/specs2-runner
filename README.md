# specs2-runner #

This is a Specs2 extension for the ScalaTest plug-in for ScalaIDE.

Specs 2 is a redesigned version of Specs using functional components (see [a great introduction](http://www.youtube.com/watch?v=lMyNRUuEvNU) by Eric Torreborre).

## Putting together the Specs2 Scala-IDE plugin ##

**Warning! Here be dragons!**

The project is built upon several work-in-progress APIs. Until they are fixed and integrated it requires quite a work to put things together if you would like to try it out. Scala guys are quite brave in this kind of situations, so I try to summarize the things that has to be done in order to make things work :-) You can start experimenting if you are brave enough, but I think it requires about ~2 full days to hack things together. So be careful.

### Setting up a Developer version of the Scala IDE ###

The Typesafe documentations are quite clear about how to set up a Scala IDE developer environment.

Find all the dev docs here:

http://scala-ide.org/docs/dev/index.html

The important sections are how to set up the devenv:

http://scala-ide.org/docs/dev/setup/setup.html

And how to run the build:

http://scala-ide.org/docs/dev/building/building.html

**In the case you would like to use the features of the Specs2 plugin, you should merge the changes of my fork of the scala-ide repository, or use my fork directly.** You can find the repo here:

https://github.com/rlegendi/scala-ide

This version contains the Specs2 plugin which contributes a few new wizards, templates and other things to the Scala IDE that helps working with specifications. Note that it contains [Skyluc's scalatest repository](https://github.com/skyluc/scalatest) (currently this is the primary repository for the ScalaTest integration) as a submodule (*see next subsection for the details*).

**If you would like to simply run Specs2 specifications with the ScalaTest plugin, you only need the Scala IDE developer environment **and** Skyluc's `scalatest` repository.** This way you will be able to run Specs2 tests through the ScalaTest plugin, but nothing more is available.

### Integrating the ScalaTest plugin ###

The ScalaTest integration is still under heavy development for the Scala IDE (thanks for [Skyluc](https://github.com/skyluc/) and [Cheeseng](https://github.com/cheeseng/) for the great work!).

The major things you should be aware of are:

* Initial work on ScalaTest integration (**closed**): https://github.com/scala-ide/scala-ide/pull/94
* Skyluc's temporal repository for the integration (**this is the current work in progress**): https://github.com/skyluc/scalatest
* A longer thread on the scala-ide-dev mailing list about the integration: https://groups.google.com/d/topic/scala-ide-dev/A-jWSJaotfQ/discussion
  
  This is an important thread to follow because it contains a few vital points (e.g., [how to build the scalatest-finders v1.0.1 dependency][finders-howto])

### Building the specs2-runner project ###

If you have set up the developer version of the Scala IDE with the ScalaTest plugin, you can run specifications on any project if you add the `specs2-runner` and the latest `scalatest` libraries to the classpath of that project. Then simply right-click on any package or specification structure, and select the proper *ScalaTest* element from the *Run As...* menu.

To acquire the latest `specs2-runner` library, you have to build it by hand at the moment. It is a simple Sbt project, you can package it by the following command:

	$ sbt package

You can find the generated Jar file in the `target` folder.

That's all!

### Future work ###

Soon the ScalaTest integration is going to finish. If that task is completed, we can push a snapshot version of the `specs2-runner` project into any repository, so it will be easily available for any Sbt/Maven/Ivy projects, no further hacking will be required.

As soon as the Scala IDE API is fixed, we can create a separate plugin (instead of working in the Scala IDE repo directly). This way we can create a separate update site, so the installation of the plugin would reqire nothing more just a few clicks in the IDE. 

## Importing the project into Eclipse ##

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

Note that the generated files contain absolute paths, that is the reason why they got ignored in the `.gitignore` file.

## License ##

This code is open source software licensed under [The MIT License](MIT) (MIT). Feel free to use it accordingly.

  [finders-howto]: https://groups.google.com/d/msg/scala-ide-dev/A-jWSJaotfQ/R4IpykP8ldYJ
  [sbteclipse]: https://github.com/typesafehub/sbteclipse
  [MIT]: http://www.opensource.org/licenses/mit-license.php

