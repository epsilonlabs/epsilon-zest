epsilon-zest
===

Graph-based visualizations of models using EOL + GEF4 Zest.

How does it work?
---

This project contributes a new type of launch configuration called "EOL ZestViz
Launch Configuration". This works mostly as a regular EOL launch configuration,
but in addition of running the script once, it uses a set of operations to
produce a graph-based visualization of the model using GEF4 Zest FX.

The `org.eclipse.epsilon.zest.examples` contains example models and EOL scripts
showing how it works. For more details, check the wiki!

Target platform and e(fx)clipse
---

To work on this plugin, please use the provided target platform in `org.eclispe.epsilon.zest.targetplatform`. If you run into classloading issues with the JavaFX classes, make sure you provide the `-Dosgi.framework.extensions=org.eclipse.fx.osgi` JVM option so JavaFX and OSGi work together.
