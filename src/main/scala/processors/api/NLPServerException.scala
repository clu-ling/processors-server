package processors.api


case class NLPServerException(msg: String) extends RuntimeException(msg)

class BadURLException(msg: String, url: String) extends NLPServerException(msg) {
 def this(url: String) = this(s"'$url' does not point to a yaml file", url)
}