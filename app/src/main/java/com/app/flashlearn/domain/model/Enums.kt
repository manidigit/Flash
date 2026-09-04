package com.app.flashlearn.domain.model

enum class ContentType { WORD, PHRASE, SENTENCE, IDIOM, VERB, EXPRESSION, DIALOGUE }
enum class ReviewStage { DAILY, WEEKLY, MONTHLY, LEARNED }
enum class Difficulty { EASY, MEDIUM, HARD, VERY_HARD }

private val difficultyOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD, Difficulty.VERY_HARD)
operator fun Difficulty.compareTo(other: Difficulty) =
    difficultyOrder.indexOf(this) - difficultyOrder.indexOf(other)
