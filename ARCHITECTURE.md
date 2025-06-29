Architecture de l'Application Alanya
Ce document décrit l'architecture globale de l'application de messagerie Alanya, ses principaux composants et leurs interactions.

1. Vue d'Ensemble
Alanya est une application de communication hybride qui combine une architecture Client-Serveur centralisée avec des communications Peer-to-Peer (P2P) directes entre les clients.

Le Serveur Central agit comme un annuaire et un facilitateur. Il ne gère pas le contenu des messages ou des appels, mais il est essentiel pour l'authentification, la gestion des contacts, la présence (statut en ligne/hors ligne) et la mise en relation pour les appels.

Les Clients sont des applications de bureau JavaFX qui, une fois connectées, établissent des connexions directes entre elles pour l'échange de messages, de fichiers et pour les flux audio/vidéo.

2. Composants Détaillés
A. Client (Application JavaFX)
Le client est le cœur de l'expérience utilisateur. Il est structuré autour du modèle FXML de JavaFX, qui sépare la vue de la logique du contrôleur.

Vues (/src/main/resources/com/Alanya/*.fxml): Définissent l'interface utilisateur avec des fichiers FXML. Chaque vue principale (authentification, interface de chat) a son propre fichier.

Contrôleurs (/src/main/java/com/Alanya/*.java):

MainAthentificationController: Gère la logique d'inscription et de connexion.

Mainfirstclientcontroller: C'est le contrôleur principal après connexion. Il orchestre l'affichage des contacts, des messages, et initie les interactions utilisateur (envoi de message, appels).

CameraCaptureController, AudioCallController, VideoCallController: Contrôleurs dédiés pour les fenêtres de capture photo et d'appels.

Services (/src/main/java/com/Alanya/services/*.java):

ContactService, PresenceService, NotificationService, AttachmentService: Classes qui encapsulent une logique métier spécifique pour éviter de surcharger les contrôleurs.

EncryptionService: Gère le chiffrement et le déchiffrement des messages avant leur sauvegarde en base de données.

Communication:

CentralServerConnection: Gère la connexion persistante au serveur central pour envoyer des commandes et recevoir des réponses.

ClientServer & PeerSession: Chaque client lance son propre mini-serveur (ClientServer) pour écouter les connexions P2P entrantes. Une PeerSession est créée pour chaque conversation active avec un autre client.

B. Serveur Central (AlanyaCentralServer.java)
Le serveur est une application Java multithread qui gère les connexions de plusieurs clients simultanément.

Gestion des Connexions: Il ouvre un ServerSocket sur un port défini et crée un ClientHandler dédié pour chaque client qui se connecte.

Logique Métier:

Authentification: Valide les identifiants des utilisateurs contre la base de données.

Annuaire de Présence: Maintient une liste en mémoire (ConcurrentHashMap) des clients connectés et de leurs informations de connexion P2P (IP, port).

Mise en Relation (Signaling): Lorsqu'un client A veut appeler un client B, il envoie la demande au serveur. Le serveur la relaie à B. Si B accepte, le serveur transmet les informations de connexion de B à A pour que la connexion P2P puisse être établie.

C. Base de Données (MySQL en ligne)
La base de données est l'unique source de vérité pour les données persistantes.

Modèle (/src/main/java/com/Alanya/model/*.java): Classes simples (POJO) qui représentent les entités de données (ex: Message, Client).

DAO (Data Access Object) (/src/main/java/com/Alanya/DAO/*.java):

UserDAO, MessageDAO, UserContactDAO: Ces classes sont responsables de toutes les interactions avec la base de données. Elles contiennent les requêtes SQL (SELECT, INSERT, UPDATE, DELETE) pour manipuler les données des utilisateurs, des messages et des contacts.

Connexion (DatabaseConnection.java): Une classe utilitaire centralisée pour établir la connexion à la base de données MySQL en ligne.

3. Flux de Communication Typiques
Envoi d'un Message
L'utilisateur A tape et envoie un message à l'utilisateur B dans son interface.

Mainfirstclientcontroller crée un objet Message.

Le message est sauvegardé dans la base de données via MessageDAO.

Le client A vérifie s'il a une PeerSession P2P active avec B.

Si oui, le message est envoyé directement à B via la socket P2P. Si non, le message reste simplement en base de données, et B le récupérera à sa prochaine connexion.

Lancement d'un Appel Vidéo
L'utilisateur A clique sur le bouton d'appel vidéo pour contacter B.

Le client A envoie une commande INITIATE_VIDEO_CALL au Serveur Central.

Le Serveur Central relaie la demande au client B.

Le client B reçoit une notification et une boîte de dialogue s'affiche.

Si B accepte, son client démarre un ServerSocket sur des ports audio/vidéo disponibles et renvoie ces informations au Serveur Central dans une réponse CALL_ACCEPTED.

Le Serveur Central transmet l'adresse IP et les ports de B au client A.

Le client A se connecte directement en P2P aux ports audio/vidéo de B. Le streaming vidéo commence.

La communication vidéo et audio se fait exclusivement en P2P. Le serveur n'est plus impliqué jusqu'à la fin de l'appel.