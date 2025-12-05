package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books

object LibrarySequences {

    // The five canonical book sequences
    val sequences: List<List<Books>> = listOf(
        listOf(
            Books.KILLING_OF_A_KING, Books.IDEOLOGY_OF_DARKNESS, Books.RADAS_JOURNEY,
            Books.TRANSVERGENCE_THEORY, Books.TRISTESSAS_TRAGEDY, Books.RADAS_CENSUS,
            Books.TREACHERY_OF_ROYALTY, Books.HOSIDIUS_LETTER, Books.RICKTORS_DIARY_7,
            Books.EATHRAM_RADA_EXTRACT, Books.VARLAMORE_ENVOY, Books.WINTERTODT_PARABLE,
            Books.TWILL_ACCORD, Books.BYRNES_CORONATION_SPEECH, Books.SOUL_JOURNEY,
            Books.TRANSPORTATION_INCANTATIONS
        ),
        listOf(
            Books.KILLING_OF_A_KING, Books.IDEOLOGY_OF_DARKNESS, Books.RADAS_JOURNEY,
            Books.TRANSVERGENCE_THEORY, Books.TRISTESSAS_TRAGEDY, Books.RADAS_CENSUS,
            Books.TREACHERY_OF_ROYALTY, Books.HOSIDIUS_LETTER, Books.VARLAMORE_ENVOY,
            Books.RICKTORS_DIARY_7, Books.EATHRAM_RADA_EXTRACT, Books.SOUL_JOURNEY,
            Books.WINTERTODT_PARABLE, Books.TWILL_ACCORD, Books.BYRNES_CORONATION_SPEECH,
            Books.TRANSPORTATION_INCANTATIONS
        ),
        listOf(
            Books.RICKTORS_DIARY_7, Books.VARLAMORE_ENVOY, Books.EATHRAM_RADA_EXTRACT,
            Books.IDEOLOGY_OF_DARKNESS, Books.RADAS_CENSUS, Books.KILLING_OF_A_KING,
            Books.TREACHERY_OF_ROYALTY, Books.HOSIDIUS_LETTER, Books.BYRNES_CORONATION_SPEECH,
            Books.SOUL_JOURNEY, Books.WINTERTODT_PARABLE, Books.TWILL_ACCORD,
            Books.RADAS_JOURNEY, Books.TRANSVERGENCE_THEORY, Books.TRISTESSAS_TRAGEDY,
            Books.TRANSPORTATION_INCANTATIONS
        ),
        listOf(
            Books.RADAS_CENSUS, Books.RICKTORS_DIARY_7, Books.EATHRAM_RADA_EXTRACT,
            Books.KILLING_OF_A_KING, Books.HOSIDIUS_LETTER, Books.WINTERTODT_PARABLE,
            Books.TWILL_ACCORD, Books.BYRNES_CORONATION_SPEECH, Books.IDEOLOGY_OF_DARKNESS,
            Books.RADAS_JOURNEY, Books.TRANSVERGENCE_THEORY, Books.TRISTESSAS_TRAGEDY,
            Books.TREACHERY_OF_ROYALTY, Books.TRANSPORTATION_INCANTATIONS, Books.SOUL_JOURNEY,
            Books.VARLAMORE_ENVOY
        ),
        listOf(
            Books.RADAS_CENSUS, Books.TRANSVERGENCE_THEORY, Books.TREACHERY_OF_ROYALTY,
            Books.RADAS_JOURNEY, Books.KILLING_OF_A_KING, Books.VARLAMORE_ENVOY,
            Books.BYRNES_CORONATION_SPEECH, Books.HOSIDIUS_LETTER, Books.TRISTESSAS_TRAGEDY,
            Books.RICKTORS_DIARY_7, Books.IDEOLOGY_OF_DARKNESS, Books.WINTERTODT_PARABLE,
            Books.TWILL_ACCORD, Books.SOUL_JOURNEY, Books.EATHRAM_RADA_EXTRACT,
            Books.TRANSPORTATION_INCANTATIONS
        )
    )

    val allSequenceBooks: Set<Books> = sequences.flatten().toSet()
}