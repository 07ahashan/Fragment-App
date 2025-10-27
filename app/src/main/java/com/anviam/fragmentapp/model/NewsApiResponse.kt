package com.anviam.fragmentapp.model

data class NewsApiResponse(
	val articles: Articles? = null
)

data class ResultsItem(
	val date: String? = null,
	val dateTime: String? = null,
	val image: String? = null,
	val eventUri: Any? = null,
	val sentiment: Any? = null,
	val wgt: Int? = null,
	val dataType: String? = null,
	val dateTimePub: String? = null,
	val source: Source? = null,
	val title: String? = null,
	val body: String? = null,
	val uri: String? = null,
	val url: String? = null,
	val relevance: Int? = null,
	val sim: Int? = null,
	val time: String? = null,
	val lang: String? = null,
	val isDuplicate: Boolean? = null,
	val authors: List<AuthorsItem?>? = null
)

data class AuthorsItem(
	val isAgency: Boolean? = null,
	val name: String? = null,
	val type: String? = null,
	val uri: String? = null
)

data class Articles(
	val totalResults: Int? = null,
	val pages: Int? = null,
	val page: Int? = null,
	val results: List<ResultsItem?>? = null
)

data class Source(
	val dataType: String? = null,
	val title: String? = null,
	val uri: String? = null
)

