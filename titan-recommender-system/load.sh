if [ $# -ne 1 ]
then
echo "Nombre de paràmetres no vàlid!"
else
#java -cp hpc-sgab-Titan-0.4.4-1.0.jar -Xms512m edu.upc.dama.Titan.benchmark.Kernel1 -i "../data/scale$1.txt" -s $1
#JAVA_HOME=$JAVA_HOME mvn "-Dexec.args= -Xms1024m -classpath %classpath edu.upc.dama.Titan.benchmark.Main" -Dexec.executable=java -Dexec.workingdir=`pwd` org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
 mvn clean install
mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main 0 10 1 0 0 0 0" -Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec

#mvn exec:java -Dexec.mainClass=edu.ufam.engcomp.graph.benchmark.Main 
fi
