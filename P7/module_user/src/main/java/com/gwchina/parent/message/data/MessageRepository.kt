package com.gwchina.parent.message.data

import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-23 12:03
 */
class MessageRepository @Inject constructor(private val messageApi: MessageApi) {

    fun messageList(position: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Observable<Optional<List<Message>>> {
        return messageApi.loadMessageList(position, pageSize)
                .optionalExtractor()
    }

    fun deleteMessageList(): Completable {
        return messageApi.deleteMessageList()
                .resultChecker()
                .ignoreElements()
    }

}