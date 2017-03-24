package processors

import _root_.processors.api.SentimentScores
import org.clulab.odin.{ExtractorEngine, Mention}
import org.clulab.processors.{Document, Processor, Sentence}
import org.clulab.processors.corenlp.CoreNLPSentimentAnalyzer
import org.clulab.processors.bionlp.BioNLPProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.clulab.processors.shallownlp.ShallowNLPProcessor


object ProcessorsBridge {

  // initialize a processor
  // withDiscourse is disabled to control memory consumption
  lazy val fastnlp = new FastNLPProcessor(withDiscourse = ShallowNLPProcessor.NO_DISCOURSE)
  lazy val bionlp = new BioNLPProcessor(withChunks = false, withDiscourse = ShallowNLPProcessor.NO_DISCOURSE)

  val defaultProc = fastnlp

  /** annotate text */
  def annotate(text: String): Document = toAnnotatedDocument(text, defaultProc)
  def annotateWithFastNLP(text: String): Document = toAnnotatedDocument(text, fastnlp)
  def annotateWithBioNLP(text: String): Document = toAnnotatedDocument(text, bionlp)

  /** Avoid sentence splitting */
  def annotate(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, defaultProc)
  def annotateWithFastNLP(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, fastnlp)
  def annotateWithBioNLP(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, bionlp)
  def toAnnotatedDocument(sentences: Seq[String], proc: Processor): Document = proc.annotateFromSentences(sentences, keepText = true)

  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String, proc: Processor): Document = {
    proc.annotate(text, keepText = true)
  }

  def toSentimentScores(text: String): SentimentScores = {
    val scores = CoreNLPSentimentAnalyzer.sentiment(text)
    SentimentScores(scores)
  }

  /** Get sentiment scores for text already segmented into sentences */
  def toSentimentScores(sentences: Seq[String]): SentimentScores = {
    val doc = annotateWithFastNLP(sentences)
    val scores = CoreNLPSentimentAnalyzer.sentiment(doc)
    SentimentScores(scores)
  }

  def toSentimentScores(sentence: Sentence): SentimentScores = {
    val score = CoreNLPSentimentAnalyzer.sentiment(sentence)
    SentimentScores(Seq(score))
  }

  def toSentimentScores(doc: Document): SentimentScores = {
    val scores = CoreNLPSentimentAnalyzer.sentiment(doc)
    SentimentScores(scores)
  }

  def getMentions(doc: Document, rules: String): Seq[Mention] = {
    val engine = ExtractorEngine(rules)
    val odinMentions = engine.extractFrom(doc)
    odinMentions
  }
}