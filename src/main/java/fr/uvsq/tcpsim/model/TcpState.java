package fr.uvsq.tcpsim.model;

/* Etats possibles d'une connexion TCP */
public enum TcpState {
    CLOSED, /* état initial, aucune connexion n'est établie */
    LISTEN, /* en attente d'une demande de connexion */
    SYN_SENT, /* demande de connexion envoyée, en attente de confirmation */
    SYN_RECEIVED, /* demande de connexion reçue, en attente de confirmation */
    ESTABLISHED, /* connexion établie, les données peuvent être échangées */
    FIN_WAIT, /* en attente de la réception d'un paquet FIN */
    CLOSE_WAIT, /* en attente de la réception d'un paquet FIN */
    LAST_ACK, /* en attente de la réception d'un paquet ACK */
    TIME_WAIT /* en attente de la réception d'un paquet ACK */
}