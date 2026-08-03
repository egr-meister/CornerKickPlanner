package com.cornerkick.planner.ui

import com.cornerkick.planner.data.model.ArrowType
import com.cornerkick.planner.data.model.AttackSide
import com.cornerkick.planner.data.model.CornerType
import com.cornerkick.planner.data.model.MarkerType
import com.cornerkick.planner.data.model.TeamColorRole

/** Human-readable labels for enums. Keeps UI text consistent and English-only. */
object Labels {
    fun cornerType(type: CornerType): String = when (type) {
        CornerType.NearPost -> "Near Post"
        CornerType.FarPost -> "Far Post"
        CornerType.ShortCorner -> "Short Corner"
        CornerType.Custom -> "Custom"
    }

    fun attackSide(side: AttackSide): String = when (side) {
        AttackSide.LeftCorner -> "Left Corner"
        AttackSide.RightCorner -> "Right Corner"
    }

    fun markerType(type: MarkerType): String = when (type) {
        MarkerType.Attacker -> "Attacker"
        MarkerType.Defender -> "Defender"
        MarkerType.Goalkeeper -> "Goalkeeper"
        MarkerType.Ball -> "Ball"
        MarkerType.Target -> "Target"
    }

    fun teamRole(role: TeamColorRole): String = when (role) {
        TeamColorRole.Attack -> "Attack"
        TeamColorRole.Defense -> "Defense"
        TeamColorRole.Neutral -> "Neutral"
    }

    fun arrowType(type: ArrowType): String = when (type) {
        ArrowType.Run -> "Run"
        ArrowType.Pass -> "Pass"
        ArrowType.Cross -> "Cross"
        ArrowType.DummyMove -> "Dummy Move"
        ArrowType.PressingArrow -> "Pressing Arrow"
    }

    /** Default team role suggested for a marker type. */
    fun defaultRole(type: MarkerType): TeamColorRole = when (type) {
        MarkerType.Attacker -> TeamColorRole.Attack
        MarkerType.Defender -> TeamColorRole.Defense
        MarkerType.Goalkeeper -> TeamColorRole.Defense
        MarkerType.Ball -> TeamColorRole.Neutral
        MarkerType.Target -> TeamColorRole.Neutral
    }
}
