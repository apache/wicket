#/bin/sh
mvn eclipse:eclipse

case `uname` in
Linux)
	find jdk-1.4 -name .classpath -exec sed -i -e "s/org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.launching.JRE_CONTAINER\/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\/J2SE-1.4/g" {} \;
	;;
Darwin)
	find jdk-1.4 -name .classpath -exec sed -i "" -e "s/org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.launching.JRE_CONTAINER\/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\/J2SE-1.4/g" {} \;
	;;
esac
