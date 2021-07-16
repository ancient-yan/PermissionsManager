package com.gwchina.parent.level.presentation

internal fun Int.isAgeInMild() = this >= 16/*大等于16岁，轻度模式*/
internal fun Int.isAgeInModerate() = this in 13..15/*13到15岁，中度模式*/
internal fun Int.isAgeInSevere() = this <= 12/*小于等于12岁，重度模式*/

internal fun List<GuardLevelVO>.mildGuardLevel() = this[0]
internal fun List<GuardLevelVO>.moderateGuardLevel() = this[1]
internal fun List<GuardLevelVO>.severeGuardLevel() = this[2]
