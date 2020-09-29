#!sh
find src -name "*.java"|grep -v SCCS|grep -v j2se  > .filelist
javac -bootclasspath . -classpath . -d tmpclasses `cat .filelist`
../tools/preverify -d classes tmpclasses
rm .filelist
