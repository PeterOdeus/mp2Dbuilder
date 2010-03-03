#!/bin/sh
CDK_PROJECT_NAME="jchempaint-primary"
CDK_PROJECT_VERSION="bioclipse-2.1.x" 
for module_name in "core" "interfaces" "ioformats" "io" "nonotify" "data" "standard" "atomtype" "render" "renderbasic" "renderextra" "renderawt" "sdg" "extra" "isomorphism" "smarts" "valencycheck" "smiles" "rendersvg"
do
mvn install:install-file \
  -Dfile=../$CDK_PROJECT_NAME/dist/jar/cdk-$module_name.jar \
  -DgroupId=org.openscience.cdk \
  -DartifactId=cdk-$module_name \
  -Dversion=$CDK_PROJECT_VERSION \
  -Dpackaging=jar \
  -DgeneratePom=true
OUT1=$?
mvn install:install-file \
  -Dfile=../$CDK_PROJECT_NAME/dist/jar/cdk-$module_name-sources.jar \
  -DgroupId=org.openscience.cdk  \
  -DartifactId=cdk-$module_name \
  -Dversion=$CDK_PROJECT_VERSION \
  -Dpackaging=jar \
  -Dclassifier=sources \
  -DgeneratePom=true
OUT2=$?
if [ $OUT1 -eq 0 -a $OUT2 -eq 0 ];then
   echo "Successful installation of cdk-$module_name"
else
   echo "Failed installation of cdk-$module_name"
   break
fi
done
