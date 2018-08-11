import java.nio.file.Paths

import javax.swing.JFrame
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacpp.{opencv_core, opencv_imgproc}
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}

object MyFirstOpenCVApp extends App {

  private def printDims(img: Mat) = {
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

  // Request closing of the application when the image window is closed
  canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  // Convert from OpenCV Mat to Java Buffered image for display
  val converter = new OpenCVFrameConverter.ToMat()
  // Show image on window
  canvas.showImage(converter.convert(img))
}
