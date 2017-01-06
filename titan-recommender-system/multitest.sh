#mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main amostra escala k1 k2 k3 k4 qtdk4" -Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec

echo "amostras"
for amostra in {1..5}
do
	echo "amostra: $amostra"

	echo "escalas"
	for escala in {10..16}
	do
		echo "escala: $escala"
		
		echo "start k1:"
	#	mvn clean install
		
		echo $(grep -q "k1," "HpcData"$amostra"_"$escala"_k123_results.csv")
      while ! $(grep -q "k1," "HpcData"$amostra"_"$escala"_k123_results.csv")
     		do
	     		mvn clean install
				mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main $amostra $escala 1 0 0 0 0" -Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
		
				echo "sleep"
				sleep 20
			done

		echo "end k1:"
		echo "start k2 + k3"

		echo $(grep -q "k3," "HpcData"$amostra"_"$escala"_k123_results.csv")
      while ! $(grep -q "k3," "HpcData"$amostra"_"$escala"_k123_results.csv")
     		do
	     		mvn clean install
#				mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main $amostra $escala 0 1 1 0 0" -		Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
				mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main $amostra $escala 0 1 1 0 0" -Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
				echo "sleep"
				sleep 20
			done
		
		echo "end k2 + k3"


		echo "start k4"
		for qtd in {1..16}
		do
			echo "start k4 qtd: $qtd"
			#repeate while the line is not in file
					echo "k4, $qtd"
					echo "HpcData"$amostra"_"$escala"_k4__results.csv"
					echo $(grep -q "k4, $qtd" "HpcData"$amostra"_"$escala"_k4__results.csv")
		        while ! $(grep -q "k4, $qtd" "HpcData"$amostra"_"$escala"_k4__results.csv")
		        do
		        		mvn clean install
						mvn "-Dexec.args=-classpath %classpath edu.ufam.engcomp.graph.benchmark.Main $amostra $escala 0 0 0 1 $qtd" -Dexec.executable=java -Dexec.workingdir=. org.codehaus.mojo:exec-maven-plugin:1.2.1:exec
						echo "sleep"
						
						sleep 20
		        done

			echo "end k4 qtd: $qtd"

		done
		echo "end k4"


	done

done
