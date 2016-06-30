import com.typesafe.scalalogging.LazyLogging
import org.clulab.processors


package object processors extends LazyLogging {

  def displaySentence(sentence: processors.Sentence): Unit = {
    logger.debug(s"""
        |processors.Sentence words: ${sentence.words.mkString(" ")}
        |processors.Sentence startOffsets: ${sentence.startOffsets.mkString(" ")}
        |processors.Sentence endOffsets: ${sentence.endOffsets.mkString(" ")}
        |processors.Sentence lemmas: ${sentence.lemmas.get.mkString(" ")}
        |processors.Sentence tags: ${sentence.tags.get.mkString(" ")}
        |processors.Sentence entities: ${sentence.entities.get.mkString(" ")}
        |dependency edges: ${sentence.dependencies.get.allEdges.mkString(" ")}
        |dependency roots: ${sentence.dependencies.get.roots.mkString(" ")}
    """.stripMargin)
  }
}
