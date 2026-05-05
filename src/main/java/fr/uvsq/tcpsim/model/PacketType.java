package fr.uvsq.tcpsim.model;


/* Type de paquets */
public enum PacketType {
    SYN, /* ouverture */
    SYN_ACK, /* ouverture + confirmation */
    ACK, /* confirmation */
    DATA, /* donnâ”œÂ®es */
    FIN, /* fermeture */
    FIN_ACK, /* fermeture + confirmation */
    NACK /* nâ”œÂ®gative acknowledgment */
}
