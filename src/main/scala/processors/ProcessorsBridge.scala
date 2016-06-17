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
  lazy val fastnlp = new FastNLPProcessor()
  lazy val bionlp = new BioNLPProcessor()

  val defaultProc = fastnlp

  def annotate(text: String): processors.Document = toAnnotatedDocument(text, defaultProc)
  def annotateWithFastNLP(text: String): processors.Document = toAnnotatedDocument(text, fastnlp)
  def annotateWithBioNLP(text: String): processors.Document = toAnnotatedDocument(text, bionlp)

  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String, proc: Processor): processors.Document = {
    proc.annotate(text)
  }

  def toSentimentScores(text: String): SentimentScores = {
    val scores = CoreNLPSentimentAnalyzer.sentiment(text)
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