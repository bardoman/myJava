
set build_loc=F:\BruceEifler\prj\Java\rmi\callback

cd %build_loc%\src

javac -deprecation -classpath . callback\*.java   

rmic -d %build_loc%\src callback.RMIServer callback.RMIClient

jar cvf callback.jar callback\*.class

copy callback.jar %build_loc%\lib
