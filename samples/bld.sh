#!sh
find src -name "*.java"|grep -v SCCS|grep -v j2se  > .filelist
javac -bootclasspath ../api/classes -classpath . -d tmpclasses `cat .filelist`
../tools/preverify -classpath "../api/classes" -d classes tmpclasses
rm .filelist
