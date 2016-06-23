package processors.api

import org.scalatest.{Matchers, FlatSpec}
import processors.ProcessorsBridge


class TestProcessorsBridge extends FlatSpec with Matchers {

  val preSplitText = Seq("this is a sentence", "...and this is a sentence", "and so is this")

  "ProcessorsBridge.annotate" should "produce a processors.Document from a pre-split sequence of text" in {
    val doc = ProcessorsBridge.annotate(preSplitText)
    doc.sentences.size == preSplitText.length should be(true)
  }

  val textForSentimentAnalysis = "If you want to make an apple pie from scratch, you must first create the universe. It is far better to grasp the universe as it really is than to persist in delusion, however satisfying and reassuring."

  "ProcessorsBridge.toSentimentScores" should "produce scores from text (one score for each sentence)" in {
    val ss = ProcessorsBridge.toSentimentScores(textForSentimentAnalysis)
    ss.isInstanceOf[SentimentScores] should be(true)
    ss.scores.nonEmpty should be(true)
  }

  it should "produce scores from a processors.Document (one score for each sentence)" in {
    val doc = ProcessorsBridge.annotate(textForSentimentAnalysis)
    val ss = ProcessorsBridge.toSentimentScores(doc)
    ss.isInstanceOf[SentimentScores] should be(true)
    ss.scores.nonEmpty should be(true)
  }

  it should "produce a single score from a processors.Sentence" in {
    val doc = ProcessorsBridge.annotate(textForSentimentAnalysis)
    val sentence = doc.sentences.head
    val ss = ProcessorsBridge.toSentimentScores(sentence)
    ss.isInstanceOf[SentimentScores] should be(true)
    ss.scores.size should be(1)
  }

  it should "produce scores from a pre-split sequence of text (one score for each sentence)" in {
    val ss = ProcessorsBridge.toSentimentScores(preSplitText)
    ss.isInstanceOf[SentimentScores] should be(true)
    ss.scores.nonEmpty should be(true)
    ss.scores.size == preSplitText.size should be(true)
  }
}
