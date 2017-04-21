package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller {
	
	static Socket socket;
	static DataOutputStream toServer = null;
	static DataInputStream fromServer = null;
	
	static float[] tab = new float[600];
	static int[] tabby = new int[50];
	static float[] przybliz = new float[400];
	static int probki = 0;
	static String ip;
	static int port;
	static int zadanyCzas = 0;
	static long tStart = 0;
	static boolean conncted;
	static boolean jest = false;
	static int zadanyPWM = 0;
	static int zadanyPWM1 = 0;
	
	static int Taau=0;
	int tauwys=0;
	int tauwys2=0;
	float sumaMNKwys=0;
	int dlugosc=0;
	int dlugosc2=0;
	int mnkwys=0;
	int mnkwys2=0;
	
	final double nakmh = 3.6;
	final double dwapi = 6.28;
	final double promien = 0.04;
	final double naczas = 200*(nakmh*dwapi*promien);
	
	@FXML private TextField login;
	@FXML private TextField port_tf;
	@FXML private Button start;
	@FXML private Button powrot;
	@FXML private TextField zadajPWM;
	@FXML private TextField zadajCzas;
	@FXML private ProgressBar progressbar;
	@FXML private Button wykres;
	@FXML private Button odbierz;
	@FXML private Button odbierztau;
	@FXML private Button wczytaj;
	@FXML private Button przybl;
	@FXML private NumberAxis xAxis;
	@FXML private NumberAxis yAxis;
	@FXML private LineChart<Integer,Float> wykress;
	@FXML private LineChart<Integer,Float> wykress1;
	@FXML private Label status1;

	
	@FXML
	private void czekStatus() {
		if (jest==true) {
			if (conncted==true) {
				status1.setText("po³¹czono");
			}
			else {
				status1.setText("nie po³¹czono");
			}
		}
		else {
			status1.setText("nie po³¹czono");
		}
	}
	
	@FXML 
	protected void polaczClicked(ActionEvent event){
		start.setVisible(true);
	}
	
	@FXML
	 private void handleButtonAction(ActionEvent event) throws IOException{
	     Stage stage; 
	     Parent root;
	     if(event.getSource()==start){
	        //get reference to the button's stage         
	        stage=(Stage) start.getScene().getWindow();
	        //load up OTHER FXML document
	        root = FXMLLoader.load(getClass().getResource("IdentificationPage.fxml"));
	        polacz();
	      }
	     else{
	    	 stage=(Stage) powrot.getScene().getWindow();
	    	 root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
	     	}
	   
	     Scene scene = new Scene(root);
         root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	     stage.setScene(scene);
	     stage.show();
	    }
	
	@FXML
	private void wykrespop() throws IOException {
		Stage stage;
		Parent root;
		stage = new Stage();
		root = FXMLLoader.load(getClass().getResource("Wykres.fxml"));
		Scene scene = new Scene(root);
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	    stage.setScene(scene);
	    stage.initModality(Modality.APPLICATION_MODAL);
	    stage.initOwner(wykres.getScene().getWindow());
		stage.setTitle("SiMR");
	    stage.showAndWait();
		
	}
	
	@FXML
	private void polacz(){ 
			try {
			ip = login.getText().trim();
			port = Integer.parseInt(port_tf.getText().trim());

			socket = new Socket(ip,port);
			toServer = new DataOutputStream(socket.getOutputStream());
			fromServer = new DataInputStream(socket.getInputStream());
			jest = true;
			conncted = socket.isConnected() && ! socket.isClosed();
			
		}
		catch (Exception ex){	
			//if(ip.isEmpty()){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Uwaga");
				alert.setHeaderText(null);
				alert.setContentText("Podaj adres!");

				alert.showAndWait();
			//}
			System.err.println(ex);
			
		}
	}
	
	@FXML
	private void wyslij_PWM(){
		try {
				
				zadanyPWM = Integer.parseInt(zadajPWM.getText().trim());
				toServer.writeBytes("cp1 " + zadanyPWM + "\n");
				toServer.flush();
		        tStart = System.currentTimeMillis();
		        long ttStart = tStart + zadanyCzas;
		        new Thread(() -> { // lambda expression
		        	try {		
		        		
		        		while (ttStart > tStart) {
		        			
		        			double postep = 1-((ttStart - tStart)/(double)(zadanyCzas));
		        			tStart = System.currentTimeMillis();

		        			Platform.runLater(() -> progressbar.setProgress(postep));
		        			Thread.sleep(10);
		        		}
		        	}
		        	catch (Exception ex) {
		    			System.err.println(ex);
		        	}
		        }).start();
			}
		
		catch (Exception ex) {
			System.err.println(ex);
		}
	}
	@FXML
	private void wyslij_PWMsz(){
		try {

			toServer.writeBytes("sz " + zadanyPWM1 + "\n");
		}
		
		catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
	@FXML
	private void wyslij_czas(){
		try {
				zadanyCzas = Integer.parseInt(zadajCzas.getText().trim());
				toServer.writeBytes("cz0 " + zadajCzas.getText().trim()+ "\n");
				System.out.println("cz0 " + zadajCzas.getText().trim());
				//toServer.writeUTF(zadajCzas.getText(1, 10));
				toServer.flush();
			}
		
		catch (IOException ex) {
			System.err.println(ex);
		}
	}
	@FXML
	private void odbierz(){
		probki = 0;
		try {
			toServer.writeBytes("od2 " + 200 + "\n");
			
			while (fromServer.available() > 0){
			 int a = fromServer.readByte()*100;
			 //System.out.print("a");System.out.println(a);
			// if (a<0){
			//	 a=256+a;
			// }
			 TimeUnit.MILLISECONDS.sleep(20);
			 int c = fromServer.readByte();
			 
			 float b = (float)a+(float)c;
			 
			 tab[probki]=b/1000;

			 System.out.print("wart");System.out.println(tab[probki]);
			 probki++;
			 TimeUnit.MILLISECONDS.sleep(20);
			 }
			}
		
		catch (Exception ex) {
			System.err.println(ex);
		}
	}
	@FXML
	private void odbieramtau(){
		try {
			toServer.writeBytes("ta " + 200 + "\n");
			int licz=0;
			while (fromServer.available() > 0){
				
				tabby[licz]=fromServer.readByte();
				 if (tabby[licz]<0){
					 tabby[licz]=256+tabby[licz];
				 }
				System.out.println(tabby[licz]);
				licz++;
				 TimeUnit.MILLISECONDS.sleep(20);
			}
			 
			 Taau = tabby[0]*tabby[1]+tabby[2];
			 sumaMNKwys = (float)(tabby[3]*tabby[4]+tabby[5])/10000;
			 if (tabby[3]==110){
				 sumaMNKwys=(float)(tabby[4]*1000+tabby[5])/10000;
			 }
			 float vmax=(float)tabby[6]*100+(float)tabby[7];
			 System.out.print("tau ");System.out.println(Taau);
			 System.out.print("sumaMNK ");System.out.println(sumaMNKwys);
			 System.out.print("vmax ");System.out.println(vmax/1000);
			
			 TimeUnit.MILLISECONDS.sleep(50);
			 Alert alert2 = new Alert(AlertType.INFORMATION);
				alert2.setTitle("Dane");
				alert2.setHeaderText(null);
				alert2.setContentText("Tau: "+Taau+" MNK: "+sumaMNKwys+" Vmax: "+vmax/1000);

				alert2.showAndWait();
			}
		
		catch (Exception ex) {
			System.err.println(ex);
		}
	}
	
	@FXML
	public void wczytajdane(){
		try {
			/*java.io.File file = new java.io.File("probki.txt");
			if (file.exists()) {
				System.out.println("Istnieje");
			}*/
			//java.io.PrintWriter output = new java.io.PrintWriter(file);
			
			int czas = 0;
			wykress.getData().clear();
			XYChart.Series<Integer, Float> series = new XYChart.Series<Integer, Float>();
			System.out.print("próbki");System.out.println(probki);
	
			for (int i = 0; i<probki; i++){
				if (i==0) {
					czas = 0;
					}
				else {
					czas = czas + (int)(naczas/tab[i]);
					}
			/*	output.print(tab[i]);
				output.print("\t");
				output.println(czas);	*/
				series.getData().add(new XYChart.Data<Integer, Float>(czas, tab[i]));
				}
			series.setName("Wykres");
			wykress.getData().add(series);
			//output.close();
		}
		catch (Exception x) {
			System.err.println(x);
		}
	}
	
	@FXML
	public void rysujprzyblizony(){
	//	try {
			
			int czas = 0;
			XYChart.Series<Integer, Float> series1 = new XYChart.Series<Integer, Float>();
		
			for (int i = 0; i<probki; i++){
				if (i==0) {
					czas = 0;
					}
				else {
					czas = czas + (int)(naczas/tab[i]);
					}
				float ad = (float)czas/(float)Taau;
				przybliz[i] = (float)tab[probki-1]*(1-(float)(Math.pow(2.718, -ad)));
				series1.getData().add(new XYChart.Data<Integer, Float>(czas, przybliz[i]));


				}
			
			series1.setName("Przybli¿enie");
			wykress.getData().add(series1);
			//output.close();
	//	}
		//catch (Exception x) {
			//System.err.println(x);
		//}
	}
	
	@FXML
	public void zapiszdane(){
		try {
			java.io.File file = new java.io.File("pwm" + zadanyPWM + "czas" + zadanyCzas + ".txt");
			if (file.exists()) {
				System.out.println("Istnieje");
			}
			java.io.PrintWriter output = new java.io.PrintWriter(file);
			int czas = 0;	
			for (int i = 0; i<probki; i++){
				if (i==0) {
					czas = 0;
					}
				else {
					czas = czas + (int)(naczas/tab[i]);
					}
				output.print(tab[i]);
				output.print("\t");
				output.println(czas);	
				}

			output.close();
		}
		catch (IOException x) {
			System.err.println(x);
		}
	}
	

}
