package jpark.bro.pacenote.di

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import org.koin.dsl.module

val buildTypeModule = module { }

/** release 빌드의 임시 홈에는 추가 항목이 없다. */
@Suppress("UnusedReceiverParameter")
@Composable
fun ColumnScope.BuildTypeHomeExtras() = Unit
