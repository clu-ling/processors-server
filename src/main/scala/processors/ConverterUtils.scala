package processors

import _root_.processors.api.{Edge, Dependencies}
import edu.arizona.sista.processors
import edu.arizona.sista.processors.DependencyMap
import edu.arizona.sista.struct.DirectedGraph


object ConverterUtils {

  def toDependencies(deps: DirectedGraph[String]): Dependencies = {

    val edges = for {
      (src, dest, rel) <- deps.allEdges
    } yield Edge(
        destination = dest,
        source = src,
        relation = rel
      )
    Dependencies(edges, deps.roots)
  }

  def toProcessorsDirectedGraph(deps: api.Dependencies): DependencyMap = {

    val recoveredEdges = deps.edges.map(e => (e.source, e.destination, e.relation))
    val dg = new DirectedGraph[String](edges = recoveredEdges.toList, roots = deps.roots)
    val dm = new DependencyMap()
    // 1 is for Stanford collapsed
    dm(1) = dg
    dm
  }

  def toProcessorsSentence(s: api.Sentence): processors.Sentence = {
    
    new processors.Sentence(
      s.words.toArray,
      startOffsets = s.startOffsets.toArray,
      endOffsets = s.endOffsets.toArray,
      tags = if (s.tags.nonEmpty) Some(s.tags.toArray) else None,
      lemmas = if (s.lemmas.nonEmpty) Some(s.lemmas.toArray) else None,
      entities = if (s.entities.nonEmpty) Some(s.entities.toArray) else None,
      norms = None,
      chunks = None,
      syntacticTree = None,
      dependenciesByType = toProcessorsDirectedGraph(s.dependencies)
    )
  }

  def toSentence(s: processors.Sentence): api.Sentence = {

    api.Sentence(
      words = s.words,
      startOffsets = s.startOffsets,
      endOffsets = s.endOffsets,
      lemmas = if (s.lemmas.nonEmpty) s.lemmas.get else Nil,
      tags =  if (s.tags.nonEmpty) s.tags.get else Nil,
      entities =  if (s.entities.nonEmpty) s.entities.get else Nil,
      dependencies = toDependencies(s.dependencies.get)
    )
  }

  def toProcessorsDocument(doc: api.Document): processors.Document = {
    val sentences = doc.sentences.map(toProcessorsSentence)
    new processors.Document(sentences.toArray)
  }

  def toDocument(doc: processors.Document): api.Document = {
    val text: String = if (doc.text.nonEmpty) doc.text.get else doc.sentences.map(_.words.mkString(" ")).mkString(" ")
    val sentences: Seq[api.Sentence] = doc.sentences.map(toSentence)
    api.Document(text, sentences)
  }

}
