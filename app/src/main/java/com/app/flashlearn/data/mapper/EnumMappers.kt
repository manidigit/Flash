package com.app.flashlearn.data.mapper

import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.ReviewStage

fun String.toContentType() = ContentType.valueOf(this)
fun ContentType.toDbString() = this.name

fun String.toReviewStage() = ReviewStage.valueOf(this)
fun ReviewStage.toDbString() = this.name

fun String.toDifficulty() = Difficulty.valueOf(this)
fun Difficulty.toDbString() = this.name
