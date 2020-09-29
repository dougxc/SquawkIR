#!sh
find classes -name "*.class" > .dellist
rm `cat .dellist`
rm .dellist
