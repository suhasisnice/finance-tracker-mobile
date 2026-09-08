package com.finsetpacer.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finsetpacer.app.data.db.TransactionEntity
import com.finsetpacer.app.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> = transactionRepository.observeCurrentMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
