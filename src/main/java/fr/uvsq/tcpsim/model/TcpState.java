package fr.uvsq.tcpsim.model;


public enum TcpState {
    CLOSED,
    LISTEN,
    SYN_SENT,
    SYN_RECEIVED,
    ESTABLISHED,
    FIN_WAIT,
    CLOSE_WAIT,
    LAST_ACK,
    TIME_WAIT
}