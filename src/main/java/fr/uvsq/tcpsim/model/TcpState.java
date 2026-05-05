package fr.uvsq.tcpsim.model;

/* Etats possibles d'une connexion TCP */
public enum TcpState {
    CLOSED, /* â”œÂ®tat initial, aucune connexion n'est â”œÂ®tablie */
    LISTEN, /* en attente d'une demande de connexion */
    SYN_SENT, /* demande de connexion envoyâ”œÂ®e, en attente de confirmation */
    SYN_RECEIVED, /* demande de connexion reâ”œÂºue, en attente de confirmation */
    ESTABLISHED, /* connexion â”œÂ®tablie, les donnâ”œÂ®es peuvent â”œÂ¬tre â”œÂ®changâ”œÂ®es */
    FIN_WAIT, /* en attente de la râ”œÂ®ception d'un paquet FIN */
    CLOSE_WAIT, /* en attente de la râ”œÂ®ception d'un paquet FIN */
    LAST_ACK, /* en attente de la râ”œÂ®ception d'un paquet ACK */
    TIME_WAIT /* en attente de la râ”œÂ®ception d'un paquet ACK */
}
