package com.alvarocervantes.fittrackplus.domain.model.progression

object ProgressionTuning {
    // e1RM y confianza (R4)
    const val CONF_HIGH_MAX_EFFECTIVE_REPS   = 6      // <= 6  -> HIGH,   w = 1.0
    const val CONF_MEDIUM_MAX_EFFECTIVE_REPS = 10     // <= 10 -> MEDIUM, w = 0.6
                                                      // >  10 -> no estimable
    const val EPLEY_REP_DIVISOR = 30.0
    const val WEIGHT_HIGH   = 1.0
    const val WEIGHT_MEDIUM = 0.6

    // Tendencia (R13)
    const val EWMA_ALPHA        = 0.3
    const val TREND_MIN_POINTS  = 4
    const val TREND_LOOKBACK    = 3
    const val TREND_RISING_PCT  =  1.5
    const val TREND_FALLING_PCT = -2.5

    // Clasificacion (R14)
    const val SURPLUS_EASY_MIN         =  3
    const val SURPLUS_STRONG           =  2
    const val SURPLUS_ON_TARGET_MIN    =  0
    const val SURPLUS_HARD             = -1
    const val SET_COMPLETION_RATIO_MIN = 0.67

    // Carga (R15)
    const val STEP_EASY                  =  2
    const val STEP_STRONG                =  1
    const val STEP_REDUCE                = -1
    const val MAX_SINGLE_JUMP_PCT        = 7.5
    const val PERCENT_BASE               = 100
    const val FAILS_TO_REDUCE            = 2
    const val FAILS_TO_REVERT            = 1
    const val DEFAULT_INCREMENT_UPPER_KG = 2.5
    const val DEFAULT_INCREMENT_LOWER_KG = 5.0

    // Ajuste intra-sesion (R17)
    const val INTRA_MAX_STEPS = 2
    const val INTRA_REDUCE_SURPLUS_MAX = -2

    // Disparadores de RECOVERING (R18)
    const val RECOVERY_COOLDOWN_EXPOSURES = 4
    const val R1_HARD_STREAK_CAP          = 10
    const val R2_BAD_IN_LAST_N            = 3
    const val R2_BAD_REQUIRED             = 2
    const val R3_MEAN_SURPLUS_MAX         = -1.0
    const val R4_STALL_COUNT              = 2
    const val R4_STALL_WINDOW             = 6
    const val R5_HARD_STREAK              = 8
    const val RECOVERY_HARD_TRIGGER_EXPOSURES = 2

    // Prescripcion de RECOVERING (R18)
    const val RECOVERY_LOAD_FACTOR = 0.90
    const val RECOVERY_SETS_FACTOR = 0.6
    const val RECOVERY_MIN_SETS    = 2
    const val RECOVERY_TARGET_RIR  = 4

    // EVALUATING (R18)
    const val EVALUATION_SETS               = 2
    const val EVALUATION_TARGET_RIR         = 2
    const val EVALUATION_PASS_SURPLUS       = 0
    const val EVALUATION_DEMOTE_PCT         = -5.0
    const val EVALUATION_RECALIBRATE_FACTOR = 0.90

    // CALIBRATING (R18)
    const val CALIBRATION_EXPOSURES   = 3
    const val CALIBRATION_MAX_RETRIES = 2
    const val CALIBRATION_FAIL_FACTOR = 0.90
    const val CALIBRATION_VOLUME_REPS = 7
    const val CALIBRATION_STRENGTH_REPS = 5
    const val CALIBRATION_TARGET_RIR = 2

    // Especificidad (R16)
    const val SPECIFIC_TIER_RATIO = 0.85
    const val PEAKING_TIER_RATIO  = 0.95
    const val BASE_VOLUME_REP_MIN = 7
    const val BASE_VOLUME_REP_MAX = 10
    const val BASE_STRENGTH_REP_MIN = 4
    const val BASE_STRENGTH_REP_MAX = 6
    const val SPECIFIC_VOLUME_REP_MIN = 6
    const val SPECIFIC_VOLUME_REP_MAX = 8
    const val SPECIFIC_STRENGTH_REP_MIN = 3
    const val SPECIFIC_STRENGTH_REP_MAX = 5
    const val PEAKING_VOLUME_REP_MIN = 6
    const val PEAKING_VOLUME_REP_MAX = 8
    const val PEAKING_STRENGTH_REP_MIN = 2
    const val PEAKING_STRENGTH_REP_MAX = 4
    const val BASE_TARGET_RIR = 2
    const val SPECIFIC_VOLUME_TARGET_RIR = 2
    const val SPECIFIC_STRENGTH_TARGET_RIR = 1
    const val PEAKING_VOLUME_TARGET_RIR = 2
    const val PEAKING_STRENGTH_TARGET_RIR = 1

    // Limite de producto (R10)
    const val MAX_PRIMARY_EXERCISES = 3
}
