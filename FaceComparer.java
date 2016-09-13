import org.opencv.imgproc.Imgproc;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.BasicFaceRecognizer;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;

/**
 * 
 * This class takes in a single image file as an input and compares it to a database(also an input) of images using Fisher 
 * Face Recognition in OpenCV. After determining whether there's a match or not, it then
 * outputs coordinates of where the face lies within the image.
 *
 *All images must have filename format as <label>-rest_of_filename.png
 *i.e. 1-image_1.png
 */
public class FaceComparer {
	
		public static void main(String[] args)
		{
			//This string is the file path to the database
			String imageDatabase = args[0];
			
			//Converts the image that is being compared to the database into grayscale.
			Mat singleImage = Highgui.imread(args[1], Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			
			//Not entirely sure the use of erosion yet
			int erosion_size = 5;
			Mat element  = Imgproc.getStructuringElement(
			    Imgproc.MORPH_CROSS, new Size(2 * erosion_size + 1, 2 * erosion_size + 1), 
			    new Point(erosion_size, erosion_size)
			);
			Imgproc.erode(singleImage, singleImage, element);
			
			File root = new File(imageDatabase);

			//Creates a FilenameFilter that makes sure the image in the database
			//has the correct file format.
		    FilenameFilter imageFilter = new FilenameFilter() {
		    	public boolean accept(File dir, String name) {
		                name = name.toLowerCase();
		                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
		            }
		     };
		     
		     File[] fileArray = root.listFiles(imageFilter);
		     
		     MatVector images = new MatVector(fileArray.length);
		     
		     Mat labels = new Mat(fileArray.length, 1, CV_32SC1);
		     
		     IntBuffer labelsBuf = labels.createBuffer();

		     int counter = 0;

		     for (File image : fileArray) {
		          
		    	 Mat img = Highgui.imread(image.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_GRAYSCALE);

		         int label = Integer.parseInt(image.getName().split("\\-")[0]);

		         images.put(counter, img);

		         labelsBuf.put(counter, label);

		         counter++;
		     }

		        BasicFaceRecognizer faceRecognizer = createFisherFaceRecognizer();

		        faceRecognizer.train(images, labels);

		        int predictedLabel = faceRecognizer.predict(singleImage);

		        System.out.println("Predicted label: " + predictedLabel);
		}

}

