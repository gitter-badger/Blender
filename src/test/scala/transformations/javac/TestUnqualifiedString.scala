package transformations.javac

import org.junit.Test
import org.scalatest.FunSuite
import util.TestUtils

import scala.reflect.io.Path


class TestUnqualifiedString extends FunSuite {

  test("basic") {
    val inputDirectory = Path("")
    TestUtils.compareWithJavacAfterRunning("UnqualifiedString", inputDirectory)
  }
}
