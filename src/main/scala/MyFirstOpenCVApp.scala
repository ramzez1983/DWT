import java.nio.file.Paths

import javax.swing.JFrame
import org.bytedeco.javacpp.indexer.FloatIndexer
import org.bytedeco.javacpp.opencv_core.{Mat, Point}
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}

object MyFirstOpenCVApp extends App {

  private def printDims(img: Mat): Unit = {
    println(s"dims: ${img.dims()}, rows: ${img.rows()}, cols: ${img.cols()}, channels: ${img.channels()}")
  }

  val path = Paths.get(getClass.getResource("/lena1.png").toURI)
  val img: Mat = imread(path.toString)
  if (img.empty()) {
    // error handling
    // no image has been created...
    // possibly display an error message
    // and quit the application
    println("Error reading image: " + path)
    System.exit(0)
  }
  printDims(img)
  val greyMat = new Mat()
  opencv_imgproc.cvtColor(img, greyMat, opencv_imgproc.CV_BGR2GRAY, 1)
  printDims(greyMat)
  val img2 = img.reshape(3,1)
  printDims(img2)
  val vec = Mat.ones(4,1,opencv_core.CV_8UC1).asMat()
  printDims(vec)
  val canvas = new CanvasFrame("My Image", 1)

  val source = Mat.zeros(3, 3, opencv_core.CV_32F).asMat()
  val sourceIndexer = source.createIndexer().asInstanceOf[FloatIndexer]
  sourceIndexer.put(0, 0, 4.toFloat)
  sourceIndexer.put(0, 1, 2.toFloat)
  sourceIndexer.put(1, 0, 1.toFloat)
  val dest = new Mat()
  val kernel = Mat.ones(1, 2, opencv_core.CV_32F).asMat()
  val kernelIndexer = kernel.createIndexer().asInstanceOf[FloatIndexer]
  //kernelIndexer.put(0,1,1.toFloat)

  val anchor: Point = new Point(-1, -1)

  opencv_imgproc.filter2D(source, dest, -1, kernel, anchor, 0.0, opencv_core.BORDER_DEFAULT)

  println(s"sourcce: $sourceIndexer")
  println(s"dest: ${dest.createIndexer().asInstanceOf[FloatIndexer]}")
  println(s"kernel: $kernelIndexer")
  //reduce columns
  val rr = 0 to dest.cols() by 2
  val dest_2 = rr.map(r => dest.col(r)).reduce { (a, b) => a.push_back(b); a }.reshape(0, rr.length).t().asMat()
  println(s"dest_2: ${dest_2.createIndexer().asInstanceOf[FloatIndexer]}")
  //reduce rows
  val cc = 0 to dest.rows() by 2
  val dest_3 = cc.map(r => dest.row(r)).reduce { (a, b) => a.push_back(b); a }
  println(s"dest_3: ${dest_3.createIndexer().asInstanceOf[FloatIndexer]}")

  // Request closing of the application when the image window is closed
  canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  // Convert from OpenCV Mat to Java Buffered image for display
  val converter = new OpenCVFrameConverter.ToMat()
  // Show image on window
  canvas.showImage(converter.convert(img))
}
