package Main;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_INTER_LINEAR;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;



import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;


import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static java.nio.file.StandardCopyOption.*;

import database.StudentInfoLocalDB;

public class Launcher extends JFrame {

    private static JPanel contentPane;    
    private static JLabel imgLabel = new JLabel();   
    private static JComponent canvas;
    protected static BufferedImage image;
    private static FrameGrabber grabber;
    protected static int width, height;
    private static CaptureImageThread streaming;
    protected static boolean mirrored = true;
    protected static double scale = 0.5;
    private static boolean isCancelled = false;        
    private static boolean toCapture = false;    
    private static IplImage gray;    
    private static IplImage grabbed;
    private static String imagePath;
    private static Rect rec;
    private static CascadeClassifier cascade = new CascadeClassifier("haarcascade_frontalface_alt.xml");    
    private static JPanel picPanel;
    private static JTextField[] textFields;
    
    
    private static void createAndShowGUI() {
    	//construct a frame
    	JFrame frame = new JFrame();
    	frame.pack();
    	//set close operation on the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set window position and size
        frame.setBounds(500, 300, 800, 500);
        frame.setResizable(false);
        //create main container
        contentPane = new JPanel();
        //set border for main container
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        //set contentPane for frame
        frame.setContentPane(contentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //set layout for main container
        contentPane.setLayout(new GridLayout());
        
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel pic_cam_panel = new JPanel();
        JPanel btnPanel = new JPanel();
        picPanel = new JPanel();
        picPanel.setLayout(new BorderLayout());
        JPanel picBtnPanel = new JPanel();
        picBtnPanel.setBackground(Color.BLACK);
        picBtnPanel.setLayout(new FlowLayout());
        JPanel infoPanel = new JPanel();
        
        
        JButton camOnBtn = new JButton("Turn on cam");
        JButton captureFaceBtn = new JButton("Capture");
        JButton recognizeBtn = new JButton("Recognize");
        JButton closeCamBtn = new JButton("Close cam");
        JButton picRecognizeBtn = new JButton("Recognize");
        JButton picBrowseBtn = new JButton("Browse");
        
        pic_cam_panel.setLayout(new BorderLayout());
        pic_cam_panel.setBounds(20, 20, 50, 50);
        pic_cam_panel.setBorder(new LineBorder((Color.CYAN),2,true)); 
        
        btnPanel.setLayout(new BoxLayout(btnPanel,BoxLayout.X_AXIS));
  
        btnPanel.add(camOnBtn);
        btnPanel.add(captureFaceBtn);
        btnPanel.add(recognizeBtn);
        btnPanel.add(closeCamBtn);
        
        picBtnPanel.add(picBrowseBtn);
        picBtnPanel.add(picRecognizeBtn);
        
        picPanel.setBorder(new LineBorder(Color.red, 4, true));
        picPanel.setPreferredSize(new Dimension(300,250));
        JPanel picPanel_Upper = new JPanel(new GridBagLayout());
        picPanel_Upper.setBackground(Color.yellow);
        picPanel_Upper.add(imgLabel);
        
        picPanel.add(picPanel_Upper, BorderLayout.CENTER);
        picPanel.add(picBtnPanel, BorderLayout.AFTER_LAST_LINE);
        
        infoPanel.setBorder(new LineBorder(Color.green, 2, true));    
        String[] labels = {"Name: ", "Email: ", "Phone: ", "Last Seen: ", "Total num of seen: "};
        int numOfLabels = labels.length;
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        textFields = new JTextField[numOfLabels];
        for (int i = 0; i < numOfLabels; i++) {
            JLabel lb = new JLabel(labels[i], JLabel.TRAILING);
            infoPanel.add(lb);
            textFields[i] = new JTextField();
            textFields[i].setBackground(Color.gray);
            textFields[i].setEditable(false);
            textFields[i].setForeground(Color.white);
            textFields[i].setFont(new Font("Courier", Font.BOLD,14));
            lb.setLabelFor(textFields[i]);
            infoPanel.add(textFields[i]);
        }
        
        
        
        leftPanel.setBorder(new LineBorder(new java.awt.Color(21, 56, 70), 1, true));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(pic_cam_panel, BorderLayout.CENTER);
        leftPanel.add(btnPanel, BorderLayout.PAGE_END);
        
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(new LineBorder(new java.awt.Color(21, 56, 70), 1, true));
        rightPanel.add(infoPanel);
        rightPanel.add(picPanel);
        rightPanel.validate();
        
		canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(image, 0, 0, null);
			}
		};
		
				
		pic_cam_panel.add(canvas, BorderLayout.CENTER);
        contentPane.add(leftPanel);
        contentPane.add(rightPanel);
        
        imgLabel.setVisible(true);
        leftPanel.setVisible(true);
        rightPanel.setVisible(true);

        
        picBrowseBtn.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

        		JFileChooser fc = new JFileChooser();
                int res = fc.showOpenDialog(null);
                try {
                    if (res == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        imagePath = file.getAbsolutePath();
                        System.out.println("Log: read image from:" + imagePath);
                        imgLabel.setIcon(new ImageIcon(imagePath));
                    } 
            
                } catch (Exception iOException) {
                }
            }
        });
        
        picRecognizeBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				recoFaceFromPic(imagePath);
			}
        	
        });
        
        camOnBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isCancelled = false;
				
        		grabber = new OpenCVFrameGrabber(0); 
        		
        		int attempt = 0;
        		
        		while (attempt < 60) {
        			attempt++;
        			try {
        				grabber.start();
        				break;
        			} 
        			catch (Exception e1) {
        				
        			}
        		}
        		if (attempt == 60) {
        			System.err.println("Log: Starting Camera Failed");
        			return;
        		}
        		
        		System.out.println("Log: Camera Ready");
        		
        		
        		
        		width = grabber.getImageWidth();
        		height = grabber.getImageHeight();
            	
        		if (scale != 1) {
        			width = (int)(width);
        			height = (int)(height);
        		}
        		
            	streaming = new CaptureImageThread();
        		streaming.execute();
        		
			}
        	
        });
        
        closeCamBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					isCancelled = true;
					if(grabber != null) {
						grabber.stop();
						grabber.release();
						grabber = null;
					}
					image = null;
					canvas.repaint(0,0,400,450);
	        		
					
				} catch (org.bytedeco.javacv.FrameGrabber.Exception e1) {
					e1.printStackTrace();
				}
							
			}
        	
        });
        
        captureFaceBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				toCapture = true;
			}
    		
    	});
        
        recognizeBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

		    	if(grabbed != null && rec.width()>0) {
		    	captureFace();
		    	Map<Integer, String> hm = new HashMap<Integer, String>();
		    	File root = new File("trainingImags");
		    	 FilenameFilter imgFilter = new FilenameFilter() {
		             public boolean accept(File dir, String name) {
		                 name = name.toLowerCase();
		                 return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
		             }
		         };
		         
		    	File[] imageFiles = root.listFiles(imgFilter);
		    	MatVector images = new MatVector(imageFiles.length);
		    	Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
		        IntBuffer labelsBuf = labels.createBuffer();

		        int counter = 0;

		        for (File image : imageFiles) {
		            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
		            
		            String[] temp = image.getName().split("\\-");
		            int label = Integer.parseInt(temp[0]);
		            hm.put(label, temp[1]);

		            images.put(counter, img);

		            labelsBuf.put(counter, label);

		            counter++;
		        }

		        //FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
		        //FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
		         FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

		        faceRecognizer.train(images, labels);
		        Mat m = new Mat(gray);
		        int predictedLabel = faceRecognizer.predict(m);
		        String predictedName = hm.get(predictedLabel);
		        int nameLength = predictedName.length();
		        predictedName = predictedName.substring(0, nameLength - 4);
		        try {
					String[] retrieveData = StudentInfoLocalDB.getInfoByName(predictedName);
					for(int i = 0; i < retrieveData.length; i++) {
						textFields[i].setText(retrieveData[i]);
					}
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		    	hm.clear();
		    	} else {
		    		System.out.println("Log: no face on screen");
		    	}
		    	
		    }
        	
        });
        
        frame.addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
               int result = JOptionPane.showConfirmDialog(frame,"Close window and exit?", "Closing Application",JOptionPane.YES_NO_OPTION);
               if(result ==JOptionPane.YES_OPTION) {
            	   isCancelled = true;
					if(grabber != null) {
						try {
							grabber.stop();
							grabber.release();
							grabber = null;
							image = null;
							canvas.repaint(0,0,400,450);
						} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
            	   System.exit(0);
               }                           	
            }
        });
        
        frame.setVisible(true);
    }
    
    private static class CaptureImageThread extends SwingWorker<Void, Void> {
    	
		protected Void doInBackground() throws Exception {
		    
		 	OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
		    Java2DFrameConverter paintConverter = new Java2DFrameConverter();
		    while (!isCancelled) {
				grabbed = null;
				while (grabbed == null) {
					try {
						grabbed = grabberConverter.convert(grabber.grab());
					}	
					catch (Exception e) {
						System.err.println("Log: Render Failed");
						Thread.sleep(100); 
					}
				}
				if (mirrored) {
					cvFlip(grabbed, grabbed, 1);
				}
				if (scale != 1) {
					IplImage resized = IplImage.create(width, height, grabbed.depth(), grabbed.nChannels());
					cvResize(grabbed, resized);
					grabbed = resized;
					if(toCapture) {
						saveFace();
						toCapture = false;
					}

				}

				//draw rectangle
				IplImage grayImg = cvCreateImage(cvGetSize(grabbed), IPL_DEPTH_8U, 1);
				cvCvtColor(grabbed, grayImg, CV_BGR2GRAY);  
				IplImage smallImg = IplImage.create(grayImg.width()/2,grayImg.height()/2, IPL_DEPTH_8U, 1);
				Mat img = new Mat(smallImg);
				cvResize(grayImg, smallImg, CV_INTER_LINEAR);
				cvEqualizeHist(smallImg, smallImg);
				CvMemStorage storage = CvMemStorage.create();
				RectVector faces = new RectVector();
				cascade.detectMultiScale(img, faces, 1.3, 3, CV_HAAR_FIND_BIGGEST_OBJECT, null, null);
				cvClearMemStorage(storage);

				for (int i = 0; i <faces.size(); i++) {

					rec = faces.get(i);
					cvRectangle(
							grabbed, 
							cvPoint( rec.x()*2, rec.y()*2 ),
							cvPoint( (rec.x() + rec.width())*2, (rec.y() + rec.height())*2 ), 
							CvScalar.GREEN,
							6, 
							CV_AA, 
							0
							);
				}
				
				Frame frame = grabberConverter.convert(grabbed);
				image = paintConverter.getBufferedImage(frame);
				canvas.repaint();
				Thread.sleep(100); 
			}

			return null;
		}
	}
    
    public static void captureFace() {
    	IplImage img = grabbed;
    	if(rec.width() > 0) {
    		CvRect r = new CvRect();
    		r.x(rec.x()*2);
    		r.y(rec.y()*2);
    		r.width(rec.width()*2);
    		r.height(rec.height()*2);
    		
            cvSetImageROI(img, r);
            IplImage cropped = cvCreateImage(cvGetSize(img), img.depth(), img.nChannels());
            cvCopy(img, cropped);
            Mat inputImg = new Mat(cropped);
            Mat resultImg = new Mat();
            Size ruler = new Size(120,120); 
            org.bytedeco.javacpp.opencv_imgproc.resize(inputImg, resultImg, ruler);

            IplImage beforeGray = new IplImage(resultImg);
            gray = cvCreateImage(cvGetSize(beforeGray), IPL_DEPTH_8U, 1);
            cvCvtColor(beforeGray, gray, CV_BGR2GRAY); 
    	}
    }
    
    public static void saveFace() throws IOException {
    	captureFace();
    	String name = JOptionPane.showInputDialog("name");
    	if(name != null) {
    		int reply = JOptionPane.showConfirmDialog(null, "Save to training images folder?", "add new face", JOptionPane.YES_NO_OPTION);
    		
    		if(reply == JOptionPane.YES_OPTION) {
    			cvSaveImage( name + ".jpg", gray);
    			Path source = FileSystems.getDefault().getPath(name+".jpg");
        		Path dest = FileSystems.getDefault().getPath("trainingImags/" + name+".jpg");
        		Files.move(source, dest, REPLACE_EXISTING);
    		} else {
    			cvSaveImage( name + ".jpg", gray);
    		}
    		
    		System.out.println("Log: image saved");
    	}  
    }
    
    
    private static void recoFaceFromPic(String name) {
    	Map<Integer, String> hm = new HashMap<Integer, String>();
    	if(name == null) {
    		System.err.println("Error: select image first");
    	} else {
    		
    	
    	
    	name = name.replace('\\', '/');
    	Mat temp = imread(name);
    	IplImage input = new IplImage(temp);
    	IplImage grayPic = cvCreateImage(cvGetSize(input), IPL_DEPTH_8U, 1);
    	cvCvtColor(input, grayPic, CV_BGR2GRAY);
    	IplImage smallImg = IplImage.create(grayPic.width()/2,grayPic.height()/2, IPL_DEPTH_8U, 1);
		Mat img = new Mat(smallImg);
		cvResize(grayPic, smallImg, CV_INTER_LINEAR);
		cvEqualizeHist(smallImg, smallImg);
		RectVector faces = new RectVector();
		cascade.detectMultiScale(img, faces, 1.3, 3, CV_HAAR_FIND_BIGGEST_OBJECT, null, null);
		
		Rect picRect = faces.get(0);
		
		CvRect rectangle = new CvRect();
		rectangle.x(picRect.x()*2);
		rectangle.y(picRect.y()*2);
		rectangle.width(picRect.width()*2);
		rectangle.height(picRect.height()*2);
		cvSetImageROI(input, rectangle);
		IplImage cropped = cvCreateImage(cvGetSize(input), input.depth(), input.nChannels());
		cvCopy(input, cropped);
		
		 Mat inputImg = new Mat(cropped);
         Mat resultImg = new Mat();
         Size ruler = new Size(120,120); 
         org.bytedeco.javacpp.opencv_imgproc.resize(inputImg, resultImg, ruler);

         IplImage beforeGray = new IplImage(resultImg);
         gray = cvCreateImage(cvGetSize(beforeGray), IPL_DEPTH_8U, 1);
         cvCvtColor(beforeGray, gray, CV_BGR2GRAY);
         
         Mat testImage = new Mat(gray);
         
         File root = new File("trainingImags");
         
         //construct filter
         FilenameFilter imgFilter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 name = name.toLowerCase();
                 return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
             }
         };
         
         //create image array after filtering
         File[] imageFiles = root.listFiles(imgFilter);
         
         //create a vector to store the training images
         MatVector images = new MatVector(imageFiles.length);
         
         //create Mat to store 
         Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
         IntBuffer labelsBuf = labels.createBuffer();

         int counter = 0;

         for (File image : imageFiles) {
             Mat i = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
             String[] temps = image.getName().split("\\-");
             int label = Integer.parseInt(temps[0]);
             hm.put(label, temps[1]);

             images.put(counter, i);

             labelsBuf.put(counter, label);

             counter++;
         }

        // FaceRecognizer faceRecognizer = createFisherFaceRecognizer();
         // FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
          FaceRecognizer faceRecognizer = createLBPHFaceRecognizer();

         faceRecognizer.train(images, labels);
         int predictedLabel = faceRecognizer.predict(testImage);
	       
         String predictedName = hm.get(predictedLabel);
	        int nameLength = predictedName.length();
	        predictedName = predictedName.substring(0, nameLength - 4);
	        try {
				String[] retrieveData = StudentInfoLocalDB.getInfoByName(predictedName);
				for(int i = 0; i < retrieveData.length; i++) {
					textFields[i].setText(retrieveData[i]);
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}    	}
    }
    

    public static void main(String[] args) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
