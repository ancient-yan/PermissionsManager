package com.gwchina.parent.message.presentation

import com.android.base.app.mvvm.ArchViewModel
import com.android.base.rx.SchedulerProvider
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.message.data.MessageRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/*
Direct return the source type of Rx from ViewModel, This is just a trying and do not imitate.
 */
/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-24 15:37
 */
class MessageViewModel @Inject constructor(
        private val messageRepository: MessageRepository,
        private val schedulerProvider: SchedulerProvider,
        private val messageMapper:MessageMapper
) : ArchViewModel() {

    fun loadMessage(position: Int = 0): Observable<Optional<List<MessageWrapper>>> {
        return messageRepository.messageList(position)
                .map {
                    Optional.ofNullable(messageMapper.map(it.orElse(null)))
                }
                .observeOn(schedulerProvider.ui())
    }

    fun clearMessage(): Completable {
        return messageRepository.deleteMessageList()
                .observeOn(schedulerProvider.ui())
    }

}