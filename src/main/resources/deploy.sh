
#!/bin/bash
echo "Doing Fresh Code Checkout for ................. bed"

cd /apps/demo/tymbl/code/

rm -rf /apps/demo/tymbl/code/bed

git clone -b main https://github.com/tymbldev/bed.git

cd /apps/demo/tymbl/code/bed

git checkout main
echo "Successfully Fresh Code Checkout for ................. bed"

echo "Doing Maven build for repo............ bed"
mvn clean install -DskipTests
mv /apps/demo/tymbl/code/bed/target/*.jar /apps/demo/tymbl/code/bed/target/services.jar
echo "Successfully Maven build for repo............ bed"

[ -d /apps/demo/tymbl/bed ] || mkdir /apps/demo/tymbl/bed
[ -d /apps/demo/tymbl/logs/bed ] || mkdir /apps/demo/tymbl/logs/bed

echo "Copying jar to /apps/demo/tymbl/bed folder"
cp /apps/demo/tymbl/code/bed/target/services.jar /apps/demo/tymbl/
echo "Successfully Copying jar to /apps/demo/tymbl/bed folder"

echo "Restarting service via supervisor with tymbl"

supervisorctl restart tymbl

echo "Successfully Restarting service via supervisor with name bed"

echo "Doing Folder cleanup.................. "
rm -rf /apps/demo/tymbl/code/bed
echo "Successfully Folder cleanup.................. /apps/demo/tymbl/code/bed"
