#/bin/sh
mvn eclipse:eclipse -DdownloadSources=true

case `uname` in
Linux)
	find . -name .classpath -exec sed -i -e "s/org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.launching.JRE_CONTAINER\/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\/J2SE-1.5/g" {} \;
	;;
*)
	find . -name .classpath -exec sed -i "" -e "s/org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.launching.JRE_CONTAINER\/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\/J2SE-1.5/g" {} \;
	;;
esac
