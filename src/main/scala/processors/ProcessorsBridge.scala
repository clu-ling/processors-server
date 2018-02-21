package processors

import _root_.processors.api.SentimentScores
import org.clulab.odin.{ ExtractorEngine, Mention }
import org.clulab.processors.{ Document, Processor, Sentence }
import org.clulab.processors.corenlp.CoreNLPSentimentAnalyzer
import org.clulab.processors.bionlp.BioNLPProcessor
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.fastnlp.FastNLPProcessor
import org.clulab.processors.shallownlp.ShallowNLPProcessor
import org.clulab.openie.entities.RuleBasedEntityFinder
import org.json4s.JsonAST.JValue

import scala.util.{ Failure, Success, Try }

object ProcessorsBridge {

  // initialize a processor
  // withDiscourse is disabled to control memory consumption
  lazy val fastnlp = new FastNLPProcessor(withDiscourse = ShallowNLPProcessor.NO_DISCOURSE)
  lazy val bionlp = new BioNLPProcessor(withChunks = false, withDiscourse = ShallowNLPProcessor.NO_DISCOURSE)
  lazy val clu = new CluProcessor()
  lazy val ef = RuleBasedEntityFinder(maxHops = 3)

  // fastnlp has an NER component plugged in
  val defaultProc: Processor = fastnlp

  /** annotate text */
  def annotate(text: String): Document = toAnnotatedDocument(text, defaultProc)
  def annotateWithFastNLP(text: String): Document = toAnnotatedDocument(text, fastnlp)
  def annotateWithBioNLP(text: String): Document = toAnnotatedDocument(text, bionlp)
  def annotateWithClu(text: String): Document = toAnnotatedDocument(text, clu)

  /** Avoid sentence splitting */
  def annotate(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, defaultProc)
  def annotateWithFastNLP(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, fastnlp)
  def annotateWithBioNLP(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, bionlp)
  def annotateWithClu(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, clu)
  def toAnnotatedDocument(sentences: Seq[String], proc: Processor): Document = proc.annotateFromSentences(sentences, keepText = true)

  /** Generate chunk labels */
  def chunkWithFastNLP(sentence: Sentence): Sentence = {
    val words = sentence.words
    val tags = sentence.tags.get
    val chunks = fastnlp.chunker.classify(words, tags)
    sentence.chunks = Some(chunks)
    sentence
  }

  def chunkWithFastNLP(doc: Document): Document = {
    val chunkedSentences = doc.sentences.map(chunkWithFastNLP)
    Document(sentences = chunkedSentences)
  }

//  /** Generate lemma labels */
//  def lemmatizeWithFastNLP(sentence: Sentence): Sentence = {
//    val doc = Document(sentences = Array(sentence))
//    val lemmatizedDoc = lemmatizeWithFastNLP(doc)
//    lemmatizedDoc.sentences.head
//  }
//
//  def lemmatizeWithFastNLP(doc: Document): Document = {
//    val coreDoc = CoreNLPUtils.mkCoreDocument(doc)
//    fastnlp.lemmatize(coreDoc)
//    coreDoc
//  }
//
//  /** Generate PoS labels */
//  def tagPartsOfSpeechWithFastNLP(sentence: Sentence): Sentence = {
//    val doc = Document(sentences = Array(sentence))
//    val taggedDoc = tagPartsOfSpeechWithFastNLP(doc)
//    taggedDoc.sentences.head
//  }
//
//  def tagPartsOfSpeechWithFastNLP(doc: Document): Document = {
//    val coreDoc = CoreNLPUtils.mkCoreDocument(doc)
//    fastnlp.tagPartsOfSpeech(coreDoc)
//    coreDoc
//  }

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

  def getMentionsAsJSON(doc: Document, rules: String): JValue = {
    Try(getMentions(doc, rules)) match {
      case Success(mentions) => ConverterUtils.toJSON(mentions)
      case Failure(error)    => ConverterUtils.toJSON(error)
    }
  }

  // openie entity finder extract and filter
  def extractAndFilterEntities(doc: Document): JValue = {
    val mentions = ef.extractAndFilter(doc)
    ConverterUtils.toJSON(mentions)
  }

  def extractAndFilterEntities(sentence: Sentence): JValue = {
    val doc = Document(Array(sentence))
    extractAndFilterEntities(doc)
  }

  // openie entity finder base
  def extractBaseEntities(doc: Document): JValue = {
    val mentions = ef.extractBaseEntities(doc)
    ConverterUtils.toJSON(mentions)
  }

  def extractBaseEntities(sentence: Sentence): JValue = {
    val doc = Document(Array(sentence))
    extractBaseEntities(doc)
  }

  // openie entity finder
  def extractEntities(doc: Document): JValue = {
    val mentions = ef.extract(doc)
    ConverterUtils.toJSON(mentions)
  }

  def extractEntities(sentence: Sentence): JValue = {
    val doc = Document(Array(sentence))
    extractEntities(doc)
  }
}
