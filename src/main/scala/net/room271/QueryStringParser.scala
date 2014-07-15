package net.room271

object QueryStringParser {

  sealed trait NumberComparison
  case class GreaterThan(value: Int) extends NumberComparison
  case class LessThan(value: Int) extends NumberComparison

  case class SearchFilters(
    q: List[String],
    ingredient: Option[String],
    maxCookTime: Option[NumberComparison],
    maxPrepTime: Option[NumberComparison],
    recipeCuisine: Option[String])

  // example qs: cake+ingredient:chocolate+maxCookTime:60
  // multiple search terms are permitted and are 'AND'ed together
  // numerical filters support '<' and '>'
  def parse(s: String): SearchFilters = {
    SearchFilters(
      q = catchAllField("q", s),
      ingredient = stringField("ingredient", s),
      maxCookTime = numberComparison("maxCookTime", s),
      maxPrepTime = numberComparison("maxPrepTime", s),
      recipeCuisine = stringField("recipeCuisine", s)
    )
  }

  def numberComparison(name: String, s: String): Option[NumberComparison] = {
    val field = getField(s, _.startsWith(name + ":"))

    field flatMap { f =>
      val isGt = f.contains('>')
      val value = getValue(f)

      value map { v =>
        if (isGt) GreaterThan(v.toInt)
        else LessThan(v.toInt)
      }
    }
  }

  def catchAllField(name: String, s: String): List[String] =
    getParts(s).filterNot(_.contains(':'))

  def stringField(name: String, s: String): Option[String] = {
    val field = getField(s, _.startsWith(name + ":"))
    field flatMap getValue
  }

  def getField(s: String, f: String => Boolean): Option[String] =
    getParts(s) find f

  def getParts(s: String): List[String] = s.split(' ').toList

  def getValue(s: String): Option[String] = {
    s.drop(s.indexOf(':') + 1) match {
      case empty if empty.isEmpty => None
      case nonEmpty => Some(nonEmpty)
    }
  }
}
