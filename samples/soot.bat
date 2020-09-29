set classpath=\w\tools\soot\soot-1.2.2\delta\classes;\w\tools\soot\soot-1.2.2\soot\classes;\w\tools\soot\soot-1.2.2\jasmin\classes;

rem java soot.Main --app -W -O --class --soot-classpath ../api/classes;classes -x java -x sun -x com -x de -d tmpclasses %1 --final-rep grimp     -p jop.cse disabled:false    -p wjop.si expansion-factor:1000     -p wjop.si insert-null-checks:false     -p wjop.si insert-redundant-casts:false         -p wjop.si  expansion-factor:8    -p wjop.si max-container-size:15000     -p wjop.si max-inlinee-size:200
java -ms256M -mx256M soot.Main --app -W -O --class --soot-classpath ../api/classes;classes -x java -x sun -x com -x de -d tmpclasses %1 --final-rep grimp     -p jop.cse disabled:false    -p wjop.si insert-null-checks:false     -p wjop.si insert-redundant-casts:false  -p wjop.si  expansion-factor:4    -p wjop.si max-inlinee-size:50


preverify -classpath ../api/classes;. -d classes tmpclasses


