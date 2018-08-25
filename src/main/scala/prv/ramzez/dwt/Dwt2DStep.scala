package prv.ramzez.dwt

import org.bytedeco.javacpp.opencv_core.Mat

case class Dwt2DStep(hh: Mat, hl: Mat, lh: Mat, ll: Mat) {

  def toList(): List[Mat] = {
    List(hh, hl, lh, ll)
  }

  def split(): List[Dwt2DStep] = {
    toList().map(s => matSplit(s)).transpose.map { case List(_hh, _hl, _lh, _ll) => Dwt2DStep(_hh, _hl, _lh, _ll) }
  }
}

object Dwt2DStep {

  def mergeChannels(steps: List[Dwt2DStep]): Dwt2DStep = {
    val l = steps.map(s => s.toList()).transpose.map(channels => matMerge(channels))
    l match {
      case List(_hh, _hl, _lh, _ll) => Dwt2DStep(_hh, _hl, _lh, _ll)
    }
  }
}