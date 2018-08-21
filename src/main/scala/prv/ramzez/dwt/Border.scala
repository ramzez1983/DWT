package prv.ramzez.dwt

trait Border {
  def getWithBorder(i: Int, max: Int): Int
}

trait WrapBorder extends Border {
  def getWithBorder(i: Int, max: Int): Int = {
    val r = Math.floorMod(i, max)
    r
  }
}

trait MirrorBorder extends Border {
  def getWithBorder(i: Int, max: Int): Int = {
    val r = if (i < 0) -i else if (i >= max) max - (i - max) - 2 else i
    r
  }
}

trait RepeatBorder extends Border {
  def getWithBorder(i: Int, max: Int): Int = {
    val r = if (i < 0) 0 else if (i >= max) max - 1 else i
    r
  }
}