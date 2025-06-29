Projet Alanya
Alanya est une application de communication hybride qui combine une architecture Client-Serveur centralisée avec des communications Peer-to-Peer (P2P) directes entre les clients.

Architecture
L'application est composée de trois parties principales:

Un serveur central (AlanyaCentralServer): Il agit comme un annuaire et un facilitateur pour l'authentification, la gestion des contacts, la présence (statut en ligne/hors ligne) et la mise en relation pour les appels.

Un client (Mainathentification): Une application de bureau JavaFX qui, une fois connectée, établit des connexions directes avec d'autres clients pour l'échange de messages, de fichiers et pour les flux audio/vidéo.

Une base de données MySQL: C'est la source de vérité pour les données persistantes comme les informations utilisateurs, les messages et les contacts.

Prérequis
Avant de compiler et d'exécuter le projet, assurez-vous d'avoir installé les logiciels suivants :

JDK 21 (Java Development Kit)

Apache Maven

Configuration
Base de Données
Le projet se connecte à une base de données MySQL en ligne. Les détails de la connexion se trouvent dans le fichier src/main/java/com/Alanya/DatabaseConnection.java.

Hôte: 163.123.183.89

Port: 17705

Nom de la base de données: alaniaOther

Utilisateur: people

Mot de passe: people2030

Assurez-vous que votre machine est autorisée à se connecter à ce serveur de base de données.

Dépendances
Le projet utilise Maven pour gérer les dépendances. Celles-ci sont définies dans le fichier pom.xml et incluent:

JavaFX version 21.0.2

MySQL Connector/J version 8.0.33

JavaCV Platform version 1.5.10 pour l'accès à la caméra

Compilation et Exécution
Ouvrez un terminal ou une invite de commande à la racine du projet (là où se trouve le fichier pom.xml).

1. Installation des dépendances
Pour télécharger et installer toutes les dépendances nécessaires, exécutez la commande suivante :

Bash

mvn install
2. Compilation du projet
Pour compiler le code source du serveur et du client, utilisez la commande :

Bash

mvn compile
3. Exécution du Serveur Central
Pour lancer le serveur central (AlanyaCentralServer), exécutez la commande suivante. Le serveur démarrera et écoutera les connexions sur le port 9000.

Bash

mvn exec:java -P run-server
Vous devriez voir des logs dans le terminal indiquant que le serveur a démarré et attend des connexions.

4. Exécution du Client
Pour lancer l'application cliente (Mainathentification), ouvrez un nouveau terminal et exécutez la commande :

Bash

mvn exec:java -P run-client
Cela ouvrira l'interface graphique du client, en commençant par la page de bienvenue et d'authentification. Vous pourrez alors vous connecter ou créer un nouveau compte.