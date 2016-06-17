package processors

import edu.arizona.sista.processors.Processor
import edu.arizona.sista.processors.corenlp.CoreNLPSentimentAnalyzer
import edu.arizona.sista.processors.bionlp.BioNLPProcessor
import edu.arizona.sista.processors.fastnlp.FastNLPProcessor


object ProcessorsBridge {

  // initialize a processor
  lazy val fastnlp = new FastNLPProcessor()
  lazy val bionlp = new BioNLPProcessor()

  def annotateWithFastNLP(text: String): api.Document = toAnnotatedDocument(text, fastnlp)
  def annotateWithBioNLP(text: String): api.Document = toAnnotatedDocument(text, bionlp)

  // convert processors document to a json-serializable format
  def toAnnotatedDocument(text: String, proc: Processor): api.Document = {
    val doc = proc.annotate(text)
    val sentences =
      for {(s, i) <- doc.sentences.zipWithIndex
           words = s.words
           startOffsets = s.startOffsets
           endOffsets = s.endOffsets
           lemmas = if (s.lemmas.nonEmpty) s.lemmas.get.toSeq else Nil
           tags = if (s.tags.nonEmpty) s.tags.get.toSeq else Nil
           entities = if (s.entities.nonEmpty) s.entities.get.toSeq else Nil
           deps = ConverterUtils.toDependencies(s.dependencies.get)
           // reformatting the dependencies as a List of maps results in a more readable json output
      } yield api.Sentence(words, startOffsets, endOffsets, lemmas, tags, entities, deps)
    api.Document(text, sentences)
  }

//  def textToJSON(text: String): JValue =
//    docToJSON(
//      annotateWithFastNLP(text)
//    )

  def docToJSON(doc: Document): JValue = {
    implicit val formats = Serialization.formats(NoTypeHints)
    val json = write(doc)
    json
  }

  def toSentimentScores(text: String): SentimentScores = {
    val scores = CoreNLPSentimentAnalyzer.sentiment(text)
    SentimentScores(scores)
  }
}