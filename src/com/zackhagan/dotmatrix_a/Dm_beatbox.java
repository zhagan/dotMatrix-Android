package com.zackhagan.dotmatrix_a;

import java.io.File;
import java.io.IOException;


import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdService;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.PdListener;
import org.puredata.core.utils.IoUtils;


import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import controlP5.*;
import select.files.*;
import apwidgets.*;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Dm_beatbox extends PApplet {
	
	public int sketchWidth() {
		return displayWidth;
	}

	public int sketchHeight() {
		return displayHeight;
	}

	public String sketchRenderer() {
		return OPENGL;
	}
	
	Context context;
	
	static ControlP5 cp5;
	
	private PdService pdService = null;
	private PdUiDispatcher dispatcher;
	float pd = 0;
	String TAG="beatbox";
	
	// load pattern stuff
	 String song = "dotMatrix.txt";
	 String[] data;
	 String songName;
	 String patternName;
	
	 static SelectLibrary fileselector;
	
	 APWidgetContainer widgetContainer; 
	 APEditText textField;

	
			
	// colors 
	// for active linegraph circle
	int cirColor0 = color(0, 255, 0); //green
	int seqColor = color(0, 255, 0); //green
	// for matrix stroke
	int stroke0 = color(255);
	int tempi = 0;
	boolean soloOn = false;
	int tempi1 = 0;
	boolean instrSelOn = false;

	int widthScr;
	int boxHeight;

	// variables for drum box lights
	boolean isPlaying = false;
	boolean dialogOpen;
	float gBeat = 0;
	int durTime = 100;
	int numofi = 8;
	float sbar = 0;
	static float cbar;
	static float copyLength=1;
	float patNum;
	//int nextBar = PApplet.parseInt(sbar);

	int timer0;
	int timer1;
	int timer2;
	int timer3;
	int timer4;
	int timer5;
	int timer6;
	int timer7;

	
	
	
	int time = millis();

	int myColor = color(255,50);

	// instrument select and Env select

	int instrSel = 10;
	int envSel = 1;
	int songLength = 64;
	int seqLength = 4;
	static boolean seqOn = false;

	int transp = 255;
	int dColor0 = color(255,0,0); // Red
	int dColor1 = color(0,transp);//black
	int dColor2 = color(255,158,0,transp);//orange
	int dColor3 = color(255,255,0,transp);//
	int dColor4 = color(0,255,0,transp); 
	int dColor5 = color(0,255,255,transp);
	int dColor6 = color(0,0,255,transp);
	int dColor7 = color(255,0,255,transp);
	int dColor8 = color(255,158,255,transp);

	int tBoxSize; //=1920/21;// displayHeight/21;//31;
	int mposX;// = tBoxSize;
	int mposY;//  = tBoxSize;
	
	// init classess

    static Matrix m1;// 

		
	Linegraph VelGraph;
	Linegraph PitGraph;
	Linegraph StaGraph;
	Linegraph LenGraph;
	Linegraph VerbGraph;
	Linegraph DelGraph;
	Linegraph PanGraph;
	
	

	Oscview ov1;// = new Oscview(oscX,osxY+tBoxSize*8,oscH,dColor0,"viewR");
	Oscview ov2;// = new Oscview(oscX,osxY+tBoxSize*9,oscH,255,"viewL"); // position x, pos y, plot height, line color, array name
	Oscview ovKick;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor1,"0view");
	Oscview ovSnr;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor2,"1view");
	Oscview ovHht;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor3,"2view");
	Oscview ovOhht;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor4,"3view");
	Oscview ovShkr;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor5,"4view");
	Oscview ovCb;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor6,"5view");
	Oscview ovClap;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor7,"6view");
	Oscview ovRide;// = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor8,"7view");
	// button and slider arrays

	Toggle[] solo;
	Toggle[] mute;
	Bang[] clear;
	Slider[] drumvol;
	Bang[] open;
	Toggle[] instrSelect;


	// songseq arrays

	Numberbox sgL;
	Bang rWav;
	Numberbox bar;
	Numberbox copybar;
	//Numberbox[] measure;
	Sngbtn[] sngbtn;


	//patterns

	static Pattern[] patterns;

	
	// radio button envelope select

	RadioButton rES;
	
	//images for logos
	PImage logo;
	PImage copyr;
	
	/**
	 * setting up libPd as a background service the initPdService() method binds
	 * the service to the background thread. call initPdService in onCreate() to
	 * start the service.
	 */

	protected final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder) service).getService();

			try {
				initPd();
				loadPatch();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
				finish();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// Never called

		}
	};

	/* Bind pd service */

	private void initPdService() {

		new Thread() {
			@Override
			public void run() {
				bindService(new Intent(Dm_beatbox.this, PdService.class),
						pdConnection, BIND_AUTO_CREATE);
			}
		}.start();
	}

	/* initialise pd, also setup listeners here */
	protected void initPd() throws IOException {
		
		//initialize graphs before loading patch PD so processing doesnt crash
		  tBoxSize = widthScr/21;// displayHeight/21;//31;
	      mposX = tBoxSize;
	      mposY  = tBoxSize;
			
		  
		  m1 = new Matrix(16, 8, mposX, mposY, tBoxSize);
		  
		  VelGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 0, 100, "Vel");// pos X, pos Y,  width, height, value min, value Max, array name
		  PitGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 1, 200, "Pit");
		  StaGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 0, 50, "Sta");
		  LenGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 1, 100, "Len");
		  VerbGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 0, 100, "Verb");
		  DelGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 0, 100, "Del");
		  PanGraph = new Linegraph(mposX, mposY, tBoxSize*16, tBoxSize*6, 0, 100, "Pan");
		  
		//oscview 
			int oscX= mposX+tBoxSize*9;//mposX+tBoxSize*16; 
			int osxY= round(mposY-tBoxSize*1.5f);//tBoxSize*;
			int oscH= tBoxSize*4;
			
	      ov1 = new Oscview(oscX,osxY+tBoxSize*8,oscH,dColor0,"viewR");
	      ov2 = new Oscview(oscX,osxY+tBoxSize*9,oscH,255,"viewL"); // position x, pos y, plot height, line color, array name
	  	  ovKick = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor1,"0view");
	  	  ovSnr = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor2,"1view");
	  	  ovHht = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor3,"2view");
	  	  ovOhht = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor4,"3view");
	  	  ovShkr = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor5,"4view");
	  	  ovCb = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor6,"5view");
	  	  ovClap = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor7,"6view");
	  	  ovRide = new Oscview(oscX,osxY+tBoxSize*8+tBoxSize/2,oscH,dColor8,"7view");
	  	  
		  VelGraph.init();
		  PitGraph.init();
		  StaGraph.init();
		  LenGraph.init();
		  VerbGraph.init();
		  DelGraph.init();
		  PanGraph.init();
		  m1.init();
		 
		// Configure the audio glue
		int sampleRate = AudioParameters.suggestSampleRate();


		pdService.initAudio(sampleRate, 0, 2, 10.0f);
		
		pdService.startAudio(new Intent(this, Dm_beatbox.class),
				R.drawable.ic_launcher, "dotMatrix rhythm creator", "life is to create");

		dispatcher = new PdUiDispatcher();
		PdBase.setReceiver(dispatcher);

		// listen for Floats
		
				dispatcher.addListener("pd_beat", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
						gBeat = x;
						//println("beat "+x);
						
					}
					
				});
				dispatcher.addListener("pd_sGroove", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
					//	cp5.getController("shuffle").setValue(x);
					    //println("receiving pd_sGroove " +x);
						
					}
					
				});
				 
				dispatcher.addListener("pd_sBpm", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
						cp5.getController("bpm").setValue(x);
						
					}
					
				});	  
					 
				dispatcher.addListener("pd_bar", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
						   sbar = PApplet.parseInt(x);
						   bar.setValue(sbar);
						
					         
						  // println("bar "+sbar);
						 //  println(patterns[PApplet.parseInt(sbar)].drmName);
						   
						 
						   
						   patterns[PApplet.parseInt(sbar)].loadPattern();
						   if(instrSel<10){
							     PdBase.readArray(VelGraph.values,0,"vel",(instrSel-1)*16,16);
						         PdBase.readArray(PitGraph.values,0,"pit",(instrSel-1)*16,16); 
						         PdBase.readArray(StaGraph.values,0,"sstart",(instrSel-1)*16,16);
						         PdBase.readArray(LenGraph.values,0,"length",(instrSel-1)*16,16);
						         PdBase.readArray(VerbGraph.values,0,"verb",(instrSel-1)*16,16); 
						         PdBase.readArray(DelGraph.values,0,"delay",(instrSel-1)*16,16);
						         PdBase.readArray(PanGraph.values,0,"pan",(instrSel-1)*16,16);
							 }
						   matrixDraw();
						   
						
					}
					
				});	  
				dispatcher.addListener("pd_patNum", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
						patNum = x;
						
					}
					
				});	  	  
				dispatcher.addListener("pd_stepTime", new PdListener.Adapter() {
					@Override
					public void receiveFloat(String source, final float x) {
					     durTime = PApplet.parseInt(x)/2;
					    //  println("durtime" +durTime);
						
					}
					
				});	  	 
		
				// listener bangs
				dispatcher.addListener("pd_kickB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
							 //   println("kick");
							    timer0 = millis()+durTime;
							   }
				
				});
				
				dispatcher.addListener("pd_snrB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
							 //   println("snr");
							    timer1 = millis()+durTime;
							   }
					
					
				});
				
				dispatcher.addListener("pd_hhtB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						 //  println("hht");
					    timer2 = millis()+durTime;
							   }
				});
				
				dispatcher.addListener("pd_ohhtB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						timer3 = millis()+durTime;
							   }
				});
				dispatcher.addListener("pd_shkrB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						 //   println("shkr");
					    timer4 = millis()+durTime;
					    
					   }
				});
					   
				dispatcher.addListener("pd_cbB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						  timer5 = millis()+durTime;
						    
						   }
				});	    
				dispatcher.addListener("pd_clapB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						timer6 = millis()+durTime;
					   
					   }
				});
				dispatcher.addListener("pd_rideB", new PdListener.Adapter() {
					
					@Override
					
					public void receiveBang(String source) {
						
						 timer7 = millis()+durTime;
						   
						   }
				});
				
	}

	protected void loadPatch() throws IOException {
		
		
		if (pd == 0) {
			File dir = getFilesDir();
			IoUtils.extractZipResource(
					getResources().openRawResource(
							com.zackhagan.dotmatrix_a.R.raw.patch), dir,
					true);
			File patchFile = new File(dir, "beatbox4.pd");
			pd = PdBase.openPatch(patchFile.getAbsolutePath());


		}
	}
	
	public static void matrixDraw() {
		float [] tempSeq = new float[128];
		   PdBase.readArray(tempSeq,0,"seq",0,128);
		   int i = 0;
		   for(int h = 0; h<8; h++){
			   for(int w = 0; w<16; w++){
			   
		   m1.matrix[w][h].on = PApplet.parseBoolean(PApplet.parseInt(tempSeq[i++]));
		   		}
		   }
	
	}

	//setup Processing stuff
	public void setup() {
		
//image logos
		logo = loadImage("dmguilogo.png");  // Load the image into the program
		copyr = loadImage("copyr.png");
		  cp5 = new ControlP5(this);
		  fileselector = new SelectLibrary(this);
		 
		  
          int fontSize = tBoxSize/4;
		  PFont p = createFont("Verdana",fontSize); 
		  cp5.setFont(p);
		  
		  
			
		  
		  
		  Main();
		  SongseqInit();

		 //oscview init
		 ov1.init();
		 ov2.init();
		 ovKick.init();
		 ovSnr.init();
		 ovHht.init();
		 ovOhht.init();
		 ovShkr.init();
		 ovCb.init();
		 ovClap.init();
		 ovRide.init();
		 
		 // load initial pattern
		 loadpatternALL loader = new loadpatternALL();
		  loader.execute(song);
		 //loadPatternAll(song);
		 
		// patterns[0].loadPattern();
		 matrixDraw();

		}
	
	
		public void draw() {
		  background(0);
		  //textSize((sketchHeight()-(mposY+tBoxSize*10))/2);
		  fill(255);
			//text("dot.Matrix by Zack Hagan (c)2014 Roc-elle Records", 0, sketchHeight()-tBoxSize/2); 
	   
		//textSize(((sketchHeight()-(mposY+tBoxSize*10))/4)*3);
			  image(logo, 0,0,tBoxSize*3,tBoxSize);
			  
			  image(copyr,0,tBoxSize*11,sketchWidth()/2,(sketchHeight()-(mposY+tBoxSize*10)));
			//text("dot.Matrix", 0, mposY-tBoxSize/4); 
		  //text(textField.getText(), 20, 20);
		  // call the drawBoxes function for drum feedback lights
		  time = millis();
		  
		  drawBoxes();
		  drawBoxes2();
		  
		  instrSelect();
		}
		
		public void mousePressed() {
		
		//println(dialogOpen);
		  if (  dialogOpen==false &&
		       instrSel==10||instrSel==-1 
		       ) {
		    m1.mPressed();
		  }
		}

		public void mouseDragged() {
		  if ( dialogOpen==false && instrSel==10||instrSel==-1 ) {

		    m1.mDragged();
		  }
		}

		public void mouseReleased() {
		  			  
		  if ( dialogOpen==false && instrSel==10||instrSel==-1) {

		    m1.mReleased();
		  }
		}
		// set the pattern name in the pattern array when done with text
		
		
		
		
		public void Main(){
		   
			
		  
		  Group g1 = cp5.addGroup("g1")
		                .setPosition(mposX+tBoxSize*16,mposY)
		                .setWidth(tBoxSize*4)
		                .activateEvent(true)
		                .setBackgroundColor(color(175))
		                .setBackgroundHeight(tBoxSize*8)
		                .close()  
		                .setLabel("solo/mute/clear/open")
		                .setBarHeight(mposY)
		                ;
		   
		    Group g2 = cp5.addGroup("g2")
		                .setPosition(mposX+tBoxSize*2,mposY)
		                .setWidth(tBoxSize*6)
		                .activateEvent(true)
		                .setBackgroundColor(color(175))
		                .setBackgroundHeight(tBoxSize*3)
		                .close()  
		                .setLabel("delay/reverb")
		                .setBarHeight(mposY)

		                ; 
		  
		      Group g4 = cp5.addGroup("g4")
		                .setPosition(mposX+tBoxSize*8,mposY)
		                .setWidth(tBoxSize*6)
		                .activateEvent(true)
		                .setBackgroundColor(color(255,80))
		                .setBackgroundHeight(tBoxSize*8)
		                .close()  
		                .setLabel("Load/Save/Copy/Clear Pattern")
		                .setBarHeight(mposY)
		                ;          
		      

		     
		     
	        	
		      // about me group 
		      
		Group aboutMe = cp5.addGroup("aboutMe")
		            .setBarHeight(mposY)
		        	.setLabel("about")
		        	.setPosition(mposX+tBoxSize*14,mposY)
		            .setBackgroundHeight(tBoxSize*8)
		        	.setWidth(tBoxSize*2)
		        	.activateEvent(true)
		        	.close()

		      		;
		      aboutMe.addCanvas(new TestCanvas());
		      
		  int bangSize = tBoxSize;
		  
		   // setup button and slider arrays
		   
		   solo = new Toggle[numofi];
		   mute = new Toggle[numofi];
		   clear = new Bang[numofi];
		   drumvol = new Slider[numofi];
		   open = new Bang[numofi];
		   instrSelect = new Toggle[11];
		   
		  for(int i = 0; i < numofi; i++){
		 
		   int posX = mposX + tBoxSize*16;
		   int posY = (i * tBoxSize) + mposY;
		 
		   int posX1 = 0;
		   int posY1 = (i * tBoxSize);
		   int stepv = i + 1;
		 
		 drumvol[i] = cp5.addSlider("Vol"+stepv+"")
		     //.setValue(0)
		     .setPosition(posX,posY)
		     .setSize(tBoxSize*4,tBoxSize-2)
		     
		     .setRange(0,100)
		     ;
		  drumvol[i].getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		  
		 solo[i] = cp5.addToggle("solo"+stepv+"")
		     //.setValue(0)
		     .setPosition(posX1,posY1)
		     .setSize(tBoxSize-2,tBoxSize-2)
		     .setGroup(g1)
		     //.setLabelVisible(false) 
		    // .setCaptionLabel("s"+stepv+"")
		     ;
		 solo[i].getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);
		   
		 mute[i] = cp5.addToggle("mute"+stepv+"")
		     //.setValue(1)
		     .setPosition(posX1+tBoxSize,posY1)
		     .setSize(tBoxSize-2,tBoxSize-2)
		     .setGroup(g1)

		     ;
		     
		 mute[i].getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);
		 
		 clear[i] = cp5.addBang("Clear"+stepv+"")
		     .setValue(0)
		     .setPosition(posX1+tBoxSize*2,posY1)
		     .setSize(tBoxSize-2,tBoxSize-2)
		     .setGroup(g1)
		     .setCaptionLabel("clr"+stepv+"")
		     ;
		 clear[i].getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);


		 open[i] = cp5.addBang("open"+stepv+"")
		     //.setValue(0)
		     .setPosition(posX1+tBoxSize*3,posY1)
		     .setSize(tBoxSize-2,tBoxSize-2)
		     //.setLabelVisible(false) 
		      .setGroup(g1)
		     // .setCaptionLabel("o"+stepv+"")
		     ;
		 open[i].getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);
		 
		 
		  }
		  
		  drumvol[0].setColorActive(dColor1).setColorForeground(dColor1);
		  drumvol[1].setColorActive(dColor2).setColorForeground(dColor2);
		  drumvol[2].setColorActive(dColor3).setColorForeground(dColor3);
		  drumvol[3].setColorActive(dColor4).setColorForeground(dColor4);
		  drumvol[4].setColorActive(dColor5).setColorForeground(dColor5);
		  drumvol[5].setColorActive(dColor6).setColorForeground(dColor6);
		  drumvol[6].setColorActive(dColor7).setColorForeground(dColor7);
		  drumvol[7].setColorActive(dColor8).setColorForeground(dColor8);
		  
		  solo[0].setColorActive(dColor1).setColorForeground(dColor1);
		  solo[1].setColorActive(dColor2).setColorForeground(dColor2);
		  solo[2].setColorActive(dColor3).setColorForeground(dColor3);
		  solo[3].setColorActive(dColor4).setColorForeground(dColor4);
		  solo[4].setColorActive(dColor5).setColorForeground(dColor5);
		  solo[5].setColorActive(dColor6).setColorForeground(dColor6);
		  solo[6].setColorActive(dColor7).setColorForeground(dColor7);
		  solo[7].setColorActive(dColor8).setColorForeground(dColor8);
		  
		  mute[0].setColorActive(dColor1).setColorForeground(dColor1);
		  mute[1].setColorActive(dColor2).setColorForeground(dColor2);
		  mute[2].setColorActive(dColor3).setColorForeground(dColor3);
		  mute[3].setColorActive(dColor4).setColorForeground(dColor4);
		  mute[4].setColorActive(dColor5).setColorForeground(dColor5);
		  mute[5].setColorActive(dColor6).setColorForeground(dColor6);
		  mute[6].setColorActive(dColor7).setColorForeground(dColor7);
		  mute[7].setColorActive(dColor8).setColorForeground(dColor8);
		  
		  clear[0].setColorActive(dColor1).setColorForeground(dColor1);
		  clear[1].setColorActive(dColor2).setColorForeground(dColor2);
		  clear[2].setColorActive(dColor3).setColorForeground(dColor3);
		  clear[3].setColorActive(dColor4).setColorForeground(dColor4);
		  clear[4].setColorActive(dColor5).setColorForeground(dColor5);
		  clear[5].setColorActive(dColor6).setColorForeground(dColor6);
		  clear[6].setColorActive(dColor7).setColorForeground(dColor7);
		  clear[7].setColorActive(dColor8).setColorForeground(dColor8);
		  
		  open[0].setColorActive(dColor1).setColorForeground(dColor1);
		  open[1].setColorActive(dColor2).setColorForeground(dColor2);
		  open[2].setColorActive(dColor3).setColorForeground(dColor3);
		  open[3].setColorActive(dColor4).setColorForeground(dColor4);
		  open[4].setColorActive(dColor5).setColorForeground(dColor5);
		  open[5].setColorActive(dColor6).setColorForeground(dColor6);
		  open[6].setColorActive(dColor7).setColorForeground(dColor7);
		  open[7].setColorActive(dColor8).setColorForeground(dColor8);
		  
		  for(int i = 0; i<10;i++){
		   // println("making inst sel");
		    int posX = 0;
		    int posY = (i * tBoxSize) + mposY;
		    int stepv = i + 1;
		    
		    instrSelect[i] = cp5.addToggle("drum"+stepv+"")
		     //.setValue(0)
		     .setPosition(posX,posY)
		     .setSize(tBoxSize-2,tBoxSize-2)
		     //.setLabelVisible(false) 
		     ;
		 instrSelect[i].getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0).setPaddingY(-10); 
		  }
		  
		  //setting the labels for the instrSelect
		 instrSelect[0].getCaptionLabel().set("Kick");
		 instrSelect[0].setColorActive(dColor1).setColorForeground(dColor1);
		 instrSelect[1].getCaptionLabel().set("Snare");
		 instrSelect[1].setColorActive(dColor2).setColorForeground(dColor2);
		 instrSelect[2].getCaptionLabel().set("HiHat");
		 instrSelect[2].setColorActive(dColor3).setColorForeground(dColor3);
		 instrSelect[3].getCaptionLabel().set("OHHT");
		 instrSelect[3].setColorActive(dColor4).setColorForeground(dColor4);
		 instrSelect[4].getCaptionLabel().set("Shaker");
		 instrSelect[4].setColorActive(dColor5).setColorForeground(dColor5);
		 instrSelect[5].getCaptionLabel().set("CowBell");
		 instrSelect[5].setColorActive(dColor6).setColorForeground(dColor6);
		 instrSelect[6].getCaptionLabel().set("Clap");
		 instrSelect[6].setColorActive(dColor7).setColorForeground(dColor7);
		 instrSelect[7].getCaptionLabel().set("Ride");
		 instrSelect[7].setColorActive(dColor8).setColorForeground(dColor8);
		 
		  
		 instrSelect[8].getCaptionLabel().set("Song").align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 
		 instrSelect[9].getCaptionLabel().set("Matrix").align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 
		 instrSelect[8].setSize(tBoxSize-2,tBoxSize*2+5);
		 instrSelect[9].setPosition(tBoxSize,tBoxSize*8+mposY+5).setSize(tBoxSize*2-2,tBoxSize*2-2);
		 
		  // radio buttons
		 
		   rES = cp5.addRadioButton("EnvSel")
		         
		         .setPosition(mposX+tBoxSize/4, tBoxSize*7 + tBoxSize/2)
		         .setSize(tBoxSize,tBoxSize)
		         
		         .setItemsPerRow(8)
		         .setSpacingColumn(tBoxSize+tBoxSize/3)
		         .setSpacingRow(15)
//		         .setGroup(drum1)
		         .addItem("Velocity",1)
		         .addItem("Pitch",2)
		         .addItem("Start",3)
		         .addItem("Length",4)
		         .addItem("Reverb",5)
		         .addItem("Delay",6)
		         .addItem("Pan",7)
		         
		         
		         ; 
		  rES.activate(0);
		  rES.getValueLabel().align(ControlP5.BOTTOM, ControlP5.LEFT).setPaddingX(0);
		  
       // pattern name corresponds with void onClickWidget
		  
		  widgetContainer = new APWidgetContainer(this); //create new container for widgets
		  textField = new APEditText(mposX+tBoxSize*14, mposY+tBoxSize*9, tBoxSize*3, tBoxSize); //create a textfield from x- and y-pos., width and height
		  widgetContainer.addWidget(textField); //place textField in container
          textField.setTextColor(255,255,255,254);
          textField.setTextSize(10);
          textField.setInputType(InputType.TYPE_CLASS_TEXT);
          textField.setImeOptions(EditorInfo.IME_ACTION_DONE);
          textField.setCloseImeOnDone(true);
		  

		  
		  
		  // toggles and buttons and transport and copy
		  
		 
		   cp5.addTextfield("songTitle")
		     //.setPosition(mposX+tBoxSize*16,tBoxSize*9+mposY)
		     .setSize(tBoxSize*5-2,tBoxSize-2)
		     .setFont(createFont("arial",20))
		     .setAutoClear(false)
		     //.setValue(patternName)
		     .setLabelVisible(false)
		     .setGroup(g4)
		     .setLock(true)
		     ;
		   cp5.addBang("load")
		       .setPosition(tBoxSize*5,0)
		       .setSize(bangSize-2,bangSize-2)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("load")
		       .setGroup(g4)
		       ;
		     cp5.getController("load").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 
		  
		    bar = cp5.addNumberbox("bar")
		       .setPosition(mposX+tBoxSize*16,tBoxSize*8+mposY)
		       .setSize(bangSize-2,bangSize-2)
		       .setCaptionLabel("")
		       .setLock(true)
		       .setDecimalPrecision(0)
		       ; 
		   cp5.getController("bar").getValueLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);

		   cp5.addNumberbox("bpmdisplay")
		       .setPosition(mposX+tBoxSize*17,tBoxSize*9+mposY)
		       .setSize(bangSize-2,bangSize-2)
		       .setCaptionLabel("BPM")
		       .setLock(true)
		       .setDecimalPrecision(0)
		       ; 
		     cp5.getController("bpmdisplay").getCaptionLabel().align(ControlP5.CENTER, ControlP5.TOP).setPaddingX(0);

		  cp5.addToggle("On")
		        .setPosition(mposX+tBoxSize*18,tBoxSize*8+mposY)
		        .setSize(bangSize*2,bangSize*2)
		       .setCaptionLabel("Play/Stop")
		       //.setGroup(main)
		       .setValue(false)
		       ;  
		   cp5.getController("On").getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		 
		   
		 
		  cp5.addBang("next")
		       .setPosition(mposX+tBoxSize*17,tBoxSize*8+mposY)
		       .setSize(bangSize-2,bangSize-2)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("next >>")
		  
		       ;
		 
		  cp5.getController("next").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);

		  cp5.addBang("prev")
		       .setPosition(mposX+tBoxSize*15,tBoxSize*8+mposY)
		       .setSize(bangSize-2,bangSize-2)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("<< prev")
		        ;
		        cp5.getController("prev").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);
		  
		  cp5.addBang("restart")
		       .setPosition(mposX+tBoxSize*14,tBoxSize*8+mposY)
		       .setSize(bangSize-2,bangSize-2)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("|<")
		  
		       ;
		 
		  cp5.getController("restart").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 
		 
		  cp5.addToggle("seqOn")
		     .setPosition(mposX+tBoxSize*13,tBoxSize*8+mposY)
		     .setSize(tBoxSize-2,tBoxSize*2-2)
		     .setCaptionLabel("seq On")
		     //.setGroup(main)
		     .setValue(false)
		     
		     ;
		  cp5.getController("seqOn").getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		  
		   cp5.addBang("clearAll")
		       .setPosition(0, tBoxSize*4)
		       .setSize(bangSize*6,bangSize)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("Clear Pattern")
		       .setGroup(g4)
		      
		       ;
		    cp5.getController("clearAll").getCaptionLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		 
		 
		  
		    cp5.addBang("save1")
		       .setPosition(tBoxSize*5,tBoxSize)
		       .setSize(bangSize-2,bangSize-2)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("save")
		       .setGroup(g4)

		       ;
		     cp5.getController("save1").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0);
		   
		   // copy controls
		   
		      cp5.addBang("cbar")
		       .setPosition(0,tBoxSize*2)
		       .setSize(bangSize,bangSize)
		       .setTriggerEvent(Bang.PRESSED)
		       //.setGroup(main)
		       .setCaptionLabel("copy bar")
		       .setGroup(g4)
		       ;
		     cp5.getController("cbar").getCaptionLabel().align(ControlP5.LEFT, ControlP5.BOTTOM).setPaddingX(0);
		 
		       

		 
		  copybar = cp5.addNumberbox("copybar")
		       .setPosition(tBoxSize*2,tBoxSize*2)
		       .setSize(tBoxSize,tBoxSize)
		       .setMultiplier(1)
		       .setScrollSensitivity(1.01f) // set the sensitifity of the numberbox
		       .setCaptionLabel("To")
		       .setRange(0,64)
		       .setGroup(g4)
		       .setLock(true)
		       ; 
		  copybar.getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP).setPaddingX(0);   
		 
		  cp5.addBang("copyplus")
		     //.setVisible(false)
		     .setPosition(tBoxSize*2,tBoxSize*2-tBoxSize/2)
		     .setSize(tBoxSize,tBoxSize/2)
		     .setGroup(g4)
		     .setCaptionLabel("+")
		     ;
		       cp5.getController("copyplus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 

		      cp5.addBang("copyminus")
		     //.setVisible(false)
		     .setPosition(tBoxSize*2,tBoxSize*3)
		     .setSize(tBoxSize,tBoxSize/2)
		     .setGroup(g4)
		     .setCaptionLabel("-")
		     ;
		    cp5.getController("copyminus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 


		    cp5.addNumberbox("copyLength")
		     
		     //.setVisible(false)
		     .setPosition(tBoxSize*3,tBoxSize*2)
		     .setSize(tBoxSize,tBoxSize)
		     .setRange(1,64)
		     .setMultiplier(1) // set the sensitifity of the numberbox
		     //.setDirection(Controller.HORIZONTAL) // change the control direction to left/right
		     //.setValue(1)
		     .setGroup(g4)
		     .setLock(true)
		     .setCaptionLabel("for")
		     ;
		      cp5.getController("copyLength").getCaptionLabel().align(ControlP5.LEFT, ControlP5.TOP).setPaddingX(0); 
		     
		     cp5.addBang("copyLplus")
		     //.setVisible(false)
		     .setPosition(tBoxSize*3,tBoxSize*2-tBoxSize/2)
		     .setSize(tBoxSize,tBoxSize/2)
		     .setGroup(g4)
		     .setCaptionLabel("+")
		     ;
		       cp5.getController("copyLplus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 

		      cp5.addBang("copyLminus")
		     //.setVisible(false)
		     .setPosition(tBoxSize*3,tBoxSize*3)
		     .setSize(tBoxSize,tBoxSize/2)
		     .setGroup(g4)
		     .setCaptionLabel("-")
		     ;
		    cp5.getController("copyLminus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 


		 cp5.addSlider("bpm")
		    .setPosition(mposX+tBoxSize*2,mposY+tBoxSize*8)
		     .setSize(tBoxSize*7,tBoxSize-2)
		     .setRange(40,220)
		     .setValue(117)
		     .setDecimalPrecision(0)
		     .setCaptionLabel("BPM")
		     .setSliderMode(Slider.FLEXIBLE)
		     //.setGroup(g2)
		     ; 
		   cp5.getController("bpm").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		   cp5.getController("bpm").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);   
		  //slider for shuffle
		  
		  cp5.addSlider("shuffle")
		     .setPosition(mposX+tBoxSize*2,mposY+tBoxSize*9)
		     .setSize(tBoxSize*7,tBoxSize-2)
		     .setRange(0,0.33f)
		     .setValue(0)
		     .setDecimalPrecision(2)
		     .setCaptionLabel("Shuffle")
		     .setSliderMode(Slider.FLEXIBLE)
		     //.setGroup(g2)
		     ; 
		   cp5.getController("shuffle").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		   cp5.getController("shuffle").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);

		  
		  // fx sliders
		  
		  cp5.addSlider("dFB")
		    .setPosition(0, 0)
		     .setSize(tBoxSize*6,tBoxSize-2)
		     .setRange(0,0.95f)
		     .setValue(0.60f)
		     .setDecimalPrecision(2)
		     .setCaptionLabel("del fb")
		     .setSliderMode(Slider.FLEXIBLE)
		     .setGroup(g2)

		     ; 
		  cp5.getController("dFB").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		  cp5.getController("dFB").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		 
		 cp5.addSlider("dStep")
		    .setPosition(0, (tBoxSize*1))
		     .setSize(tBoxSize*6,tBoxSize-2)
		     .setRange(1,8)
		     .setValue(3)
		     .setDecimalPrecision(0)
		     .setCaptionLabel("del step")
		     .setNumberOfTickMarks(8)
		     .setSliderMode(Slider.FLEXIBLE)
		     .setGroup(g2)
		     ; 
		  cp5.getController("dStep").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0); 
		  cp5.getController("dStep").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		 
		  cp5.addSlider("vDecay")
		    .setPosition(0,tBoxSize*2)
		     .setSize(tBoxSize*6,tBoxSize-2)
		     .setRange(50,100)
		     .setValue(85)
		     .setDecimalPrecision(0)
		     .setCaptionLabel("Rvb Dec")
		     .setSliderMode(Slider.FLEXIBLE)
		     .setGroup(g2)
		     ; 
		  cp5.getController("vDecay").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		  cp5.getController("vDecay").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		 
		 cp5.addSlider("vXover")
		    .setPosition(0,tBoxSize*3)
		     .setSize(tBoxSize*6,tBoxSize-2)
		     .setRange(100, 8000)
		     .setValue(3000)
		     .setDecimalPrecision(0)
		     .setCaptionLabel("Rvb Xvr")
		     .setSliderMode(Slider.FLEXIBLE)
		     .setGroup(g2)
		     ; 
		  cp5.getController("vXover").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		  cp5.getController("vXover").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		 
		  cp5.addSlider("vDamp")
		    .setPosition(0, tBoxSize*4)
		     .setSize(tBoxSize*6,tBoxSize-2)
		     .setRange(0,100)
		     .setValue(40)
		     .setDecimalPrecision(0)
		     .setCaptionLabel("Rvb Damp")
		     .setSliderMode(Slider.FLEXIBLE)
		     .setGroup(g2)
		     ; 
		  cp5.getController("vDamp").getValueLabel().align(ControlP5.LEFT, ControlP5.CENTER).setPaddingX(0);
		  cp5.getController("vDamp").getCaptionLabel().align(ControlP5.RIGHT, ControlP5.CENTER).setPaddingX(0);
		  
		 
		 
		}

	

		// functipn for on/off

		public void On(boolean theFlag) {
		  if(theFlag==true) {
		    isPlaying = true;
		    PdBase.sendFloat("pd_power", (float)1);
		  } else {
		    PdBase.sendFloat("pd_power", (float)0);
		    isPlaying = false;
		  }
		}

		// function for clear all

		public void clearAll() {
			new ClearFragment(Dm_beatbox.this).show(getFragmentManager(), "MyDialog");

			/*
			PdBase.sendBang("pd_clear");
	        m1.cCells();
	        matrixDraw();
	        */
		}
		
		public void next(){
		
		  PdBase.sendBang("pd_next");
		}
		public void prev(){
		
		 PdBase.sendBang("pd_prev");
		}

		public void save1(){
		  
		  fileselector.selectOutput("Select a file to write to:", "fileSelected");
		}
		public void fileSelected(File selection) {
		  if (selection == null) {
		   //println("Window was closed or the user hit cancel.");
		  } else {
			  toast(getString(R.string.saved)+selection.getAbsolutePath());

			//  println("User selected " + selection.getAbsolutePath());
		     // Save to File
		  // The same file is overwritten by adding the data folder path to saveStrings().
		String[] saveData = new String[65];
		   
		
		for(int i = 0; i<patterns.length; i++){  
			    
			   String seqData="";
			   String velData="";
			   String pitData="";
			   String startData="";
			   String lengthData="";
			   String verbData="";
			   String delayData="";
			   String panData="";
			   String drmvolData="";
			   String drmNameData="";
			   String drmPathNameData="";
			   String slidervalData="";
			 
			  


			  
			   for(int s = 0; s< patterns[i].sliderval.length; s++){
			      slidervalData = slidervalData+ patterns[i].sliderval[s]+",";
			   }
			    
			   for(int s = 0; s< patterns[i].drmvol.length; s++){
			     
			    //   println("drmvol saving "+drmvol[s]);
			      drmvolData = drmvolData+ patterns[i].drmvol[s]+",";

			   }
			    for(int s = 0; s< patterns[i].drmName.length; s++){
			     // drmName[s] =  drmName[s];
			      drmNameData = drmNameData+ patterns[i].drmName[s]+",";
			       }
			    for(int s = 0; s< patterns[i].drmPathName.length; s++){
				     // drmName[s] =  drmName[s];
				      drmPathNameData = drmPathNameData+ patterns[i].drmPathName[s]+",";
				       }
			    for(int s = 0; s< patterns[i].seq.length; s++){
			    // Concatenate bubble variables
			    seqData =seqData+patterns[i].seq[s]+",";
			    velData =velData+ patterns[i].vel[s]+",";
			    pitData =pitData+ patterns[i].pit[s]+",";
			    startData =startData+ patterns[i].start[s]+",";
			    lengthData =lengthData+ patterns[i].slength[s]+",";
			    verbData =verbData+ patterns[i].verb[s]+",";
			    delayData =delayData+ patterns[i].delay[s]+",";
			    panData =panData+ patterns[i].pan[s]+",";
			    
			      //data[i] = ""+ seq[s]; //
			     } 
			  
			       saveData[i] = patterns[i].pName+","
			       +drmPathNameData	   
			       +drmNameData
			       +drmvolData
			       +slidervalData
			       +seqData
			       +velData
			       +pitData
			       +startData
			       +lengthData
			       +verbData
			       +delayData
	               +panData;
	              
		   } // end of saving patterns text files 
		     String sngbtnData="";
		    for(int i = 0; i<sngbtn.length;i++){
		    	int s = i+1;
		    	sngbtnData = sngbtnData+ cp5.getController("measurebox"+s).getValue()+",";	
		    	
		    }
		    saveData[64] =	sngbtnData;
		   // println("saving file");
		    saveStrings(selection.getAbsolutePath()+".txt", saveData);
		   
		   }
		  
		}

		public void load(){
			fileselector.selectInput("Select a file to load:", "fileload");
		}
		public void fileload(File selection) {
		  if (selection == null) {
		 //   println("Window was closed or the user hit cancel.");
		  } else {
		 //   println("User selected " + selection.getAbsolutePath());
		     // Save to File
		  // The same file is overwritten by adding the data folder path to saveStrings().
		  for(int i = patterns.length; i>=0; i--){
		   // println("shortening");
		shorten(patterns);
		  }
		  song = selection.getAbsolutePath();
		  
		  loadpatternALL loader = new loadpatternALL();
		  loader.execute(song);
		 // loadPatternAll(song);
		  cp5.get(Textfield.class,"songTitle").setText( selection.getName());
		 // patterns[0].loadPattern();
		  matrixDraw();
		  }
		}

//copy pattern stuff

		public void cbar(){
			
			new CopyFragment(Dm_beatbox.this).show(getFragmentManager(), "MyDialog");
			
			//startActivity(new Intent(Dm_beatbox.this, CopyDialog.class));
		}

		public static void copyPattern(){
			for(int i = PApplet.parseInt(cbar); i<cbar+copyLength; i++){
			    
				 patterns[PApplet.parseInt(i)].copyPattern();
				  }
		}
		
		
		public void restart(){
		  
		    PdBase.sendBang("pd_rewind");

		 
		}

		public void seqOn(boolean theFlag) {
		  if(theFlag==true) {
		    PdBase.sendFloat("pd_link", (float)1);
		  //  println("pd_link");
		    seqOn = true;
		  } else {
		    PdBase.sendFloat("pd_link", (float)0);
		    seqOn = false;
		  }
		  
		}


		public void SongseqInit(){
		 Group g5 = cp5.addGroup("g5")
		                .setPosition(0,0)
		                .setVisible(false)
		                .hideBar()
		                //.setWidth(tBoxSize*4)
		                //.activateEvent(true)
		                //.setBackgroundColor(color(255,80))
		               // .setBackgroundHeight(tBoxSize*8)
		                //.close()  
		                //.setLabel()
		                ;
		 sgL = cp5.addNumberbox("seqLength")
		     
		     //.setVisible(false)
		     .setPosition(mposX,height/4)
		     .setSize((tBoxSize*3)/2,tBoxSize)
		     .setRange(1,32)
		     //.setValue(1)
		     .setGroup(g5)
		     .setLock(true)
		     .setCaptionLabel("seq \n length")
		     
		     ;
	       cp5.getController("seqLength").getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(0); 

		     cp5.addBang("seqplus")
		     //.setVisible(false)
		     .setPosition(mposX,height/4-tBoxSize/2)
		     .setSize((tBoxSize*3)/2,tBoxSize/2)
		     .setGroup(g5)
		     .setCaptionLabel("+")
		     ;
		       cp5.getController("seqplus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 

		      cp5.addBang("seqminus")
		     //.setVisible(false)
		     .setPosition(mposX,height/4 + tBoxSize)
		     .setSize((tBoxSize*3)/2,tBoxSize/2)
		     .setGroup(g5)
		     .setCaptionLabel("-")
		     ;
		    cp5.getController("seqminus").getCaptionLabel().align(ControlP5.CENTER, ControlP5.TOP).setPaddingX(0); 

		     
		rWav = cp5.addBang("recWav")
		    // .setVisible(false)
		     .setPosition(mposX,height/4 + tBoxSize*2+15)
		     .setSize((tBoxSize*3)/2,tBoxSize)
		     .setGroup(g5)
		     .setCaptionLabel("record \n wav \n file")
		     ;
		 
	       cp5.getController("recWav").getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.TOP).setPaddingX(0); 

		  sngbtn = new Sngbtn[songLength+8]; 
		  
		     int i = 0;
		     int unit = tBoxSize+tBoxSize/2;
		  for (int y = 0; y < 9; y++) {
		    for (int x = 0; x < 8; x++) {
		      sngbtn[i++] = new Sngbtn(tBoxSize,i, tBoxSize*3+mposX+x*(unit+3), y*((unit/2)*3)); //size, number, pos X, pos Y
		    
		    }

		    
		   }
		   
		  
		}

		public void recWav(){
			
			new RecordFragment(Dm_beatbox.this).show(getFragmentManager(), "MyDialog");

			//fileselector.selectOutput("Select a file to write to:", "fileRecSave");
		}
	  static public void recWavFileSelect(){
	    PdBase.sendFloat("pd_power", (float)0);
      	//PdBase.sendBang("pd_recStart");
      	PdBase.sendFloat("pd_link", (float)1);
      	seqOn=true;
      	cp5.getController("seqOn").setValue(1);
      	fileselector.selectOutput("Select a file to write to:", "fileRecSave");

	  }
	  
	     public void fileRecSave(File selection) {
			  if (selection == null) {
			   // println("Window was closed or the user hit cancel.");
			  } else {
			   // println("User selected " + selection.getAbsolutePath());
			     // Save to File
			  // The same file is overwritten by adding the data folder path to saveStrings().
			   
			    PdBase.sendSymbol("pd_recName", selection.getAbsolutePath());
			    PdBase.sendBang("pd_rwav");	
			    PdBase.sendBang("pd_recStart");
			    PdBase.sendFloat("pd_power", 1);	
			    //PdBase.sendBang("pd_rewind");
			   
			    
			  }
			}
	     
		 public void seqplus(){
		   if(seqLength<32){
		   seqLength++;
		   cp5.getController("seqLength").setValue(seqLength);
		   }
		}
		 public void seqminus(){
		   if(seqLength>0){
		   seqLength--;
		   cp5.getController("seqLength").setValue(seqLength);
		   }
		}

	
		
		public void controlEvent(ControlEvent theEvent) {
		    

		 
		    

		  
		  // sngbuttons
		 
		  for (int i=0;i<seqLength+1;i++) {
		    
		    if (theEvent.isFrom("mplus"+i)&& sngbtn[i].mNum<=songLength) {
		      sngbtn[i].mNum = sngbtn[i].mNum+1;
		      cp5.getController("measurebox"+i).setValue(sngbtn[i].mNum);
		    }
		   
		    if (theEvent.isFrom("mminus"+i) && sngbtn[i].mNum>0) {
		      sngbtn[i].mNum = sngbtn[i].mNum -1;
		      cp5.getController("measurebox"+i).setValue(sngbtn[i].mNum);
		    }
		 
		    
		  }

		 // text fields
		 
		  if(theEvent.isAssignableFrom(Textfield.class)) {
		
		            if(theEvent.getName()=="patName"){
		            //	println("name "+theEvent.getStringValue());
		            patterns[PApplet.parseInt(sbar)].pName =  theEvent.getStringValue();
		            }
		  }
		  
		  
		// group stuff so that the they ovelapping works
		 if(theEvent.isFrom("g2")) {    
		          
		            if(cp5.getGroup("g2").isOpen()==true){
		             dialogOpen = true;
		           //  println("open"+dialogOpen);
		            }
		            if(cp5.getGroup("g2").isOpen()==false && cp5.getGroup("g4").isOpen()==false 
		            		&& cp5.getGroup("aboutMe").isOpen()==false){
		             dialogOpen = false;
		          //    println("close"+dialogOpen);
		            }
		        }
		  
		   if(theEvent.isFrom("g4")) {    
		          
		            if(cp5.getGroup("g4").isOpen()==true){
		             dialogOpen = true;
		          //   println("open"+dialogOpen);
		            }
		            if(cp5.getGroup("g4").isOpen()==false && cp5.getGroup("g2").isOpen()==false 
		            		&& cp5.getGroup("aboutMe").isOpen()==false){
		             dialogOpen = false;
		          //    println("close"+dialogOpen);
		            }
		        }
		        
		   if(theEvent.isFrom("g1")) {
		 ///   println("got an event from g1 ");
		    if(theEvent.getGroup().isOpen()){
		      for (int i=0;i<drumvol.length;i++) {
		      drumvol[i].setVisible(false);
		      }
		    }
		      else{
		        for (int i=0;i<drumvol.length;i++) {
		      drumvol[i].setVisible(true);
		      } 
		    //  +", isOpen? "+theEvent.getGroup().isOpen()
		        
		      }      
		   
		   }
		   
		   if(theEvent.isFrom("aboutMe")) {
			 //  println("aboutMe");
		       if(cp5.getGroup("aboutMe").isOpen()==true){
		             dialogOpen = true;
		             cp5.getGroup("g2").close();
		             cp5.getGroup("g4").close();
		          //   println("open"+dialogOpen);
		            }
		            if(cp5.getGroup("g4").isOpen()==false && cp5.getGroup("g2").isOpen()==false 
		            		&& cp5.getGroup("aboutMe").isOpen()==false){
		             dialogOpen = false;
		          //    println("close"+dialogOpen);
		            }
				   }
		  
		  // copy events
		  
		  if (theEvent.isFrom(copybar)) {
		    cbar = copybar.getValue();
		   // println("cbar "+cbar);
		  }
		  
		  // copy  a measure fields  
		  
		  if (theEvent.isFrom("copyplus")&& copyLength<32) {
		      cbar++;
		      cp5.getController("copybar").setValue(cbar);
		    }
		   
		    if (theEvent.isFrom("copyminus") && copyLength>0) {
		      cbar--;
		      cp5.getController("copybar").setValue(cbar);
		    }
		  
		    if (theEvent.isFrom("copyLplus")&& copyLength<32) {
		      copyLength++;
		      cp5.getController("copyLength").setValue(copyLength);
		    }
		   
		    if (theEvent.isFrom("copyLminus") && copyLength>1) {
		      copyLength--;
		      cp5.getController("copyLength").setValue(copyLength);
		    }
		  
		  //instrument select
		  
		for (int i=0;i<10;i++) {

		  if (theEvent.isFrom(instrSelect[i])) {
		   
		   
		         
		  if(instrSelect[i].getValue() == 1){
		         tempi1 = i;
		         instrSel = i+1;
		         instrSelOn=true;
		         
		         PdBase.readArray(VelGraph.values,0,"vel",i*16,16);
		         PdBase.readArray(PitGraph.values,0,"pit",i*16,16); 
		         PdBase.readArray(StaGraph.values,0,"sstart",i*16,16);
		         PdBase.readArray(LenGraph.values,0,"length",i*16,16);
		         PdBase.readArray(VerbGraph.values,0,"verb",i*16,16); 
		         PdBase.readArray(DelGraph.values,0,"delay",i*16,16);
		         PdBase.readArray(PanGraph.values,0,"pan",i*16,16);
		         
		         PdBase.sendFloat("pd_y", (float)i);
		         for(int m = 0; m<10; m++){
		            if(i!=m){
		          instrSelect[m].setValue(0);
		           
		           }
		          
		         }
		        }
		     if(instrSelect[tempi1].getValue()==0 && instrSelOn==true){
		       instrSelect[9].setValue(1);
		       instrSelOn=false;
		      }
		    }
		  }
		  // open command
		for (int i=0;i<numofi;i++) {
		    //int step = i+1;
		    if (theEvent.isFrom(open[i])) {
		     
		     fileselector.selectInput("Select a file to load:", "openfile"+i+""); 
		     }
		  }
		// fx slider sends
		
		 if (theEvent.isFrom("dFB")){
		     PdBase.sendFloat("pd_delFB", cp5.getController("dFB").getValue());
		     
		     patterns[PApplet.parseInt(sbar)].sliderval[2] = cp5.getController("dFB").getValue();
		   }
		 if (theEvent.isFrom("dStep")){
		     PdBase.sendFloat("pd_delStep", cp5.getController("dStep").getValue());
		     patterns[PApplet.parseInt(sbar)].sliderval[3] = cp5.getController("dStep").getValue();
		   }
		 if (theEvent.isFrom("vDecay")){
		     PdBase.sendFloat("pd_verbDecay", cp5.getController("vDecay").getValue());
		     patterns[PApplet.parseInt(sbar)].sliderval[4] = cp5.getController("vDecay").getValue();
		   } 
		 if (theEvent.isFrom("vXover")){
		     PdBase.sendFloat("pd_verbXover", cp5.getController("vXover").getValue());
		     patterns[PApplet.parseInt(sbar)].sliderval[5] = cp5.getController("vXover").getValue();
		   } 
		  if (theEvent.isFrom("vDamp")){
		     PdBase.sendFloat("pd_verbDamp", cp5.getController("vDamp").getValue());
		     patterns[PApplet.parseInt(sbar)].sliderval[6] = cp5.getController("vDamp").getValue();
		   } 
		// copy pattern bar

		 if (theEvent.isFrom("copybar")){
		     PdBase.sendFloat("barCopyOff", cp5.getController("copybar").getValue());
		   }
		   
		 //bpm
		  if (theEvent.isFrom("bpm")) {
		    PdBase.sendFloat("pd_bpm",(cp5.getController("bpm").getValue()));
		    cp5.getController("bpmdisplay").setValue(cp5.getController("bpm").getValue());
		    patterns[PApplet.parseInt(sbar)].sliderval[0] = cp5.getController("bpm").getValue();
		  }
		  
		   if (theEvent.isFrom("shuffle")) {
		    PdBase.sendFloat("pd_groove",(cp5.getController("shuffle").getValue()));
		    patterns[PApplet.parseInt(sbar)].sliderval[1] = cp5.getController("shuffle").getValue();
		   
		  }
		 
		  //song measures

		  for(int i = 0; i<songLength; i++){
		    float s = i-1;
		  if (theEvent.isFrom("measurebox"+i+"") ){
		       PdBase.sendFloat("sPat", (float)s);
		       PdBase.sendFloat("Pat", cp5.getController("measurebox"+i+"").getValue() );
		      // println("measure sent " +i +" pat " +cp5.getController(""+i+"").getValue() );
		     }
		  }

		  
		   if (theEvent.isFrom("seqLength")){
		     PdBase.sendFloat("seqLength", cp5.getController("seqLength").getValue());
		   }
		  // slider variables out to pd
		  for(int i = 0; i<numofi; i++){
		 
		     int step = i + 1;
		     
		     if (theEvent.isFrom(drumvol[i])) {
		       
		       PdBase.sendFloat("sVol", (float)i);
		       PdBase.sendFloat("Vol", drumvol[i].getValue());
		       PdBase.sendFloat("Vol"+step, (drumvol[i].getValue())/100);
		       patterns[PApplet.parseInt(sbar)].drmvol[i] = drumvol[i].getValue();
		       //println("i"+i+" "+drumvol[i].getValue());
		     }
		   }
		   
		   // solo logic...
		   
		       
		      
		      
		      for(int i = 0; i<numofi; i++){

		    
		     int step = i + 1;
		     if (theEvent.isFrom(cp5.getController("solo"+step))) {
		  
		       //println("temp i " +tempi);
		      if(solo[tempi].getValue() == 0 && soloOn == true){ 
		       instrSelect[9].setValue(1);
		        soloOn = false;
		        for(int m = 0; m<numofi; m++){
		           mute[m].setValue(0);
		        }
		        //println("solo off");
		       }
		       
		      
		       if(solo[i].getValue() == 1){
		        soloOn = true;
		        instrSelect[i].setValue(solo[i].getValue());
		        tempi=i;
		        
		        
		        for(int m = 0; m<numofi; m++){
		           mute[m].setValue(1);
		          
		          
		      
		           
		           if(i!=m){
		          solo[m].setValue(0);
		           
		                   }
		          if(i==m){ 
		           mute[m].setValue(0);
		            //if(solo[tempi].getValue() == 0){
		           //    mute[m].setValue(0);
		          // println("reset");
		           //  }
		            }
		          
		          
		          }
		       
		      
		         }
		          
		       }
		      
		      }  
		      
		  
		 // mute events   
		   
		    
		    for(int i = 0; i<numofi; i++){
		  
		     int step = i + 1;
		     if (theEvent.isFrom(cp5.getController("mute"+step))) {
		      PdBase.sendFloat("mute"+step, mute[i].getValue());
		       
		      // println("i"+i+" "+mute[i].getValue());
		     }
		   }
		   
		  // clear instrument for a measure
		    
		    for(int i = 0; i<numofi; i++){
		  
		     int step = i + 1;
		     if (theEvent.isFrom(cp5.getController("Clear"+step))) {
		        
		       PdBase.sendFloat("pd_y",(float)i);
		       PdBase.sendBang("pd_cdrum");
		       PdBase.readArray(VelGraph.values,0,"vel",i*16,16);
		       PdBase.readArray(PitGraph.values,0,"pit",i*16,16); 
		       PdBase.readArray(StaGraph.values,0,"sstart",i*16,16);
		       PdBase.readArray(LenGraph.values,0,"length",i*16,16);
		       PdBase.readArray(VerbGraph.values,0,"verb",i*16,16); 
	           PdBase.readArray(DelGraph.values,0,"delay",i*16,16);
	           PdBase.readArray(PanGraph.values,0,"pan",i*16,16);
		       
	           matrixDraw();
		       
		      // println("bang open"+i);
		     }
		   }
		}

		// methods for opening files all eight

		public void openfile0(File selection) {
		  if (selection == null) {
		  } else {
		    instrSelect[0].setCaptionLabel(selection.getName());
		    patterns[PApplet.parseInt(sbar)].drmPathName[0] = selection.getAbsolutePath();
		    patterns[PApplet.parseInt(sbar)].drmName[0] = selection.getName();
		  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
		   PdBase.sendSymbol("1Open", selection.getAbsolutePath());
		  }
		}

		public void openfile1(File selection) {
		  if (selection == null) {
		  //  println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[1].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[1] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[1] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
		   	    PdBase.sendSymbol("2Open", selection.getAbsolutePath());
		  }
		}

		public void openfile2(File selection) {
		  if (selection == null) {
		  //  println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[2].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[2] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[2] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("3Open", selection.getAbsolutePath());
		  }
		}

		public void openfile3(File selection) {
		  if (selection == null) {
		  //  println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[3].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[3] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[3] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("4Open", selection.getAbsolutePath());
		  }
		}

		public void openfile4(File selection) {
		  if (selection == null) {
		   // println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[4].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[4] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[4] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("5Open", selection.getAbsolutePath());
		  }
		}

		public void openfile5(File selection) {
		  if (selection == null) {
		   // println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[5].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[5] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[5] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("6Open", selection.getAbsolutePath());
		  }
		}

		public void openfile6(File selection) {
		  if (selection == null) {
		  //  println("Window was closed or the user hit cancel.");
		  } else {
		    	instrSelect[6].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[6] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[6] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("7Open", selection.getAbsolutePath());
		  }
		}

		public void openfile7(File selection) {
		  if (selection == null) {
		   // println("Window was closed or the user hit cancel.");
		  } else {
			    instrSelect[7].setCaptionLabel(selection.getName());
			    patterns[PApplet.parseInt(sbar)].drmPathName[7] = selection.getAbsolutePath();
			    patterns[PApplet.parseInt(sbar)].drmName[7] = selection.getName();
			  // patterns[int(sbar)].drmName[0] =  selection.getAbsolutePath();
			    PdBase.sendSymbol("8Open", selection.getAbsolutePath());
		  }
		}
		public void drawBoxes(){
		 
		  noStroke();
		  
		  
		 
		  rectMode(CORNER);
		  
		  fill(0,0,155,50);

		   
		  if(time < timer0){  
		   rect(mposX-tBoxSize,mposY+tBoxSize*0,tBoxSize,tBoxSize);
		     fill(255);
		 }
		 
		 rect(mposX-tBoxSize,mposY+tBoxSize*0,tBoxSize,tBoxSize);
		    fill(0,0,155);
		 
		 
		if(time < timer1){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*1,tBoxSize,tBoxSize);
		     fill(dColor2);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*1,tBoxSize,tBoxSize);
		     fill(0,0,155);
		 if(time < timer2){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*2,tBoxSize,tBoxSize);
		     fill(dColor3);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*2,tBoxSize,tBoxSize);
		     fill(0,0,155);  
		 
		if(time < timer3){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*3,tBoxSize,tBoxSize);
		     fill(dColor4);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*3,tBoxSize,tBoxSize);
		     fill(0,0,155); 
		   
		   if(time < timer4){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*4,tBoxSize,tBoxSize);
		     fill(dColor5);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*4,tBoxSize,tBoxSize);
		     fill(0,0,155);  
		 
		if(time < timer5){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*5,tBoxSize,tBoxSize);
		     fill(dColor6);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*5,tBoxSize,tBoxSize);
		     fill(0,0,155);
		 if(time < timer6){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*6,tBoxSize,tBoxSize);
		     fill(dColor7);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*6,tBoxSize,tBoxSize);
		     fill(0,0,155);  
		 
		if(time < timer7){  
		  rect(mposX-tBoxSize,mposY+tBoxSize*7,tBoxSize,tBoxSize);
		     fill(dColor8);
		 }
		 rect(mposX-tBoxSize,mposY+tBoxSize*7,tBoxSize,tBoxSize);
		     fill(0,0,155);      
		 
		}

		// boxes to show seq location

		public void drawBoxes2(){
		  int unit = tBoxSize+tBoxSize/2;
		  if(instrSel == 9 && seqOn == true){
		   
		   if(gBeat%4 == 0){
		     fill(seqColor);
		     rect((tBoxSize*3 + mposX)+(((patNum%8))*(unit+3)),(tBoxSize/3)*7*(floor(map(patNum,0 , 128, 0, 16))),(tBoxSize/2)*3,tBoxSize*2);
		   // println(map(patNum,0 , 128, 0, 16));
		   // for(int i = 0; i>seqLength; i++){ tBoxSize*3+mposX+x*(unit+3)
		      
		   // }
		   
		   } 
		  }
		}
		public void instrSelect(){
		 // println(+instrSel);
		  
		    
		  if(instrSel>0 && instrSel<9 && dialogOpen==false){
		      
		     boxHeight = 150;
		     
		     rES.setVisible(true);

		     if(envSel==1){
		      VelGraph.display();
		    }
		     if(envSel==2){
    	 PitGraph.display();  
		     }
		     if(envSel==3){
	      StaGraph.display();  
		     }
		     if(envSel==4){
		      LenGraph.display();  
		     }
		      if(envSel==5){
		      VerbGraph.display();  
		     }
		     if(envSel==6){
		      DelGraph.display();  
		     }
		     if(envSel==7){
		      PanGraph.display();  
		     }

		  }
		    if(dialogOpen==true){
		      rES.setVisible(false);
		    }
		    
		  if(instrSel==1){

		 ovKick.display();
		 
		    
		  } 
		     else {
		    
		  }
		  if(instrSel==2) {
		  ovSnr.display();
		  } else {
		     
		  }
		   if(instrSel==3) {
		   ovHht.display();
		   
		  } else {
		    
		  }
		  if(instrSel==4) {
		    ovOhht.display();
		  } else {
		     
		  }
		   if(instrSel==5){
		     ovShkr.display();
		  } 
		     else {
		     
		  }
		   if(instrSel==6) {
		    ovCb.display();
		  } else {
		     
		  }
		   if(instrSel==7) {
		    ovClap.display();
		  } else {
		     
		  }
		  if(instrSel==8) {
		      ovRide.display();
		     
		  } else {
		     
		  }
		 
		   ov1.display();
		   ov2.display();
		  if(instrSel==9) {
		   
		    
		    ov1.display();
		     ov2.display();
		    
		    cp5.getGroup("g2").setVisible(false);
		    cp5.getGroup("g4").setVisible(false);
		    cp5.getGroup("aboutMe").setVisible(false);

		    rES.setVisible(false);
		    cp5.getGroup("g5").setVisible(true);
		   
		    //rWav.setVisible(true);
		    for(int i = 0; i < seqLength; i++){
		      if(i<seqLength){
		   // measure[i].setVisible(true);
		      sngbtn[i].setVis(true,i);
		      }
		  }
		  for(int i = songLength-1; i>=seqLength; i--){
		        
		   //     measure[i].setVisible(false);
		          sngbtn[i].setVis(false,i);
		      }
		  } else {
		    cp5.getGroup("g5").setVisible(false);
		    cp5.getGroup("g2").setVisible(true);
		    cp5.getGroup("g4").setVisible(true);
		    cp5.getGroup("aboutMe").setVisible(true);
		    
		     for(int i = 0; i < songLength; i++){
		   // measure[i].setVisible(false);
		      sngbtn[i].setVis(false,i);
		      }
		    
		  }
		  if(dialogOpen==false && instrSel==10||instrSel==-1) {
		    boxHeight = tBoxSize * 8;
		    
		    rES.setVisible(false);
		 
		  
		    
		     m1.matrixDisplay();
		     
		     int mx = tBoxSize;
		   for(int i = 0;i<16; i++){
		  
		  //println("making boxes");
		  
		  if(i%4==0){
		    fill(255,80);
		  }
		  
		  else if((i+2)%4==0){
		    fill(190,80);
		  }
		  else{
		    fill(140,80);
		  }
		  rect((mposX+1)+(mx*i)-1, mposY , mx, boxHeight);
		 
		  
		}
		    
		
		     ov1.display();
		     ov2.display();
		
		  /*
		    ovKick.display();
		    ovSnr.display();
		    ovHht.display();
		    ovOhht.display();
		    ovShkr.display();
		    ovCb.display();
		    ovClap.display();
		    ovRide.display();
		     */   
		 
		 /*  

		  
		    */
		    
		  } else {
		  
		  }
		  
		  
		}


		// the radiobutton function for instrmuent select

		 
		public void EnvSel(int a){
		 
		 envSel = a;
		  
		 }


		class Linegraph{
		 
		float[] values;
		float plotX1, plotX2, plotY1, plotY2;
		int aSize = 16;
		String a;
		float locX = 0;
		float locY = 0;
		float plotHeight;
		float plotWidth;
		float circleSize;
		float maxVal = 200;
		float minVal = 0;
		PFont helvetica;

		Linegraph(float x, float y, float PW, float PH,  float minV, float maxV, String aName){
		  locX = x;
		  locY = y;
		  plotHeight = PH;
		  a = aName;
		  plotWidth = PW;
		  circleSize = PW/16;
		  minVal = minV;
		  maxVal = maxV;
		  
		  
		}

		public void init() {
		  

		 // PdBase.subscribe("paralist");  // Uncomment if you want to receive messages sent to the receive symbol "foo" in Pd
		 
		 // smooth();
		  //aSize = pd.arraySize(a);
		  //println(aSize);
		  values = new float[aSize];
		  //pd.readArray(values,0,a,0,aSize);
		 // helvetica = createFont("Helvetica-Bold", 14);
		 // textFont(helvetica);
		 // values[stepNumber] = Vel;  
		  
		  // set plot size
		  plotX1 = locX;
		  plotX2 = locX + plotWidth;
		  plotY1 = locY;
		  plotY2 = locY + plotHeight;
		}

		public void display() {
		  
		  
		  // draw plot bg
		  /*
		  fill(140);
		  noStroke();
		  rectMode(CORNERS);
		  rect(plotX1, plotY1, plotX2, plotY2);
		  */
		  
		   int mx = tBoxSize;
		   for(int i = 0;i<16; i++){
		  
		  //println("making boxes");
		  
		  if(i%4==0){
		    fill(255,80);
		  }
		  
		  else if((i+2)%4==0){
		    fill(190,80);
		  }
		  else{
		    fill(140,80);
		  }
		  rect((mposX+1)+(mx*i)-1, mposY , mx, tBoxSize*6);
		 
		  
		}
		 
		  noFill();
		  stroke(255);
		  strokeWeight(2);  
		  beginShape();
		  
		  float x, y;
		 
		  
		  for (int i = 0; i < values.length; i++) {
		    x = map(i, 0, values.length-1, plotX1+(circleSize/2), plotX2-(circleSize/2));
		    y = map(values[i], minVal, maxVal, plotHeight + locY-(circleSize/2), locY+(circleSize/2));
		    vertex(x, y);
		    
		   
		    
		  }
		  
		  endShape();
		  
		  // draw points on mouse over
		  for (int i = 0; i < values.length; i++) {
		    x = map(i, 0, values.length-1,plotX1+(circleSize/2), plotX2-(circleSize/2));
		    y = map(values[i], minVal, maxVal,  plotHeight + locY-(circleSize/2), locY+(circleSize/2));
		       
		    // check mouse pos
		    // float delta = dist(mouseX, mouseY, x, y);
		     
		      // conditons for animation of beat
		      //drawing the circles
		      
		        stroke(255);
		        fill(175);
		        ellipse(x, y, circleSize, circleSize);
		      
		      
		      float mvalue = 0;
		      if(time<timer0 && 
		        i==gBeat 
		        && instrSel==1
		        ){
		         
		         fill(dColor1);
		         stroke(170,0,0);
		         ellipse(x, y, circleSize+10, circleSize+10);  
		      }
		     
		      if(time<timer1 && i==gBeat && instrSel==2){
		        
		          stroke(mvalue+100,0,0);
		          fill(dColor2);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		      if(time<timer2 && i==gBeat && instrSel==3){
		        
		         stroke(mvalue+100,0,0);
		         fill(dColor3);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		      if(time<timer3 && i==gBeat && instrSel==4){
		        
		         stroke(mvalue+100,0,0);
		         fill(dColor4);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		       if(time<timer4 && i==gBeat && instrSel==5){
		       
		         stroke(mvalue+100,0,0);
		         fill(dColor5);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		      if(time<timer5 && i==gBeat && instrSel==6){
		        
		         stroke(mvalue+100,0,0);
		         fill(dColor6);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		      if(time<timer6 && i==gBeat && instrSel==7){
		        
		         stroke(mvalue+100,0,0);
		         fill(dColor7);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		      if(time<timer7 && i==gBeat && instrSel==8){
		       // float mvalue = 0;
		         stroke(mvalue+100,0,0);
		         fill(dColor8);
		         ellipse(x, y, circleSize+10, circleSize+10);
		      }
		    /*
		      else{
		        stroke(255);
		        fill(0);
		        ellipse(x, y, circleSize, circleSize);
		      }
		     */
		     
		  
		   
		   
		    float delta = abs(mouseX - x);
		    if ((delta < circleSize/2) && mouseY > plotY1 && mouseY < plotY2 && dialogOpen==false) {
		     
		      
		      int labelVal = round(values[i]);
		      Label label = new Label("" + labelVal, x, y);
		      if(mousePressed==true && mouseY>locY && mouseY<plotHeight+locY){
		       mvalue = map(mouseY, plotHeight + locY, locY, minVal, maxVal);
		       values[i] = mvalue; 
		     
		      // sending values to PD
		      
		       PdBase.sendFloat("s"+a, i);
		       PdBase.sendFloat(a, round(mvalue));
		       
		       // saving changes to the pattern array
		       if(a.equals("Vel")){
		       patterns[PApplet.parseInt(sbar)].vel[i+(instrSel-1)*16] = round(mvalue);
		       }
		       if(a.equals("Pit")){
			       patterns[PApplet.parseInt(sbar)].pit[i+(instrSel-1)*16] = round(mvalue);
			       }
		       if(a.equals("Sta")){
			       patterns[PApplet.parseInt(sbar)].start[i+(instrSel-1)*16] = round(mvalue);
			       }
		       if(a.equals("Len")){
			       patterns[PApplet.parseInt(sbar)].slength[i+(instrSel-1)*16] = round(mvalue);
			       }
			       if(a.equals("Verb")){
				       patterns[PApplet.parseInt(sbar)].verb[i+(instrSel-1)*16] = round(mvalue);
				       }
			       if(a.equals("Del")){
				       patterns[PApplet.parseInt(sbar)].delay[i+(instrSel-1)*16] = round(mvalue);
				       }
			       if(a.equals("Pan")){
				       patterns[PApplet.parseInt(sbar)].pan[i+(instrSel-1)*16] = round(mvalue);
				       }
		      }
		      
		      
		      }
		    

		    }
		  
		  }
		  
		 public void sValues(int s, float y){
		  
		   values[s] = y;
		  
		 } 


		class Label {
		  
		  Label(String txt, float x, float y) {
		    
		    // get text width
		    float labelW = textWidth(txt);
		    
		    // check if label would go beyond screen dims
		    if (x + labelW + 20 > width) {
		      x -= labelW + 20;
		    }
		    
		    // draw bg
		    fill(100);
		    noStroke();
		    //ellipseMode(CENTER); // note: this is the default mode. confusing b/c similar to CORNERS (plural)
		    //ellipse(x, y, labelW+10, labelW+10); 
		    
		    // draw text
		    fill(255,0,0);
		    textSize(circleSize*.75f);
		    text(txt, x-circleSize/3, y);
		  }
		}

		/* unused recieve for PD
		void receiveList(String source, Object... args){

		   if(source.equals("paralist")){
		  //parsing stepNum
		  float stepNum = Float.parseFloat(args[0].toString());
		  int s = (int)stepNum;
		   
		  //parsing parameters   
		  float Vel = Float.parseFloat(args[1].toString());
		  float Pit = Float.parseFloat(args[2].toString());
		  float Sta = Float.parseFloat(args[3].toString());
		  float Len = Float.parseFloat(args[4].toString());
		 
		   //parsing instrument number
		  float instrNum = Float.parseFloat(args[5].toString()); 
		  
		  int i = (int)instrNum;
		  
		  println("parameters in Processing " +Vel);
		  
		  //values[s] = Vel;
		  
		     }

		  }
		  */

		}
		 





		class Matrix{
		 
		 int cols, rows;
		 float locX,locY,boxSize;
		 boolean isInside = false;
		 M_button[][] matrix; 
		 
		Matrix(int x, int y, float posX, float posY, float bS){
		 
		  
		 cols = x;
		 rows = y;
		 locX = posX;
		 locY = posY;
		 boxSize = bS;
		 matrix = new M_button[cols][rows];
		 
		} 
		  
		public void init(){
		    // A loop to evenly space out the buttons along the window
		  for(int x = 0; x < cols; x++) {
		    for(int y = 0; y < rows; y++) {
		    matrix[x][y] = new M_button(locX+(x*(boxSize)),locY+(y*(boxSize)),boxSize,boxSize); 
		   
		    }
		   }
		  }

		public void matrixDisplay(){
		   for (int x = 0; x < cols; x++) {
		     for (int y = 0; y < rows; y++){
		     matrix[x][y].display(x,y); 
		    
		     }
		    }
		  
		  }
		  
		 // call the internal clear function 
		 public void cCells(){
		   for (int x = 0; x < cols; x++) {
		      for (int y = 0; y < rows; y++) {
		   matrix[x][y].clearCells(x,y);
		      }
		   }
		  }
		// set the cells on or off

		 public void sCells(int x, int y, boolean noteOn){
		   matrix[x][y].setCells(noteOn);
		  
		 } 
		public void mPressed(){
		   for (int x = 0; x < cols; x++) {
		     for (int y = 0; y < rows; y++){
		    matrix[x][y].click(mouseX,mouseY, x,y); 
		     }
		   } 
		}

		public void mDragged(){
		  for (int x = 0; x < cols; x++) {
		    for (int y = 0; y < rows; y++){ 
		    matrix[x][y].drag(mouseX,mouseY, x,y); 
		     }
		    } 
		}
		public void mReleased(){
		  isInside = false;
		  int i = 0;
		  for (int y = 0; y < rows; y++){ 
			  for (int x = 0; x < cols; x++) {
			  
			    patterns[PApplet.parseInt(sbar)].seq[i++]  = parseInt(matrix[x][y].on); 
			     }
			    } 
		  
		}
		class M_button  {    
		  boolean Once = false;
		  
		  
		  // Button location and size
		  float x;   
		  float y;   
		  float w;   
		  float h;   
		  // Is the button on or off?
		  boolean on;  

		  // Constructor initializes all variables
		  M_button(float tempX, float tempY, float tempW, float tempH)  {    
		    x  = tempX;   
		    y  = tempY;   
		    w  = tempW;   
		    h  = tempH;   
		    on = false;  // Button always starts as off
		  }    
		  
		  public void click(int mx, int my, int ix, int iy) {
		    // Check to see if a point is inside the rectangle
		    if (mx > x && mx < x + w && my > y && my < y + h) {
		      on = !on;
		      //println("button number " +ix +" " +iy +" " +on);
		      //send floats to pd
		      PdBase.sendFloat("pd_y", (float)iy);
		      PdBase.sendFloat("pd_x", (float)ix);
		      
		      isInside = true;
		      Once = true;
		      //println(isInside);
		    }   
		  }
		  
		  
		  public void drag(int mx, int my, int ix, int iy ) {
		    // Check to see if a point is inside the rectangle
		    if (mx > x && mx < x + w && my > y && my < y + h 
		     
		        && Once == false 
		        && isInside == true) {
		        on = !on;
		          Once = true;
		     //println("button number " +ix +" " +iy +" " +on);
		     //println("old i " +oldi +"new i " +i); 
		      PdBase.sendFloat("pd_y", (float)iy); 
		      PdBase.sendFloat("pd_x", (float)ix);
		    
		         } 
		    // check to see if the point is outside the rectangle and reset Once
		    if(mx < x || mx > x + w || my < y || my > y + h){
		           Once = false;
		           //println(Once);
		         }  
		  }
		  
		 
		  

		  // Draw the rectangle
		  public void display(int bmX, int bmY) {
		    rectMode(CORNER);
		    
		    stroke(stroke0);
		    // The color changes based on the state of the button
		    if (on) {
		      float beat = map(x,  mposX, mposX + tBoxSize*16, 0, 16);
		      if(bmY==0){
		      fill(dColor1);
		      }
		      if(bmY==1){
		      fill(dColor2);
		      }
		      if(bmY==2){
		      fill(dColor3);
		      }
		      if(bmY==3){
		      fill(dColor4);
		      }
		       if(bmY==4){
		      fill(dColor5);
		      }
		      if(bmY==5){
		      fill(dColor6);
		      }
		      if(bmY==6){
		      fill(dColor7);
		      }
		      if(bmY==7){
		      fill(dColor8);
		      }
		     // println(+round(beat) +" gBeat " +gBeat);
		     
		      if(gBeat == floor(beat) 
		       && isPlaying == true
		      )
		      {
		        fill(dColor0);
		      //println(+round(beat) +" gBeat " +gBeat);
		      }
		    } else {
		      fill(255,150);
		    }
		    rect(x,y,w,h);
		   
		  }
		  
		  public void clearCells(int x, int y){
		   on = false; 
		  }
		  
		  public void setCells(boolean setOn){
		   on = setOn; 
		  } 
		  
		 }

		}



		class Oscview {
		  
		float[] dispvalues;
		float[] values;
		float plotX1, plotX2, plotY1, plotY2;
		int aSize;
		String b = "view";
		int locX;
		int locY;
		int plotHeight=tBoxSize;
		int plotWidth=400;
		int circleSize = 1;


		PFont helvetica;
		int w;
		int sp;
		float splace;
		int anewSize = plotWidth;
		int lineColor = color(255);
		 
		 Oscview(int x,int y, int PH, int tlineColor, String tempArrayName){
		   locX = x;
		   locY = y;
		   plotHeight = PH;
		   plotWidth = round(plotHeight*1);//just changed...
		   anewSize = plotWidth;
		   b = tempArrayName;
		   lineColor = tlineColor;
		   
		 }
		 
		public void init() {
		  
		  //PdBase.subscribe("splace");  // Uncomment if you want to receive messages sent to the receive symbol "foo" in Pd.
		 // smooth();
		  aSize = PdBase.arraySize(b);
		 // println("array size "+anewSize);
		  dispvalues = new float[aSize];
		  values = new float[anewSize];
		  
		  
		//  PdBase.readArray(dispvalues,0,b,0,aSize);
		 
		  plotX1 = locX;
		  plotX2 = locX + plotWidth;
		  plotY1 = locY;
		  plotY2 = locY + plotHeight;
		  
		//  println(values.length);
		   
		}

		public void display() {
		 
		  PdBase.readArray(dispvalues,0,b,0,aSize);

		  // draw plot bg
		  fill(155);
		  noStroke();
		  rectMode(CORNERS);
		  //rect(plotX1, plotY1, plotX2, plotY2);



		  
		  float x, y;
		 // float x1, y1;
		  beginShape();
		  noFill();
		  stroke(lineColor);
		  strokeWeight(2);
		  for (int i = 0; i < values.length; i++) {
		    values[i] = dispvalues[i*(aSize/plotWidth)];
		    x = map(i, 0, values.length-1, plotX1+(circleSize/2), plotX2-(circleSize/2));
		    y = (map(values[i], -1, 1,locY, locY+plotHeight));
		    vertex(x, y);
		    
		    }
		    endShape();
		    
		      beginShape();
		  noFill();
		  stroke(175);
		  strokeWeight(2);
		  for (int i = 0; i < values.length; i++) {
		    values[i] = dispvalues[i*(aSize/plotWidth)];
		    x = map(i, 0, values.length-1, plotX1+(circleSize/2), plotX2-(circleSize/2));
		    y = (map(values[i], -1, 1,locY, locY+plotHeight));
		    vertex(x, y+2);
		    }
		    endShape();
		 
		    

		   
		  
		  }


		}

		class Pattern {
		 
		 int patternNumber;
		 
		 String pName; //pattern name
		  
		  // drum sample names/paths
		 
		 String[] drmName;// = new String[8];
		 String[] drmPathName; 

		 
		  //Slider values
		 
		 float[] drmvol;// = new float[8];

		// slider values in an array
		 float[] sliderval;// = new float[7]; //1 bpm, 2 groove, 3 dlyFB,4 dlyStep,5 rvbLive, 6 rvbXver, 7 rvbDamp
		// float bpm, groove, 
		// dlyFB, dlyStep, rvbLive, rvbXver, rvbDamp;
		 
		  // sequence arrays
		  
		  int numofstep = 128;
		  float[] seq;// = new float[numofstep];
		  float[] vel;// = new float[numofstep];
		  float[] pit;// = new float[numofstep];
		  float[] start;//t = new float[numofstep];
		  float[] slength;// = new float[numofstep];
		  float[] verb;// = new float[numofstep];
		  float[] delay;// = new float[numofstep];
		  float[] pan;// = new float[numofstep];
		  
		 
		  
		 Pattern(String pNameTemp, String[] drmPathNameTemp, String[] drmNameTemp,float[] drmvolTemp,float[] slidervalTemp,float[] seqTemp,
		         float[] velTemp,float[] pitTemp,float[] startTemp,float[] slengthTemp,float[] verbTemp,float[] delayTemp,
		         float[] panTemp, int patternNumberTemp) {
		          
		           
		           
		           patternNumber=patternNumberTemp;
		           pName = pNameTemp;
		           drmPathName = drmPathNameTemp;
		           drmName = drmNameTemp;
		           drmvol = drmvolTemp;
		           sliderval=slidervalTemp;
		           seq = seqTemp;
		           vel=velTemp;
		           pit=pitTemp;
		           start=startTemp;
		           slength=slengthTemp;
		           verb=verbTemp;
		           delay=delayTemp;
		           pan=panTemp;
		// println(" pattern num "+patternNumber);
		// println(pName);
		// println(drmName);
		 
		   }
		   
		   
		  
		  public void loadPattern() {
			  // very time sensitive load the patttern at the right time
			  
			    PdBase.writeArray("seq",0,seq,0,128);
			    PdBase.writeArray("vel",0,vel,0,128);
			    PdBase.writeArray("pit",0,pit,0,128);
			    PdBase.writeArray("sstart",0,start,0,128);
			    PdBase.writeArray("length",0,slength,0,128);
			    PdBase.writeArray("verb",0,verb,0,128);
			    PdBase.writeArray("delay",0,delay,0,128);
			    PdBase.writeArray("pan",0,pan,0,128);
			   
			       cp5.getController("bpm").setValue(sliderval[0]);
				   cp5.getController("shuffle").setValue(sliderval[1]);
				   cp5.getController("dFB").setValue(sliderval[2]);
				   cp5.getController("dStep").setValue(sliderval[3]);
				   cp5.getController("vDecay").setValue(sliderval[4]);
				   cp5.getController("vXover").setValue(sliderval[5]);
				   cp5.getController("vDamp").setValue(sliderval[6]);
				   
			    for(int s = 0; s< drmvol.length; s++){
				      drumvol[s].setValue(drmvol[s]);
				      
				    }
			    
			  textField.setText(pName);
		   // cp5.get(Textfield.class,"patName").setText(pName);
		    //0 bpm, 1 groove, 2 dlyFB,3 dlyStep,4 rvbLive, 5 rvbXver, 6 rvbDamp
		  
		  
		    
		  

		for(int i = 0; i<drmName.length;i++){
		  int s =i+1;
		 // drmName[i] =  names[i+1];
		  instrSelect[i].getCaptionLabel().set(drmName[i]);
		  //println("drum name "+drmName[i]);
		  PdBase.sendSymbol(""+s+"Open", drmPathName[i]);
		  
		 		} 

		  
		  }
	

		public void copyPattern() {
		//  println("copy");
			
			pName = textField.getText();
			      
		   PdBase.readArray(seq,0,"seq",0,128);
		    PdBase.readArray(vel,0,"vel",0,128);
		    PdBase.readArray( pit,0,"pit",0,128);
		    PdBase.readArray( start,0,"sstart",0,128);
		    PdBase.readArray( slength,0,"length",0,128);
		    PdBase.readArray( verb,0,"verb",0,128);
		    PdBase.readArray( delay,0,"delay",0,128);
		    PdBase.readArray( pan,0,"pan",0,128);
		    
		      //0 bpm, 1 groove, 2 dlyFB,3 dlyStep,4 rvbLive, 5 rvbXver, 6 rvbDamp
		 
		    sliderval[0] =  cp5.getController("bpm").getValue();
		    sliderval[1] =  cp5.getController("shuffle").getValue();
		    sliderval[2] = cp5.getController("dFB").getValue();
		    sliderval[3] = cp5.getController("dStep").getValue();
		    sliderval[4] = cp5.getController("vDecay").getValue();
		    sliderval[5] = cp5.getController("vXover").getValue();
		    sliderval[6] = cp5.getController("vDamp").getValue();
		      
		       for(int s = 0; s< drmvol.length; s++){

		        drmvol[s] =  drumvol[s].getValue();
		       }
		       
		        for(int s = 0; s< drmName.length; s++){
		          drmName[s] =  instrSelect[s].getLabel();
		        }
		        for(int s = 0; s< drmPathName.length; s++){
			          drmPathName[s] =  patterns[PApplet.parseInt(sbar)].drmPathName[s];
			        }
		    }
	
		}


	


		

		class Sngbtn {

		  int sBoxSize;

		  int mNum = 0;
		  int sngbtnNum;
		  int sbposX;
		  int sbposY;  
		  
		  Sngbtn(int tS, int sbN,int sbX,int sbY){
		    
		  sBoxSize = tS;
		  sngbtnNum = sbN;
		  sbposX = sbX;
		  sbposY =sbY;
		  
		  cp5.addNumberbox("measurebox"+sngbtnNum)
		     .setPosition(sbposX,sbposY+(tBoxSize-tBoxSize/8))
		     .setSize(tBoxSize,tBoxSize/2)
		     .setScrollSensitivity(1)
		     //.setValue(50)
		     .setLock(true)
		     .setRange(0,64)
		     .setCaptionLabel(""+sngbtnNum)
		     .setVisible(false)
		     .setDecimalPrecision(0)
		     ;
		  cp5.getController("measurebox"+sngbtnNum).getCaptionLabel().align(ControlP5.RIGHT_OUTSIDE, ControlP5.CENTER).setPaddingX(0); 

		  cp5.addBang("mplus"+sngbtnNum)
		       .setPosition(sbposX, sbposY)
		       .setSize(tBoxSize, tBoxSize-tBoxSize/8)
		       //.setLabelVisible(false)
		       .setCaptionLabel("+")
		       .setVisible(false)
		       ;
		       
		  cp5.getController("mplus"+sngbtnNum).getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 

		  cp5.addBang("mminus"+sngbtnNum)
		       .setPosition(sbposX, sbposY+((tBoxSize/2)*3)-tBoxSize/8)
		       .setSize(tBoxSize, tBoxSize-tBoxSize/8)
		       // .setLabelVisible(false)
		       .setCaptionLabel("-")
		       .setVisible(false)
		       ;
		  cp5.getController("mminus"+sngbtnNum).getCaptionLabel().align(ControlP5.CENTER, ControlP5.CENTER).setPaddingX(0); 

		    
		  }
		 public void setVis(boolean vis, int i){
		   int n = i+1;
		   if(vis==true){
		 //  for(int i = 0; i < seqLength; i++){
		     cp5.getController("measurebox"+n+"").setVisible(true);
		     cp5.getController("mplus"+n+"").setVisible(true);
		     cp5.getController("mminus"+n+"").setVisible(true);

		 //    }
		   }
		  
		   if(vis==false){
		     
		 //  for(int i = 0; i < seqLength; i++){
		     cp5.getController("measurebox"+n+"").setVisible(false);
		     cp5.getController("mplus"+n+"").setVisible(false);
		     cp5.getController("mminus"+n+"").setVisible(false);

		 //    }
		    }
		   }
		}
		
		//toast stuff
		private Toast toast = null;

		/**
		 * creating a handy toast notification message method here
		 * 
		 * @param msg
		 *            String MESSAGE_TO_BE_DISPLAYED_AS_TOAST
		 */
		private void toast(final String msg) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (toast == null) {
						toast = Toast.makeText(getApplicationContext(), "",
								Toast.LENGTH_SHORT);
					}
					toast.setText(msg);
					toast.show();
				}
			});
		}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_dm_beatbox);
		initPdService();
		// make sure the screen doesn't turn off
	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	   //get display size
	//set the size
		Display display = getWindowManager().getDefaultDisplay();
		widthScr = display.getWidth();
	//	println("width "+display.getWidth());
	
		initSystemServices();
				
	}
	
	private void initSystemServices() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (pdService == null)
					return;
				if (state == TelephonyManager.CALL_STATE_IDLE) {
					pdService.startAudio();
				} else {
					pdService.stopAudio();
				}
			}
		}, PhoneStateListener.LISTEN_CALL_STATE);
	}
	

	@Override
	public void onBackPressed() {
	    new AlertDialog.Builder(this)
	           .setMessage("Are you sure you want to exit?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	                    finish();
	               }
	           })
	           .setNegativeButton("No", null)
	           .show();
	}


	
	@Override
	public void onDestroy() {
		super.onDestroy();
		

		
		// release all resources called by pdservice
		dispatcher.release();
		if (pd != 0) {
			PdBase.closePatch((int) pd);
			pd = 0;
		}
		pdService.stopAudio();
		unbindService(pdConnection);
	}

	public class loadpatternALL extends AsyncTask<String, String, String>{

		  private String resp;

		  @Override
		  protected String doInBackground(String... params) {
		   publishProgress("loading..."); // Calls onProgressUpdate()
		   try {
		    // Do your long operations here and return the result
			   String[] data = loadStrings((params[0]));
			  //   println("number of patterns "+data.length);
			     cp5.get(Textfield.class,"songTitle").setText(params[0]);
			     
			     patterns = new Pattern[data.length-1];
			     
	   for (int i = 0; i < patterns.length; i ++ ) {
			 String pName; //pattern name
			  
			  // drum sample names/paths
			 
			 String[] drmName = new String[8];
			 String[] drmPathName = new String[8];

			 
			  //Slider values
			 
			 float[] drmvol = new float[8];

			// slider values in an array
			 float[] sliderval = new float[7]; //1 bpm, 2 groove, 3 dlyFB,4 dlyStep,5 rvbLive, 6 rvbXver, 7 rvbDamp
			// float bpm, groove, 
			// dlyFB, dlyStep, rvbLive, rvbXver, rvbDamp;
			 
			  // sequence arrays
			  int numofstep = 128;
			  float[] seq = new float[numofstep];
			  float[] vel = new float[numofstep];
			  float[] pit = new float[numofstep];
			  float[] start = new float[numofstep];
			  float[] slength = new float[numofstep];
			  float[] verb = new float[numofstep];
			  float[] delay = new float[numofstep];
			  float[] pan = new float[numofstep];
			     
			   //  Each line is split into an array of floating point numbers.
			   float[] values = PApplet.parseFloat(split(data[i], "," )); 
			   String[] names = split(data[i], ',' );
			   
			    pName = names[0];
			    
			   

			    for(int s = 0; s<drmPathName.length;s++){
					  
					   drmPathName[s] =  names[s+1];
					 
					  //println("drum path name "+drmPathName[s]);

					  
					}
			    
			    for(int s = 0; s<drmName.length;s++){
					  
					   drmName[s] =  names[s+9];
					 
					 // println("drum name "+drmName[s]);

					  
					} 
			 for(int s = 0; s< drmvol.length; s++){
			      drmvol[s]=values[s+17];
			      
			   }
			   
			 for(int s = 0; s< sliderval.length; s++){
			     sliderval[s] = values[s+25];
			   }
			    
			 int stepOffset = 32;
			  for (int s = 0; s < seq.length; s ++ ) {
			   seq[s] = values[s+stepOffset]; 
			   vel[s] = values[s+(numofstep+stepOffset)];
			   pit[s] = values[s+(numofstep*2+stepOffset)];
			   start[s] = values[s+(numofstep*3+stepOffset)];
			   slength[s] = values[s+(numofstep*4+stepOffset)];
			   verb[s] = values[s+(numofstep*5+stepOffset)];
			   delay[s] = values[s+(numofstep*6+stepOffset)];
			   pan[s] = values[s+(numofstep*7+stepOffset)];
			 

			    // println("step "+i);
			    // println("seq "+seq[i]); 
			    // println("vel "+vel[i]); 
			  }  
			    //println("making pattern"+i);
			     patterns[i] = new Pattern(pName, drmPathName, drmName, drmvol,sliderval,seq,vel,pit,start,slength,verb,delay,pan,i); 
			
			   }
			   
	   float[] sbvalues = PApplet.parseFloat(split(data[64], "," )); 
	  for(int i = 0; i<sbvalues.length; i++){
	  // println("mNum "+PApplet.parseInt(sbvalues[i]));
	   sngbtn[i].mNum = PApplet.parseInt(sbvalues[i]);
	   int s = i+1;
	   
	   cp5.getController("measurebox"+s).setValue(PApplet.parseInt(sbvalues[i]));
	   
	  }
		   } catch (Exception e) {
		    e.printStackTrace();
		    resp = e.getMessage();
		   }
		   return resp;
		  }
	
		
		  /*
		   * (non-Javadoc)
		   * 
		   * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		   */
		  @Override
		  protected void onPostExecute(String result) {
		   // execution of result of Long time consuming operation
		//   finalResult.setText(result);
			  toast(getString(R.string.loaded));
			  patterns[PApplet.parseInt(sbar)].loadPattern();
			  matrixDraw();
		  }

		  /*
		   * (non-Javadoc)
		   * 
		   * @see android.os.AsyncTask#onPreExecute()
		   */
		  @Override
		  protected void onPreExecute() {
		   // Things to be done before execution of long running operation. For
		   // example showing ProgessDialog
		  }

		  /*
		   * (non-Javadoc)
		   * 
		   * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		   */
		  @Override
		  protected void onProgressUpdate(String... text) {
		 //  finalResult.setText(text[0]);
		   // Things to be done while execution of long running operation is in
		   // progress. For example updating ProgessDialog
			  toast(getString(R.string.loading));

		  }
		 
		
	}//end  of class loadpatternALL
	
	
//about me	
	class TestCanvas extends Canvas {
		  
		  float n;
		  float a;
		  
		  public void setup(PApplet p) {
			  
		  //  println("starting a test canvas.");
		   
		  }
		  public void draw(PApplet p) {
			p.fill(175,150);
		    p.rect(-tBoxSize*14,0,tBoxSize*16,tBoxSize*8);
		   
		   
		  }
		}
	

	public void onClickWidget(APWidget widget) {  
		//println("name "+textField.getText());
		  
		  if(widget == textField){ 
			 // println("name "+textField.getText());
		    patterns[PApplet.parseInt(sbar)].pName = textField.getText();
		  }
	}


	
}
