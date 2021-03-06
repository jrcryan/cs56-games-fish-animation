package edu.ucsb.cs56.projects.games.fish_animation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.*;
import java.io.*;
import java.net.*;

/**
   Creates a JFrame that animates Fish and allows for a shark
   to eat the fish. Tracks the amount of fish eaten with an
   elapsed amount of time.

   @author Lawrence Khuu
   @author Casey Barbello
   @author Daryl Pham
   @author Christina Morris
   @author Mathew Glodack
   @author Jenna Cryan
   @author Josephine Vo
   @version for CS56, Winter 2014, UCSB
*/

public class FishAnimationEnvironment extends JFrame implements Serializable {
    Thread animate;
    DrawingPanel fishPanel = new DrawingPanel();
    JFrame animation = new JFrame();
    
    int maxX = 1366, maxY = 768; // Default height and width of the game at start
    int posX = maxX/2, posY = maxY/2;  //used to position the shark at the origin
    int maxWidth = 100; //max width of the fish
    int boatX = maxX;//hold the position of the boat
    int maxD = 10; //holds the maximum diameter of the bubbles
    
    int eaten = 0; //number of fish eaten
    int numFish = 75; //number of fish in environment
    int numBubbles = 10+(int)(Math.random()*20); //creates a random amount of bubbles
    int numJellyFish; //holds the number of Jellyfish to be created
    int Highscore;
    
    int timer, timerload = 0;
    long time1 = System.nanoTime()/1000000000; //Used to get the start time of the game
    long time2, pausestart, pausetime = 0; // time that the pause button was pressed for
    int delay = 20; // The pause button delay
    boolean stop = false; // used to know when to pause the animation and when not to
    boolean load = false; // used to determine whether or not the game loads from the serialized form
	
    //create ArrayLists for fish, bubbles, and jellyfish.
    ArrayList<Fish> fishArray = new ArrayList<Fish>();    
    ArrayList<Bubbles> bubblesArray = new ArrayList<Bubbles>();
    ArrayList<JellyFish> jellyfish = new ArrayList<JellyFish>();

    /** 
	Method addNewBubbles adds Bubbles to the ArrayList
	@param bubble a Bubbles object
    */
    private void addNewBubbles(Bubbles bubble) {
	bubblesArray.add(bubble);
    }
  
    /** 
	Method addNewFish add Fish to the ArrayList
	@param fish a Fish object
    */
    private void addNewFish(Fish fish) {
	fishArray.add(fish);
    }
    
    private Fish createRandomFish(int xBound, int yBound, int maxWidth) {
	int randomX = (int) (Math.random() * xBound);
	int randomY = (int) (Math.random() * yBound);
	int randomWidth = 10 + (int) (Math.random() * (maxWidth-10));
	return new Fish(randomX, randomY, randomWidth, randomWidth / 5);
    }
    
    /** 
	Method createsBubbles
	@param x for the x position 
	@param y for the y position
	@param diameter for the diameter of the bubble
    */
    private Bubbles createBubbles( int x, int y, int diameter) {
	int randomX = (int) (Math.random() * x);
	int randomY = (int) (Math.random() * y);
	int randomD = (int) (Math.random() * diameter);
	return new Bubbles(randomX, randomY, randomD);
    }
	
    /**
       Constructor for FishAnimationEnvironment. Creates a JFrame and a Jpanel that 
       is placed inside of it. Animation is done on the JPanel
    */
    public FishAnimationEnvironment() {
	for(int i = 0; i < numFish; i++){
	    addNewFish(createRandomFish(maxX, maxY, maxWidth));
	}
    }
    
    /**
       Custom Constructor for FishAnimationEnvironment. Creates a JFrame and a Jpanel that 
       is placed inside of it. Animation is done on the JPanel.  This Constructor takes
       in input from the Menu GUI for the difficulty and creates Jellyfish based on the
       selected difficulty.
    */
    public FishAnimationEnvironment(int difficulty, boolean l) {
	numJellyFish = difficulty;
	load = l;
	if(load){
	    //deserialize the score, fish, shark, boat, and jellyfish
	    try {
		FileInputStream fileStream = new FileInputStream("saved.ser");
		ObjectInputStream os = new ObjectInputStream(fileStream);
		eaten = os.readInt();
		numJellyFish = os.readInt();
		timerload = os.readInt();
		posX = os.readInt();
		posY = os.readInt();
		boatX = os.readInt();

		for(int k = 0; k < numFish; k++) {
		    Fish f = new Fish(os.readDouble(), os.readDouble(), os.readDouble(), os.readDouble());
		    fishArray.add(f);
		}
		for(int i = 0; i < numJellyFish; i++) {
		    int x = os.readInt();
		    int y = os.readInt();
		    double speed = os.readDouble();
		    JellyFish j = new JellyFish(x, y, speed);
		    jellyfish.add(j);
		}
		os.close();
	    } catch(Exception ex) { 
		ex.printStackTrace();
		load = false;
		numJellyFish = 3;
	    }
	}
	
	//Adds the fish into the Array only if the load button was not selected
	if(load == false) {
	    for(int i = 0; i < numFish; i++){
		addNewFish(createRandomFish(maxX, maxY, maxWidth));
	    }
	}
	
	//Adds the random amount of Bubbles into the ArrayList
	for(int i = 0; i < numBubbles; i++) {
	    addNewBubbles(createBubbles(maxX, maxY, maxD));
	}

	//Adds the Jellyfish into the game 
	if(load == false) {
	    for(int i = 0; i < numJellyFish; i++) {
		JellyFish j = new JellyFish((int)(Math.random() * 12345) % maxX, maxY, (Math.random() * 123) % 50 + 15);
		jellyfish.add(j);
	    }
	}

	animation.getContentPane().add(BorderLayout.CENTER, fishPanel);
	animate = new Animate();
	animate.start();
	animation.setDefaultCloseOperation(EXIT_ON_CLOSE);    
	animation.setSize(maxX, maxY);
	animation.setVisible(true);
	GameMenu game = new GameMenu();
	game.makemenu();
    }//end constructor
    
    /**
       Innerclass for a custom Jpanel for animation.  Draws out the 
       background and the in-game components including a timer, 
       the score, the shark, the fish, the jellyfish, the seaweed, 
       the boat, and the bubbles.     
    */

    class DrawingPanel extends JPanel {
    
	public void paintComponent(Graphics g){ 
	    
	    //Sets background color and adds background image
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setColor(Color.BLUE);
	    g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	    URL reefURL = getClass().getResource("/resources/CoralReef.jpg");
	    Image reef = new ImageIcon(reefURL).getImage();	   
	    super.paintComponent(g); //replace current painting
	    g.drawImage(reef, 0, 0, this); 
	    
	    //Starts the Action listener to listen for mouse events in the panel
	    MouseHandler handler = new MouseHandler();
	    fishPanel.addMouseListener(handler);
	    fishPanel.addMouseMotionListener(handler);
	    
	    //Images in the game
	    URL sharkURL = getClass().getResource("/resources/shark.jpg");
	    Image shark = new ImageIcon(sharkURL).getImage();
	    URL seaweedURL = getClass().getResource("/resources/Seaweed.jpg");
	    Image seaweed = new ImageIcon(seaweedURL).getImage();
	    URL boatURL = getClass().getResource("/resources/cartoon-boat.jpg");
	    Image boat = new ImageIcon(boatURL).getImage();
	    
	    
	    //Draws the seaweed at the specified points
	    for (int i = 0; i < this.getWidth() + 125; i += 125) {
		g.drawImage(seaweed, i, this.getHeight() - 83, this);
	    }
	    
	    //Draws the image of the boat and also animates it
	    g.drawImage(boat, boatX, -135, this);
	    if(!stop) {
		boatX -= 10;
		if(boatX <= -250) {
		    boatX = this.getWidth();
		}
	    }
	    
	    //Draws the fish based off the fish info array
	    g2.setColor(Color.YELLOW);
	    for(int i = 0; i < fishArray.size(); i++) {
		g2.draw(fishArray.get(i));
	    }
	    
	    //Draws the image of the Shark
	    Shark s = new Shark(posX, posY);
	    int newXPos = (int) s.getXPos() - 160;
	    int newYPos = (int) s.getYPos() - 130;
	    g2.drawImage(shark, newXPos, newYPos, this);
	    
	    //Draws the bubbles with the blue gradient
	    Color b = new Color(127, 255, 212);
	    for(int i = 0; i < bubblesArray.size(); i++) {
	    	double yD = bubblesArray.get(i).getYPos();
	    	int yI = (int) yD;
	    	double xD = bubblesArray.get(i).getXPos();
	    	int xI = (int) xD;
		int BubblesY = yI - bubblesArray.get(i).getDiameter() / 2;
		int BubblesY2 = yI + bubblesArray.get(i).getDiameter() / 2;
		int BubblesX = xI - bubblesArray.get(i).getDiameter() / 2;
		int BubblesX2 = xI + bubblesArray.get(i).getDiameter() / 2;

		//Creates a gradient color for the Bubble
		GradientPaint gradient = new GradientPaint(BubblesX, BubblesY, Color.BLUE, BubblesX2, BubblesY2, b);
		g2.setPaint(gradient);

		//Draws a Bubbles object onto the screen
		int xb = (int) bubblesArray.get(i).getXPos(); 
		int yb = (int) bubblesArray.get(i).getYPos();
		int db = (int) bubblesArray.get(i).getDiameter();
		g2.fillOval(xb, yb, db, db);
	    }
	    
	    //Sets the Color to PINK for the jellyfish
	    g2.setColor(Color.PINK);
	    for(int i = 0; i < jellyfish.size(); i++) {
	    	int jNewXPos = (int) jellyfish.get(i).getXPos();
		int jNewYPos = (int) jellyfish.get(i).getYPos();
		if(jellyfish.get(i).CheckJellyFish() == true) {
		    //Draws the Body of the JellyFish
		    g2.fillArc(jNewXPos, jNewYPos, 50, 40, 0, 180);

		    //Draws the Tentacles of the JellyFish
     		    for(int j = (int) jellyfish.get(i).getXPos()+5; j < jellyfish.get(i).getXPos() + 50; j += 5){
			g2.drawLine(j, jNewYPos + 75, j, jNewYPos + 10);
		    }
		}
		else {
		    //Draws the Body of the JellyFish
		    g2.fillArc(jNewXPos + 5, jNewYPos, 40, 50, 0, 180); 
		    
		    //Draws the Tentacles of the JellyFish
		    for(int j = (int) jellyfish.get(i).getXPos() + 10; j < jellyfish.get(i).getXPos() + 45; j += 5){
			g2.drawLine(j,jNewYPos+95,j,jNewYPos+10);
		    }
		}	    
	    }		
	    
	    //displays the number of points
	    g.setFont(new Font("Corsiva Hebrew", Font.PLAIN, 40));
	    g.setColor(Color.RED);
	    String str1 = "Points: " + eaten + "!";
	    g.drawString(str1, 0, 35);
	    
	    //displays the current elapsed time of the game in seconds
	    g.setFont(new Font("Corsiva Hebrew", Font.PLAIN, 30));
	    g.setColor(Color.RED);
	    String str2 = "Seconds Elapsed: " + timer;
	    g.drawString(str2, 0, 65);

	    if(eaten >= 50) {
		stop = true;
		g.setFont(new Font("Corsiva Hebrew", Font.PLAIN, 100));
		g.setColor(Color.RED);
		String win = "YOU WON!";
		g.drawString(win, 350, 400);
	    }
	    else if(eaten <= -25) {
		stop = true;
		g.setFont(new Font("Corsiva Hebrew", Font.PLAIN, 100));
		g.setColor(Color.RED);
		String lose = "Game Over!";
		String lose2 = "Better luck next time!";
		g.drawString(lose, 325, 250);
		g.drawString(lose2, 140, 400);
	    }   
	}
    } //end DrawingPanel
    
    /**
       Class that animates the game to get the fish and bubbles to move.
    */
    class Animate extends Thread{
	public void run(){
	    
	    try {
		while(true) {
		    display(delay);
		} //end while loop
	    } catch(Exception ex) {
		if(ex instanceof InterruptedException){}
		else { //Unexpected exception occurred.
		    System.out.println(ex);
		    System.exit(1); //terminate program
		} //end else
	    } //end catch
	} //end run
	
	/** 
	    Class that holds the information for each separate fish in order to
	    animate the fish and know its whereabouts on the screen.
	*/
	class FishInfo {
	    double x, y, width, height;
	    FishInfo(double x, double y, double width, double height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	    }
	    double getXPos() {
		return x;
	    }
	    double getYPos() {
		return y;
	    }
	    double getWidth() {
		return width;
	    }
	    double getHeight() {
		return height;
	    }
	}	   
	
	/**
	   Creates each frame and also checks if the fish have been eaten.  
	   Removes fish and creates a new fish if it has been eaten.
	**/
	void display(int delay) 
	
	    throws InterruptedException {
	    // uses the stop boolean variable to pause
	    if(!stop) {
		ArrayList<FishInfo> info = new ArrayList<FishInfo>();
	 
		for(int i = 0; i < fishArray.size(); i++) {

		    // get x position, y position, width, and height of fish
		    int xf = (int) fishArray.get(i).getXPos();
		    int yf = (int) fishArray.get(i).getYPos();
		    int wf = (int) fishArray.get(i).getWidth();
		    int hf = (int) fishArray.get(i).getHeight();
		
		    /* check if each fish is at sharks mouth and remove from screen and
		       increment eaten if true */
		    if ((xf > posX - 40 && xf < posX + 40) && (yf > posY - 25 && yf < posY + 25)) {
			info.add(new FishInfo(fishPanel.getWidth(), Math.random() * maxY, wf, hf));    
			eaten++;
		    }
		    else {
			info.add(new FishInfo(xf, yf, wf, hf));
		    }
		}

		for(int i = 0; i < jellyfish.size(); i++) {
		    // get x and y positions of jellyfish
		    int xj = (int) jellyfish.get(i).getXPos();
		    int yj = (int) jellyfish.get(i).getYPos();

		    // check if shark is touching a jellyfish and delete jellyfish and penalize if true
		    if((xj - 20 > posX - 200 && xj + 20 < posX + 40) && (yj + 60 > posY - 60 && yj < posY + 25)) {
			eaten -= 10;
			// reset jellyfish position
			jellyfish.get(i).setX(((int) ((Math.random() * 12345) % fishPanel.getWidth())));
			jellyfish.get(i).setY((int) fishPanel.getHeight());
		    }
		} 
		fishArray.clear();

		// Displays the Bubbles of the bubblesArray ArrayList
		for(int i = 0; i < bubblesArray.size(); i++) {
		    if (bubblesArray.get(i).getYPos() <= -20) {
			bubblesArray.get(i).setX((int) (Math.random() * fishPanel.getWidth()));
			bubblesArray.get(i).setY(fishPanel.getHeight());
		    }
		    else
			// moves the bubbles 2 pixels
			bubblesArray.get(i).move(2);
	     
		}
		// Displays the jellyfish ArrayList
		for(int i = 0; i < jellyfish.size(); i++) {
		    if(jellyfish.get(i).getYPos() <= -100){
			jellyfish.get(i).setX(((int) ((Math.random() * 12345) % fishPanel.getWidth())));
			jellyfish.get(i).setY((int) fishPanel.getHeight());	    
		    }

		    // If the jellyfish is true it moves 10 pixels and setMove to false
		    if(jellyfish.get(i).CheckJellyFish() == true) {
			if (jellyfish.get(i).getCount() % 20 == 0) {
			    jellyfish.get(i).move(jellyfish.get(i).getSpeed());
			    jellyfish.get(i).setMove(false);
			}
		    }

		    // Sets move from false to true
		    if (jellyfish.get(i).getCount() % 20 == 10) {
			jellyfish.get(i).setMove(true);
		    }
		    jellyfish.get(i).setCount();
		}
	  
		// Speed of the fish moving across the screen
		double currentSpeed = numFish % 6;
		for(int i = 0; i < info.size(); i++) {
		    if((currentSpeed - 1) < 2) {
			currentSpeed = (numFish % 10) + 1;
		    }
		    else {
			currentSpeed--;
		    }
		    double newX = info.get(i).getXPos() - currentSpeed;
		    int wp = fishPanel.getWidth();
		    int hp = fishPanel.getHeight();

		    if(newX < -50) {
			addNewFish(new Fish(wp, ((Math.random() * hp) % hp), info.get(i).getWidth(), info.get(i).getHeight()));
		    }
		    else {
			addNewFish(new Fish(newX, info.get(i).getYPos(), info.get(i).getWidth(), info.get(i).getHeight()));
		    }
		}
	    
		/* Some math to calculate the real time minus the amount of time the game 
		   was paused in order to keep the game at the correct time always */
		if(!stop) {
		    time2 = System.nanoTime() / 1000000000 - pausetime;
		    timer = (int)(time2 - time1) + timerload;
		}
	    
		fishPanel.repaint();
		if(Thread.currentThread().interrupted()) {
		    throw(new InterruptedException());
		}
		
		Thread.currentThread().sleep(delay);
	    } // end display method
	}
    } // end inner class named Animate
    
    /**
       Class to handle mouse events. Some methods are present but not defined
       because every method of the implemented class must be present to avoid
       compiler error
    */
    public class MouseHandler implements MouseListener, MouseMotionListener{
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
	}	
	
	public void mouseEntered(MouseEvent e) {
	}
        
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseMoved(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
	    if(stop == false) {
		posX = e.getX();
		posY = e.getY();
	    }
	}	
    }
	
    /** 
	Class that creates an in game menu in order to Pause, 
	resume, and save the game.
    */
    class GameMenu implements ActionListener {
	JButton Pause;
	URL pauseURL = getClass().getResource("/resources/PauseButton.jpg");
	ImageIcon pause = new ImageIcon(pauseURL);
	JButton Save = new JButton("Save & Exit");
	JButton Exit = new JButton("Exit");
	
	public void main (String[] args) {
	    GameMenu menu = new GameMenu();
	    menu.makemenu();
	}
	
	/**
	   Main GUI interface for the in-Game GUI. This menu 
	   allows for user to select pause, resume, or save&exit.
	*/
	public void makemenu() {
	
	    Pause = new JButton(pause);
	    Pause.addActionListener(this);
	    Save.addActionListener(this);
	    Exit.addActionListener(this);
	    
	    JPanel allTheButtons = new JPanel(new BorderLayout());
	    animation.getContentPane().add(BorderLayout.SOUTH, allTheButtons);
	    allTheButtons.add(BorderLayout.EAST, Exit);
	    allTheButtons.add(BorderLayout.CENTER, Save);
	    allTheButtons.add(BorderLayout.WEST, Pause);
	    animation.setVisible(true);
	}
	
	/** 
	   Method to allow for actions that are performed on the button
	   to be taken in and used as the source for an event to take place
	*/
	public void actionPerformed(ActionEvent event) {
	    if(event.getSource() == Pause) {
		if(stop == false) {
		    stop = true;
		    pausestart = System.nanoTime() / 1000000000;
		}
		else {
		    stop = false;
		    pausetime += (System.nanoTime() / 1000000000 - pausestart);
		}
	    }

	    /* serialize if save was clicked.  Exits the game as well after the save.
	       Saves all of the positions of all the different items that are on the 
	       screen including the shark, fish, jelly, and the boat */
	    if(event.getSource() == Save) {
		try {
		    FileOutputStream fs = new FileOutputStream("saved.ser");
		    ObjectOutputStream os = new ObjectOutputStream(fs);
		    os.flush();
		    os.writeInt(eaten);
		    os.writeInt(numJellyFish);
		    os.writeInt(timer);
		    os.writeInt(posX);
		    os.writeInt(posY);
		    os.writeInt(boatX);
		    for(int k = 0; k < numFish; k++) {
			os.writeDouble(fishArray.get(k).getXPos());
			os.writeDouble(fishArray.get(k).getYPos());
			os.writeDouble(fishArray.get(k).getWidth());
			os.writeDouble(fishArray.get(k).getHeight());
		    }
		    for(int i = 0; i < numJellyFish; i++) {
		    int toWriteX = (int) jellyfish.get(i).getXPos();
		    int toWriteY = (int) jellyfish.get(i).getYPos();
			os.writeInt(toWriteX);
			os.writeInt(toWriteY);
			os.writeDouble(jellyfish.get(i).getSpeed());
		    }
		    os.close();
		}
		catch(Exception ex) {
		    ex.printStackTrace();
		}
		eaten = 0;
		numJellyFish = 0;
		System.exit(0);
	    }
		//If exit is clicked exits the game
		if(event.getSource() == Exit) {
			System.exit(0);
		}
	}
	
    }
    
} // end class Animate
