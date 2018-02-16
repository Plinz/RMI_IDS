# Chat By Duquennoy Antoine and Duverney Thomas

## Fonctionnalités
	
- Envoi des messages en broadcast
- Récupération de l'historique des messages à la connexion
- Sauvegarde de l'historique des messages dans un fichier par le serveur
- Restoration des messages après panne
- Interface graphique du client pour afficher les messages envoyés par les utilisateurs connectés
- Coloration des noms des utilisateurs discutant sur le chat
- Visualisation de la liste des utilisateurs connectés au chat
- Déconnexion d'un utilisateur du chat
- Les noms d'utilisateurs du chat sont uniques
- Envoi de messages privés entre utilisateurs 
- Gestion de plusieurs salons de discution 

## Utilisation

### Pour utiliser via Internet :

Ouvrir le port 1099 de votre box !!!!!!!!
Client : Lancez le jar executable (avec IP=votre adresse IP) :

	java -jar -Djava.rmi.server.hostname="IP" Client.jar
	
Serveur : Lancez le jar executable (sur votre serveur via ssh) (avec IP=l'adresse IP du server) (nécessite les ports 1099 et 2000 d'ouvert) :

	java -jar -Djava.rmi.server.hostname="IP" Server.jar
	

### Via les sources

Compilez les sources avec Java 8
dans le dossier bin, lancez :

Lancez le serveur : (ServerMain)
Lancez des clients : (ClientGUI)
Pour les clients renseignez l'adresse du serveur (localhost par défaut) et un nom de client

Pour le client à la connexion vous pouvez joindre un serveur avec la commande :
	
	/con <Pseudo> <AdresseDuServeur> <Port>
	/con <Pseudo> <AdresseDuServeur>          	(Le port sera 1099 par default)
	/con <Pseudo>					(Le port sera 1099 et le server localhost)
	
Lorsque vous êtes connecté à un serveur vous arrivez sur le salon Accueil.
Pour en créer un nouveau clic droit sur le panneau de gauche et nouveau salon ou lancer la commande :
	
	/create <NomDuSalon>
	
Pour joindre un autre salon clic droit sur le nom du salon à joindre et joindre le salon ou lancer la commande :

	/join <NomduSalon>
	
Pour quitter le salon clic droit sur le salon à quitter et quitter le salon ou lancer la commande :

	/leave <NomDuSalon>
	
Pour détruire un salon que l'on a créer clic droit sur le salon à détruire et détruire le salon ou lancer la commande :

	/destroy <NomDuSalon>
	
Pour se déconnecter du serveur lancer la commande :

	/exit
