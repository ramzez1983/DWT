package prv.ramzez.dwt

import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core.{Mat, Point}
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}

object Transformer {
}

class Transformer(val lowFilter: List[Float], val highFilter: List[Float]) {

  def reduceColumns(dest: Mat): Mat = {
    val columns = 0 to dest.cols() by 2
    columns.map(r => dest.col(r)).reduce { (a, b) => a.push_back(b); a }.reshape(0, columns.length).t().asMat()
  }

  def reduceRows(dest: Mat): Mat = {
    val rows = 0 to dest.rows() by 2
    rows.map(r => dest.row(r)).reduce { (a, b) => a.push_back(b); a }
  }

  //TODO: extract this to DWT Filter trait
  def createKernel(filter: List[Float]): Mat = {
    val k = Mat.zeros(1, filter.length, opencv_core.CV_32F).asMat()
    val indexer = k.createIndexer().asInstanceOf[FloatIndexer]
    for ((i, v) <- 0L to filter.length zip filter) {
      indexer.put(0L, i, v)
    }
    k
  }

  lazy val rowHighFilter: Mat = createKernel(highFilter)
  lazy val rowLowFilter: Mat = createKernel(lowFilter)

  def toColumnKernel(filter: Mat): Mat = filter.reshape(0, filter.cols())

  private val anchor = new Point(-1, -1)
  private val delta = 0.0
  private val borderType = opencv_core.BORDER_DEFAULT

  def decomposeStep(img: Mat): DwtStep = {
    val low, high, ll, lh, hl, hh = new Mat()

    opencv_imgproc.filter2D(img, low, -1, rowLowFilter, anchor, delta, borderType)
    val low_2 = reduceColumns(low)
    opencv_imgproc.filter2D(img, high, -1, rowHighFilter, anchor, delta, borderType)
    val high_2 = reduceColumns(high)

    opencv_imgproc.filter2D(low_2, ll, -1, toColumnKernel(rowLowFilter), anchor, delta, borderType)
    val ll_2 = reduceRows(ll)
    opencv_imgproc.filter2D(low_2, lh, -1, toColumnKernel(rowHighFilter), anchor, delta, borderType)
    val lh_2 = reduceRows(lh)

    opencv_imgproc.filter2D(high_2, hl, -1, toColumnKernel(rowLowFilter), anchor, delta, borderType)
    val hl_2 = reduceRows(hl)
    opencv_imgproc.filter2D(high_2, hh, -1, toColumnKernel(rowHighFilter), anchor, delta, borderType)
    val hh_2 = reduceRows(hh)

    DwtStep(hh_2, hl_2, lh_2, ll_2)
  }

  def decompose(img: Mat, level: Int): DwtStructure = {
    decompose(img, level, List())
  }

  def decompose(img: Mat, level: Int, structure: DwtStructure): DwtStructure = {
    if (level <= 0) structure
    else {
      val step = decomposeStep(img)
      decompose(step.LL, level - 1, structure :+ step)
    }
  }
}