package com.github.h0tk3y.synsetOps

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

/**
 * Created by igushs on 5/31/16.
 */

class Quote(val sentence: String)

class Meaning(
        @JacksonXmlElementWrapper(useWrapping = false)
        val quote: List<Quote>
)

class Page(
        val title: String,

        @JacksonXmlElementWrapper(useWrapping = false)
        val meaning: List<Meaning>
)

class WiktionaryModel(
        @JacksonXmlElementWrapper(useWrapping = false)
        val page: List<Page>
) {
    companion object {
        fun parse(from: String): WiktionaryModel {
            val mapper = XmlMapper().registerKotlinModule().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            return mapper.readValue<WiktionaryModel>(from)
        }
    }

    fun toWordsQuotesMap(): Map<String, List<String>> =
            page.associateBy({ it.title }) { it.meaning.map { it.quote.first().sentence } }
}