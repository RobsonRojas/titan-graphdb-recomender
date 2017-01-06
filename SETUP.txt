ProjetoFinal2
=============

Projeto final de robson

Instalando requisitos:

git
	No terminal (root), digite: 

	# apt-get install git


JDK 8 da Oracle

	Siga os passos seguintes:

	Remover o OpenJDK. No terminal (root), digite: 

	# apt-get remove --purge openjdk-* 

	Digite s para confirmar a remoção do OpenJDK. 

	Aguarde a remoção ser concluída. 

	Agora, vamos instalar o Java Oracle (JDK 8). No terminal (root), copie e cole os comandos abaixo: 

	# echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
	# echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
	# apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886 

	Após adicionar os repositórios e a key, atualize a lista de pacotes: 

	# apt-get update 

	Instale o JDK 8, digite: 

	# apt-get install oracle-java8-installer 

	Clique em OK e em Aceitar. 

	Aguarde a instalação ser concluída. 

	Após o término da instalação, digite no terminal: 

	$ java -version 

	Irá aparecer algo assim: 

	 java version "1.8.0_05"
	 Java(TM) SE Runtime Environment (build 1.8.0_05-b13)
	 Java HotSpot(TM) 64-Bit Server VM (build 25.5-b02, mixed mode) 

	Confirme a instalação do compilador Java: 

	$ javac -version 

	A minha versão é a: 

	 javac 1.8.0_05 

	Pronto! Agora seu Java Oracle 8 já está instalado. 




Apache Maven 3.2.1
	Siga os seguintes passos:

	Abra o arquivo sources.list:

		sudo -H gedit /etc/apt/sources.list

	Adicione as linhas seguintes ao arquivo:

		deb http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main

		deb-src http://ppa.launchpad.net/natecarlson/maven3/ubuntu precise main

	Execute os comandos seguintes:

		sudo apt-get update && sudo apt-get install maven3
		
		sudo rm /usr/bin/mvn

		sudo ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn


Netbeans IDE 8.0.3:

	Baixe o instalador do link http://download.netbeans.org/netbeans/8.1/beta/
	
	Siga as instruções em https://netbeans.org/community/releases/81/install.html#install_windows



Baixando o projeto o projeto:

	Vá para a pasta home e execute:
	git clone https://github.com/RobsonRojas/ProjetoFinal2.git




Editando o projeto

Considerando que a pasta raiz do projeto seja PROJETO_FINAL, vá até a pasta PROJETO_FINAL/FONTES/hpc-sgab
Nesta pasta estão os fontes do projeto do benchmark para o Neo4j (pasta graph-benchmark-Neo4j-2.1.6) e Titan (pasta graph-benchmark-titan)

Abra o netbeans e abra cada um dos projetos.


