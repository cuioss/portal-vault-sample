package de.cuioss.portal.client.vault.server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Extends a {@link Boolean} with a undefined state
 *
 * @author Oliver Wolff
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttributeStatus {

    /** The information resolves to {@link Boolean#TRUE} . */
    TRUE("cui-icon-thumbs_up", "success") {

        @Override
        public AttributeStatus negate() {
            return FALSE;
        }
    },

    /** The information resolves to {@link Boolean#FALSE} . */
    FALSE("cui-icon-thumbs_down", "danger") {

        @Override
        public AttributeStatus negate() {
            return TRUE;
        }
    },

    /** The information resolves to nothing -> unknown status. */
    UNKOWN("cui-icon-circle_question_mark", "primary") {

        @Override
        public AttributeStatus negate() {
            return UNKOWN;
        }
    };

    /**
     * @return the negation of the current status: TRUE -> FALSE, FALSE -> TRUE, UNKNOWN -> UNKNOWN
     */
    public abstract AttributeStatus negate();

    @Getter
    private final String icon;

    @Getter
    private final String state;

    /**
     * @param status may be null, if so {@link AttributeStatus#UNKOWN} will be returned
     * @return the status depending on given {@link Boolean}
     */
    public static final AttributeStatus parse(Boolean status) {
        if (null == status) {
            return UNKOWN;
        }
        if (status.booleanValue()) {
            return TRUE;
        }
        return FALSE;
    }
}
