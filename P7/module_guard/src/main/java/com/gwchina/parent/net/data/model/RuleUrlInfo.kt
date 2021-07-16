package com.gwchina.parent.net.data.model


//{
//    "pattern_id": "D39D9F44C0A80A452FCBF46DB14F6EDE",
//    "pattern_type": "1",
//    "week_intercept_count": 6,
//    "total_intercept_count": 777
//}
/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-15 17:31
 *      Desc :
 */
data class RuleUrlInfo(val pattern_id: String = "",
                       var pattern_type: String? = "",
                       val week_intercept_count: Int = 0,
                       val total_intercept_count: Int = 0)