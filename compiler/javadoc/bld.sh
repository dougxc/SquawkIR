#!sh
find ../src -name '*.java' | grep -v SCCS | \
  sed 's:^../src/\(.*\)/[^/]*\.java:\1:g' | \
  sed 's:/:.:g'| sort | uniq > .packagelist
javadoc -linkoffline http://java.sun.com/j2se/1.3/docs/api . \
  -private -sourcepath ../src -d html `cat .packagelist`
