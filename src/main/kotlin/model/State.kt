package model

/**
 * Represent the status on node.
 */
enum class State {

    /**
     * Active node (part of Shape).
     */
    ACTIVE,

    /**
     * Not active node (part of Model array).
     */
    NOT_ACTIVE,

    /**
     * Node is blinking during line clear animation.
     */
    BLINKING,

    /**
     * Node is disappearing (fading out).
     */
    DISAPPEARING

}