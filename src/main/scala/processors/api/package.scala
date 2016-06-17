package processors

import com.typesafe.scalalogging.LazyLogging


package object api extends LazyLogging {

  def displaySentence(s: api.Sentence): Unit = {
    logger.debug(s"""
        |api.Sentence words: ${s.words.mkString(" ")}
        |api.Sentence startOffsets: ${s.startOffsets.mkString(" ")}
        |api.Sentence endOffsets: ${s.endOffsets.mkString(" ")}
        |api.Sentence lemmas: ${s.lemmas.mkString(" ")}
        |api.Sentence tags: ${s.tags.mkString(" ")}
        |api.Sentence entities: ${s.entities.mkString(" ")}
        |dependency edges: ${s.dependencies.edges.mkString(" ")}
        |dependency roots: ${s.dependencies.roots.mkString(" ")}
    """.stripMargin)
  }

}
