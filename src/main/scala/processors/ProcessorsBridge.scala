package processors

import _root_.processors.api.SentimentScores
import edu.arizona.sista.odin.ExtractorEngine
import edu.arizona.sista.processors
import edu.arizona.sista.processors.Processor
import edu.arizona.sista.processors.corenlp.CoreNLPSentimentAnalyzer
import edu.arizona.sista.processors.bionlp.BioNLPProcessor
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor


object ProcessorsBridge {

  // initialize a processor
  // withDiscourse is disabled to control memory consumption
  lazy val fastnlp = new FastNLPProcessor(withDiscourse = false)
  lazy val bionlp = new BioNLPProcessor(withDiscourse = false)

  val defaultProc = fastnlp

  /** annotate text */
  def annotate(text: String): processors.Document = toAnnotatedDocument(text, defaultProc)
  def annotateWithFastNLP(text: String): processors.Document = toAnnotatedDocument(text, fastnlp)
  def annotateWithBioNLP(text: String): processors.Document = toAnnotatedDocument(text, bionlp)

  /** Avoid sentence splitting */
  def annotate(sentences: Seq[String]): processors.Document = toAnnotatedDocument(sentences, defaultProc)
  def annotateWithFastNLP(sentences: Seq[String]): processors.Document = toAnnotatedDocument(sentences, fastnlp)
  def annotateWithBioNLP(sentences: Seq[String]): processors.Document = toAnnotatedDocument(sentences, bionlp)
  def toAnnotatedDocument(sentences: Seq[String], proc: Processor): processors.Document = proc.annotateFromSentences(sentences, true)

  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String, proc: Processor): processors.Document = {
    proc.annotate(text)
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

  def toSentimentScores(sentence: processors.Sentence): SentimentScores = {
    val score = CoreNLPSentimentAnalyzer.sentiment(sentence)
    SentimentScores(Seq(score))
  }

  def toSentimentScores(doc: processors.Document): SentimentScores = {
    val scores = CoreNLPSentimentAnalyzer.sentiment(doc)
    SentimentScores(scores)
  }

  def getMentions(doc: processors.Document, rules: String): Seq[api.Mention] = {
    val engine = ExtractorEngine(rules)
    val odinMentions = engine.extractFrom(doc)

    val mentions = for {
      om <- odinMentions
    } yield ConverterUtils.toMention(om)

    mentions
  }
}