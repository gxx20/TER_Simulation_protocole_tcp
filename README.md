# Simulation du protocole TCP

Projet Java de simulation pédagogique d'une connexion TCP entre un client et un serveur. Le programme ne crée pas de vraie socket réseau : il reproduit en mémoire les grandes étapes d'un échange TCP.

## Objectif

Cette application illustre :

- l'ouverture d'une connexion avec le handshake `SYN` / `SYN_ACK` / `ACK`
- l'envoi de données par paquets
- la gestion de paquets corrompus avec `NACK`
- la retransmission des paquets perdus ou corrompus
- la fermeture de connexion avec `FIN` / `FIN_ACK` / `ACK`

## Fonctionnement

Au lancement, la classe principale instancie un client et un serveur, puis enchaîne les étapes suivantes :

1. affichage des états initiaux
2. ouverture de la connexion TCP simulée
3. demande de transfert de 7 paquets avec une fenêtre de réception de 3
4. traitement des paquets reçus, avec retransmission si nécessaire
5. fermeture propre de la connexion

Le serveur contient des données fictives nommées `Bloc-1` à `Bloc-8`. Certains paquets sont volontairement marqués comme corrompus pour montrer le mécanisme de reprise.

## Nouvelle fonctionnalité

Le programme accepte maintenant des paramètres en ligne de commande pour rendre la simulation plus flexible :

- `totalPacketsRequested` : nombre total de paquets demandés au serveur
- `receiveWindow` : taille de la fenêtre de réception

Exemple :

```bash
java -cp target/classes fr.uvsq.tcpsim.Main 5 2
```

En plus de la trace classique, le client affiche un résumé chiffré du transfert avec :

- le nombre de paquets demandés
- le nombre de paquets reçus
- le nombre de paquets corrompus détectés
- le nombre de retransmissions
- le nombre de cycles de transfert
- l'état final du transfert

## Interfaces disponibles

Deux nouvelles interfaces ont été ajoutées pour piloter la simulation :

1) CLI interactive

Lancer :

```bash
java -cp target/classes fr.uvsq.tcpsim.cli.InteractiveCLI
```

Suivre les invites pour définir le nombre de paquets et la fenêtre, puis lancer la simulation.

2) GUI Swing

Lancer :

```bash
java -cp target/classes fr.uvsq.tcpsim.gui.TcpGui
```

Une fenêtre s'ouvre ; entre les paramètres et clique sur `Lancer`. La sortie de la simulation s'affiche dans la zone de texte.

## Configuration

Tu peux créer un fichier `config.properties` à la racine du projet pour définir des valeurs par défaut :

```properties
corruptionProbability=0.25
lossProbability=0.0
defaultPackets=7
defaultWindow=3
exportPath=transfer_summary.csv
```

Si `config.properties` existe, les valeurs seront lues automatiquement par l'application.

## Export CSV

Après une simulation (CLI ou GUI), tu peux exporter le `TransferSummary` au format CSV. Le chemin d'export est défini par `exportPath` dans `config.properties` (par défaut `transfer_summary.csv`). Le CSV inclut l'en-tête :

```
requestedPackets,receivedPackets,retransmissions,corruptedPacketsDetected,cycles,receiveWindow,completed
```

## Arrêt immédiat

Le bouton `Stop` de la GUI demande l'arrêt immédiat du transfert : le client reçoit une demande d'annulation et cesse la boucle de transfert au prochain point de contrôle. Cela permet d'interrompre rapidement de longues simulations.

## Tests

Des tests unitaires ont été ajoutés pour vérifier :

- le comportement du serveur avec probabilité de corruption/perte configurée
- la génération CSV du résumé
- la notification de progression via `TransferListener`

Lancer les tests :

```bash
mvn test
```

## Exemple de résultat

Le programme affiche une trace similaire à celle-ci :

```text
Etat initial du client : CLOSED
Etat initial du serveur : LISTEN
...
[CLIENT]: Connexion établie.
...
[CLIENT]: Transfert terminé.
...
Etat final du client : CLOSED
Etat final du serveur : CLOSED
```

## Structure du projet

- `src/main/java/fr/uvsq/tcpsim/Main.java` : point d'entrée de l'application
- `src/main/java/fr/uvsq/tcpsim/client/TcpClient.java` : logique du client TCP simulé
- `src/main/java/fr/uvsq/tcpsim/server/TcpServer.java` : logique du serveur TCP simulé
- `src/main/java/fr/uvsq/tcpsim/model/` : modèles de paquets, états et transferts
- `src/test/java/` : tests

## Prérequis

- Java 17 ou plus récent
- Maven

## Lancer le projet

Depuis la racine du projet :

```bash
mvn clean compile
java -cp target/classes fr.uvsq.tcpsim.Main
```

Ou, si tu veux lancer les tests en même temps :

```bash
mvn test
```

## Remarques

- Ce projet simule le comportement de TCP, mais n'utilise pas le réseau réel.
- Les numéros de séquence et les paquets corrompus sont gérés de manière déterministe pour rendre la démonstration lisible.