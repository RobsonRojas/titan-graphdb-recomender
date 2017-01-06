if [ $# -ne 1 ]
then
echo "Nombre de paràmetres no vàlid!"
else
java -cp graph-benchmark-Neo4J-1.0-jar-with-dependencies.jar edu.upc.dama.Neo4J.benchmark.Main -i "neo-db-scale$1/" -s "../data/sample_scale$1.txt"
fi
