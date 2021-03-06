/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package jfxwithjbox2d;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import jfxwithjbox2d.SpriteClass.Sprite;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;

/**
 *
 * @author dilip
 */
public class JFXwithJBox2d extends Application {
    
	//private static final Image IMAGE = new Image("http://upload.wikimedia.org/wikipedia/commons/7/73/The_Horse_in_Motion.jpg");

    private static final int COLUMNS  =   4;
    private static final int COUNT    =  10;
    private static final int OFFSET_X =  18;
    private static final int OFFSET_Y =  25;
    private static final int WIDTH    = 374;
    private static final int HEIGHT   = 243;
    
    static boolean playerFacingRight = true;
    
    final static Set<PhysicsObject> physicsObjects = new HashSet<PhysicsObject>();
    final static Set<Updatee> updatees = new HashSet<Updatee>();
	static Group root;
	
	static FlameAudioTest flameAudio = new FlameAudioTest();
	protected boolean flamethrowing = false;
	protected float flameProgress = 0.f;
	protected float flamePeriod = 0.01f;
	Random generator = new Random();
	
	long time = System.nanoTime();
	protected float dragX;
	protected float dragY;
	
	static Sprite playerSprite;

	public static ImageView titleScreen;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		new AudioPlayerSimple().play("BGM_0.ogg",-10.f); 
		
		primaryStage.setTitle("TERRORFORM!");
		primaryStage.setFullScreen(false);
		primaryStage.setResizable(false);
		//        Camera camera = new ParallelCamera();

		root = new Group(); //Create a group for holding all objects on the screen
		final Scene scene = new Scene(root, Utils.WIDTH, Utils.HEIGHT,Color.BLACK);

		//Ball array for hold the  balls
		final PhysicsObject[] ball = new PhysicsObject[Utils.NO_OF_BALLS];

		Random r = new Random(System.currentTimeMillis());

		CollisionDetector cd = new CollisionDetector();
		Utils.world.setContactListener(cd);
		
//		Ball asdf = new Ball(0, 2, 0, BodyType.STATIC, Color.ALICEBLUE);
//		SpriteClass bgclass = new SpriteClass();
//		bgclass.putImageStrip("bg", "background.png", 1, 1.f);
//		Sprite bgsprite = bgclass.new Sprite(661,256);
//		root.getChildren().add(bgsprite.imageView);
//		updatees.add(bgsprite);
//		bgsprite.setImageStrip("bg", false, false);
////		bgsprite.imageView.setTranslateY(200);
//		asdf.setNode(bgsprite.imageView);
//		physicsObjects.add(asdf);
		
		//Add player
		Utils.player = new Player(4,3);
		physicsObjects.add(Utils.player);
		SpriteClass spriteClass = new SpriteClass();
		spriteClass.putImageStrip("run", "./runcombined.png", 4, 0.5f);
		spriteClass.putImageStrip("standing", "./standing.png", 1, 1.f);
		spriteClass.putImageStrip("death", "./deathcombined.png", 5, 1.f);
		final Sprite sprite = spriteClass.new Sprite(80, 80);
		playerSprite = sprite;
//		Utils.player.getBody().setTransform(new Vec2(4,3), -(float)Math.PI/16);
		
		updatees.add(sprite);
		Utils.player.setNode(sprite.imageView);
		root.getChildren().add(Utils.player.getNode());
		sprite.setImageStrip("run", true, true);
		Utils.player.getBody().setFixedRotation(true);
		
		//center camera on player
		Utils.cameraX = Utils.player.getBody().getPosition().x-4;
		Utils.cameraY = Utils.player.getBody().getPosition().y-3;
		
		//Add terrain 
		Level level = new Level();
		ArrayList<PhysicsObject> levelObjects= level.levelOne();
		for(PhysicsObject thing : levelObjects )
		{
			physicsObjects.add(thing);
			root.getChildren().add(thing.getNode());
		}
		
		/**
		 * Generate balls and position them on random locations.  
		 */
		for(int i=0;i<Utils.NO_OF_BALLS;i++) {
			ball[i]=new Box(r.nextFloat()*8,r.nextFloat()*6+2, .1f, .05f, 0f, BodyType.DYNAMIC,Color.RED);
			//ball[i]=new Ball(r.nextFloat()*8,r.nextFloat()*6+2, .05f, BodyType.DYNAMIC,Color.RED)
			physicsObjects.add(ball[i]);
		}

		//Add ground to the application, this is where balls will land
		//Utils.addGround(20, 1);


		final Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);

		Duration duration = Duration.seconds(1.0/60.0); // Set duration for frame.
		
		
		
		//UPDATE event
		//Create an ActionEvent, on trigger it executes a world time step and moves the balls to new position 
		EventHandler<ActionEvent> ae = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				long newtime = System.nanoTime();
				float timePassed = (float)(newtime - time)/1000000000;
				time = newtime;
				timeStep(timePassed);
				for(Updatee updatee : updatees) updatee.update(timePassed);
			}
		};

		/**
		 * Set ActionEvent and duration to the KeyFrame. 
		 * The ActionEvent is trigged when KeyFrame execution is over. 
		 */
		KeyFrame frame = new KeyFrame(duration, ae, null,null);

		timeline.getKeyFrames().add(frame);

		//Create button to start simulation.
		final Button btn = new Button();
		btn.setLayoutX((Utils.WIDTH/2));
		btn.setLayoutY((Utils.HEIGHT/3));
		btn.setText("Start");
		btn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				timeline.playFromStart(); 
				btn.setVisible(false);
				root.getChildren().remove(titleScreen);
			}
		});
		
		titleScreen = new ImageView();
	        // simple displays ImageView the image as is
	    	try {
	    		Image image = new Image(new FileInputStream("startscreen.png"));
	            titleScreen.setImage(image);
	            
	    	} catch (FileNotFoundException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}

	    	titleScreen.setFitWidth(800);
	    	titleScreen.setPreserveRatio(true);
	    	titleScreen.setY(60);
	    	titleScreen.setX(0);
	    root.getChildren().add(titleScreen);
	    
		//Add button to the root group
		root.getChildren().add(btn);

		//Add all balls to the root group
		for(int i=0;i<Utils.NO_OF_BALLS;i++) {
			root.getChildren().add(ball[i].getNode());
		}

		//ON MOUSE EVENT
		//Draw hurdles on mouse event.
		EventHandler<MouseEvent> addHurdle = new EventHandler<MouseEvent>(){
			public void handle(MouseEvent me) {
				//Get mouse's x and y coordinates on the scene
				dragX = (float)me.getSceneX();
				dragY = (float)me.getSceneY();
				
				
			}
		};
		
		EventHandler<MouseEvent> onMouseDown = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if(!Utils.player.dead) {
					flameAudio.activate();
					flamethrowing = true;
				}
			}
		};
		
		EventHandler<MouseEvent> onMouseUp = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				flameAudio.deactivate();
				flamethrowing = false;
			}
		};

		//ON KEY EVENT
				//Draw hurdles on mouse event.
				EventHandler<KeyEvent> keyboardEvent = new EventHandler<KeyEvent>(){
					
						public void handle(KeyEvent me) {
							//Get mouse's x and y coordinates on the scene

							if(me.getCode() == KeyCode.A){
								//Utils.player.getBody().applyLinearImpulse(new Vec2(-.5f,0), Utils.player.getBody().getPosition());//(Utils.player.getBody().getLinearVelocity().add(new Vec2(-1f,0)));
								if(Utils.player.getBody().getLinearVelocity().x>-3){
									//Utils.player.getBody().applyForce(new Vec2(-5f,0f), Utils.player.getBody().getPosition());
									if(!Utils.player.dead) {
										Utils.player.getBody().setLinearVelocity(new Vec2(Utils.player.getBody().getLinearVelocity().x-2f,Utils.player.getBody().getLinearVelocity().y));
										sprite.setImageStrip("run", true, true);
										playerFacingRight = false;
									}
//									sprite.imageView.setScaleX(-1.f);
								}
							}else if(me.getCode() == KeyCode.D){
								if(Utils.player.getBody().getLinearVelocity().x <3){
									//Utils.player.getBody().applyForce(new Vec2(5f,0f), Utils.player.getBody().getPosition());
									if(!Utils.player.dead) {
										Utils.player.getBody().setLinearVelocity(new Vec2(Utils.player.getBody().getLinearVelocity().x+2f,Utils.player.getBody().getLinearVelocity().y));
										sprite.setImageStrip("run", true, true);
										playerFacingRight = true;
									}
//									sprite.imageView.setScaleX(1.f);
								}
							}else if((me.getCode() == KeyCode.SPACE || me.getCode()== KeyCode.W) && ((float)(time-Utils.lastLandscapeTime)/1000000000 < 0.1f)||Math.abs(Utils.player.getBody().getLinearVelocity().y)<0.01f) {
								if(!Utils.player.dead) {
									Utils.player.getBody().setLinearVelocity(Utils.player.getBody().getLinearVelocity().add(new Vec2(0,5f)));//.applyForce(new Vec2(0,100), Utils.player.getBody().getPosition());
									Utils.lastLandscapeTime -= 1000000000;
								}
							}
						}

						
						//Get mouse's x and y coordinates on the scene						
						/*
						if(me.getCode() == KeyCode.A){
							Utils.player.getBody().applyLinearImpulse(new Vec2(-.5f,0), Utils.player.getBody().getPosition());//(Utils.player.getBody().getLinearVelocity().add(new Vec2(-1f,0)));
						}else if(me.getCode() == KeyCode.D){
							Utils.player.getBody().applyLinearImpulse(new Vec2(.5f,0), Utils.player.getBody().getPosition());//setLinearVelocity(Utils.player.getBody().getLinearVelocity().add(new Vec2(1f,0)));//.applyForce(new Vec2(1000,0), Utils.player.getBody().getPosition());
						}else if((me.getCode() == KeyCode.SPACE || me.getCode()== KeyCode.W) && (float)(time-Utils.lastLandscapeTime)/1000000000 < 0.1f){
							Utils.player.getBody().setLinearVelocity(Utils.player.getBody().getLinearVelocity().add(new Vec2(0,5f)));//.applyForce(new Vec2(0,100), Utils.player.getBody().getPosition());
							Utils.lastLandscapeTime -= 1000000000;
						}*/

					

				};

		scene.setOnKeyPressed(keyboardEvent);
		scene.setOnMouseDragged(addHurdle);
		scene.setOnMousePressed(onMouseDown);
		scene.setOnMouseReleased(onMouseUp);
		
//        final ImageView imageView = new ImageView(/*IMAGE*/);
//        imageView.setViewport(new Rectangle2D(OFFSET_X, OFFSET_Y, WIDTH, HEIGHT));
//        
//        final Animation animation = new SpriteAnimation(
//                imageView,
//                Duration.millis(1000),
//                COUNT, COLUMNS,
//                OFFSET_X, OFFSET_Y,
//                WIDTH, HEIGHT
//        );
//        animation.setCycleCount(Animation.INDEFINITE);
//        animation.play();
//        root.getChildren().add(imageView);
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	protected void timeStep(float timePassed/*ish*/) {
		
		if(playerFacingRight) {
			Utils.player.getNode().setScaleX(1.0);
			Utils.player.getBody().setTransform(Utils.player.getBody().getTransform().position, -(float)Math.PI/16);
		}
		if(!playerFacingRight) {
			Utils.player.getNode().setScaleX(-1.0);
			Utils.player.getBody().setTransform(Utils.player.getBody().getTransform().position, (float)Math.PI/16);
		}
		
		if(Utils.player.getBody().getLinearVelocity().lengthSquared() < 0.1f) {
			if(!Utils.player.dead) playerSprite.setImageStrip("standing", false, false);
		}
		
		//Create time step. Set Iteration count 8 for velocity and 3 for positions
		Utils.world.step(timePassed, 8, 3); 

		//allow each object to perform any custom behavior
		for(Iterator<PhysicsObject> iter = physicsObjects.iterator(); iter.hasNext();) {
			PhysicsObject physicsObject = iter.next();
			if(physicsObject.act()) {
				iter.remove();
				JFXwithJBox2d.root.getChildren().remove(physicsObject.getNode());
				Utils.world.destroyBody(physicsObject.getBody());
			}
		}
		
		//Update camera
		Utils.updateCamera();

		//Move balls to the new position computed by JBox2D
		for(PhysicsObject physicsObject : physicsObjects) {
			Body body = (Body)physicsObject.getNode().getUserData();
			float xpos = Utils.toPixelPosX(body.getPosition().x);
			float ypos = Utils.toPixelPosY(body.getPosition().y);
			float angle = body.getAngle();
			body.setAngularVelocity(0.f);
			physicsObject.getNode().setRotate(180/Math.PI*angle);
			physicsObject.getNode().setLayoutX(xpos);
			physicsObject.getNode().setLayoutY(ypos);
		}
		
		if(flamethrowing && !Utils.player.dead) {
			flameProgress += timePassed;
			while(flameProgress >= flamePeriod) {
				flameProgress -= flamePeriod;
				
				float dx = Utils.toPosX(dragX) - Utils.player.getBody().getPosition().x + 0.1f*((float)generator.nextDouble()-0.5f);
				float dy = Utils.toPosY(dragY) - Utils.player.getBody().getPosition().y + 0.1f*((float)generator.nextDouble()-0.5f);
				Vec2 impulse = new Vec2(dx, dy);
				impulse.normalize();
				Vec2 spawnOffset = impulse.mul(.4f);
				impulse.mulLocal(.1f);
				
				//hurdle.getBody().getPosition();
				
				//Draw ball on this location. Set balls body type to static.
				//Ball hurdle = new Ball(Utils.toPosX(dragX), Utils.toPosY(dragY),.02f,BodyType.DYNAMIC,Color.BLUE);
				//TreeSegment hurdle = new TreeSegment(Utils.toPosX(dragX), Utils.toPosY(dragY),.2f,.4f , 1f);
				//Flame hurdle = new Flame(Utils.player.getBody().getPosition().x, Utils.player.getBody().getPosition().y, .1f);
				//hurdle.getBody().applyLinearImpulse(impulse, hurdle.getBody().getPosition());
				
				Vec2 adjustedSpawnPosition= Utils.player.getBody().getPosition().add(spawnOffset);
				Flame hurdle = new Flame(adjustedSpawnPosition.x,adjustedSpawnPosition.y, .1f);
				hurdle.getBody().applyLinearImpulse(impulse, adjustedSpawnPosition);
			}
		}

	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
