#!sh
chmod +x tools/preverify
cd compiler
sh bld.sh
cd ..
cd api
sh bld.sh
cd ..
cd samples
sh bld.sh
cd ..
