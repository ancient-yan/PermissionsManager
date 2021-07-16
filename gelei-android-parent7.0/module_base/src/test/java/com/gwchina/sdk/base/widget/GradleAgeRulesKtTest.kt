package com.gwchina.sdk.base.widget

import com.gwchina.sdk.base.utils.getRecommendedGradeByBirthday
import org.junit.Test

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-04-12 11:34
 */
class GradleAgeRulesKtTest {

    @Test
    fun testGetRecommendedGradleByBirthday() {
        println(getRecommendedGradeByBirthday(1998, 4,1))
        println(getRecommendedGradeByBirthday(1999, 4,1))
        println(getRecommendedGradeByBirthday(2002, 1,1))
        println(getRecommendedGradeByBirthday(2019, 4,1))
        println(getRecommendedGradeByBirthday(2013, 8,1))
        println(getRecommendedGradeByBirthday(2019, 5,14))
    }

}