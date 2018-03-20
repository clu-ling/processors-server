package processors

import org.clulab.odin.{ ExtractorEngine, Mention }
import org.clulab.processors.{ Document, Processor, Sentence }
import org.clulab.processors.clu._
import org.clulab.openie.entities.RuleBasedEntityFinder
import org.json4s.JsonAST.JValue

import scala.util.{ Failure, Success, Try }

object ProcessorsBridge {

  // initialize a processor
  // withDiscourse is disabled to control memory consumption
  lazy val clustanford: Processor = new CluProcessorWithStanford()
  lazy val clubio: Processor = new BioCluProcessor()
  lazy val clu: Processor = new CluProcessor()
  lazy val ef = RuleBasedEntityFinder(maxHops = 3)

  // fastnlp has an NER component plugged in
  val defaultProc: Processor = clu

  def mkDocument(sentence: Sentence): Document = Document(Array(sentence))

  /** annotate text */
  def annotate(text: String): Document = toAnnotatedDocument(text, defaultProc)
  def annotateWithCluStanford(text: String): Document = toAnnotatedDocument(text, clustanford)
  def annotateWithCluBio(text: String): Document = toAnnotatedDocument(text, clubio)
  def annotateWithClu(text: String): Document = toAnnotatedDocument(text, clu)

  /** Avoid sentence splitting */
  def annotate(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, defaultProc)
  def annotateWithCluStanford(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, clustanford)
  def annotateWithCluBio(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, clubio)
  def annotateWithClu(sentences: Seq[String]): Document = toAnnotatedDocument(sentences, clu)
  def toAnnotatedDocument(sentences: Seq[String], proc: Processor): Document = proc.annotateFromSentences(sentences, keepText = true)

  def tokenize(text: String): Document = {
    clu.mkDocument(text, keepText = true)
  }

  def lemmatize(text: String): Document = {
    val doc = tokenize(text)
    clu.lemmatize(doc)
    doc
  }

  def lemmatize(sentence: Sentence): Sentence = {
    val doc = mkDocument(sentence)
    val lemmatizedDoc = lemmatize(doc)
    lemmatizedDoc.sentences.head
  }

  def lemmatize(doc: Document): Document = {
    clu.lemmatize(doc)
    doc
  }

  def tag(text: String): Document = {
    val doc = tokenize(text)
    val lemmatizedDoc = lemmatize(doc)
    tag(lemmatizedDoc)
  }

  def tag(sentence: Sentence): Sentence = {
    // CluProcessor's pos tagger uses lemmas as features
    val doc = if (sentence.lemmas.isEmpty) lemmatize(mkDocument(sentence)) else mkDocument(sentence)
    tag(doc).sentences.head
  }

  def tag(doc: Document): Document = {
    clu.tagPartsOfSpeech(doc)
    doc
  }

  // FIXME: what features does the chunker require?
  def chunk(text: String): Document = {
    val doc = clu.mkDocument(text)
    clu.chunking(doc)
    doc
  }

  def chunk(sentence: Sentence): Sentence = {
    val doc = mkDocument(sentence)
    val doc2 = chunk(doc)
    doc2.sentences.head
  }

  def chunk(doc: Document): Document = {
    clu.chunking(doc)
    doc
  }

  // FIXME: what features does the NER require?
  def ner(text: String): Document = {
    val doc = clu.mkDocument(text)
    clu.recognizeNamedEntities(doc)
    doc
  }

  def ner(sentence: Sentence): Sentence = {
    val doc = mkDocument(sentence)
    val doc2 = ner(doc)
    doc2.sentences.head
  }

  def ner(doc: Document): Document = {
    clu.recognizeNamedEntities(doc)
    doc
  }

  // FIXME: do we need to ensure tags are Penn-style?
  def parseStanford(sentence: Sentence): Sentence = {
    val doc = mkDocument(sentence)
    val parsedDoc = parseStanford(doc)
    parsedDoc.sentences.head
  }

  def parseStanford(doc: Document): Document = {
    clustanford.parse(doc)
    doc
  }

  // FIXME: do we need to ensure tags are universal?
  def parseUniversal(sentence: Sentence): Sentence = {
    val doc = mkDocument(sentence)
    val parsedDoc = parseUniversal(doc)
    parsedDoc.sentences.head
  }

  def parseUniversal(doc: Document): Document = {
    clu.parse(doc)
    doc
  }

  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String, proc: Processor): Document = {
    proc.annotate(text, keepText = true)
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
