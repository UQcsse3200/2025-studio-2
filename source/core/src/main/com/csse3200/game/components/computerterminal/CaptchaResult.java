package com.csse3200.game.components.computerterminal;

import java.util.Set;

/**
 * Immutable snapshot of a CAPTCHAs
 *
 * success true if the player's selection matches the correct set
 * selected indices the player chose at submit time
 * correct  indices considered correct for the puzzle
 */
public record CaptchaResult(boolean success, Set<Integer> selected, Set<Integer> correct) {}
