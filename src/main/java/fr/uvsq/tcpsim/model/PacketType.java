package fr.uvsq.tcpsim.model;


/* Type de paquets */
public enum PacketType {
    SYN, /* ouverture */
    SYN_ACK, /* ouverture + confirmation */
    ACK, /* confirmation */
    DATA, /* données */
    FIN, /* fermeture */
    FIN_ACK, /* fermeture + confirmation */
    NACK /* négative acknowledgment */
}