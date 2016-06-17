package processors

import _root_.processors.api.{Edge, Dependencies}
import edu.arizona.sista.odin.{TextBoundMention, EventMention, RelationMention}
import edu.arizona.sista.processors
import edu.arizona.sista.struct.Interval
import edu.arizona.sista.odin
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

  def toMention(mention: odin.Mention): api.Mention = mention match {

    case tb: odin.TextBoundMention =>
      api.Mention(
        label = tb.label,
        labels = tb.labels.toSet,
        arguments = Map.empty[String, Seq[api.Mention]],
        trigger = None,
        start = tb.tokenInterval.start,
        end = tb.tokenInterval.end,
        sentence = tb.sentence,
        document = toDocument(tb.document),
        keep = tb.keep,
        foundBy = tb.foundBy
      )

    case em: odin.EventMention =>
      api.Mention(
        label = em.label,
        labels = em.labels.toSet,
        arguments = toArguments(em.arguments),
        trigger = Some(toMention(em.trigger)),
        start = em.tokenInterval.start,
        end = em.tokenInterval.end,
        sentence = em.sentence,
        document = toDocument(em.document),
        keep = em.keep,
        foundBy = em.foundBy
      )

    case rel: odin.RelationMention =>
      api.Mention(
        label = rel.label,
        labels = rel.labels.toSet,
        arguments = toArguments(rel.arguments),
        trigger = None,
        start = rel.tokenInterval.start,
        end = rel.tokenInterval.end,
        sentence = rel.sentence,
        document = toDocument(rel.document),
        keep = rel.keep,
        foundBy = rel.foundBy
      )
  }

  def toOdinMention(mention: api.Mention): odin.Mention = mention match {

    case tb if tb.arguments.isEmpty && tb.trigger.isEmpty =>
      new TextBoundMention(
        labels = tb.labels.toSeq,
        tokenInterval = Interval(tb.start, tb.end),
        sentence = tb.sentence,
        document = toProcessorsDocument(tb.document),
        keep = tb.keep,
        foundBy = tb.foundBy
      )

    case em if em.arguments.nonEmpty && em.trigger.isDefined =>
      new EventMention(
        labels = em.labels.toSeq,
        trigger = toOdinMention(em.trigger.get).asInstanceOf[TextBoundMention],
        arguments = toOdinArguments(em.arguments),
        sentence = em.sentence,
        document = toProcessorsDocument(em.document),
        keep = em.keep,
        foundBy = em.foundBy
      )

    case rel if rel.arguments.nonEmpty && rel.trigger.isEmpty =>
      new RelationMention(
        labels = rel.labels.toSeq,
        arguments = toOdinArguments(rel.arguments),
        sentence = rel.sentence,
        document = toProcessorsDocument(rel.document),
        keep = rel.keep,
        foundBy = rel.foundBy
      )
  }

  def toArguments(arguments: Map[String, Seq[odin.Mention]]): Map[String, Seq[api.Mention]] = {
    arguments.mapValues(_.map(toMention))
  }

  def toOdinArguments(arguments: Map[String, Seq[api.Mention]]): Map[String, Seq[odin.Mention]] = {
    arguments.mapValues(_.map(toOdinMention))
  }
}
