package jfxwithjbox2d;

import javafx.scene.paint.Color;
import jfxwithjbox2d.SpriteClass.Sprite;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

public class GorillaEnemy extends Ball {

	boolean onFire=false;
	int health=150;
	Sprite sprite;
	boolean attacking = false;
	
	public GorillaEnemy(float posX, float posY) {
		super( posX, posY, .4f, BodyType.DYNAMIC, Color.GREY);
		
		JFXwithJBox2d.physicsObjects.add(this);
		SpriteClass spriteClass = new SpriteClass();
		spriteClass.putImageStrip("gorilla", "./walk strip.png", 4, .8f);
		spriteClass.putImageStrip("attack", "./attack strip.png", 3, .8f);
		spriteClass.putImageStrip("dead","./dead.png", 1, .8f);
		sprite = spriteClass.new Sprite(80, 80);
		JFXwithJBox2d.updatees.add(sprite);
		this.setNode(sprite.imageView);
		JFXwithJBox2d.root.getChildren().add(this.getNode());
		sprite.setImageStrip("gorilla", true, true);
		
	}

	boolean facingRight;
	boolean dead = false;
	
	@Override
	public boolean act() {
		float px = Utils.player.getBody().getPosition().x;
		float py = Utils.player.getBody().getPosition().y;
		float ex = this.getBody().getPosition().x;
		float ey = this.getBody().getPosition().y;
		int sign = -1;
		if(px>ex)
			sign = 1;

		if(onFire){
			//burn
//			health--;
		}
		if(health<=0){
			sprite.setImageStrip("dead", false, false);
			dead = true;
//			return true;
		}
		
		if(!dead) {
			//if close, charge
			if(Math.abs(px-ex)<2.5)
			{
				if(!attacking) {
					sprite.setImageStrip("attack", true, true);
					attacking = true;
				}
				this.getBody().applyForce( new Vec2 (sign*2, .8f),this.getBody().getPosition() );
			}
			//move towards player if within screen range
			else if(Math.abs(px-ex)<5){
				sprite.setImageStrip("gorilla", true, true);
				attacking = false;
				this.getBody().applyForce( new Vec2 (sign*.8f, 0f),this.getBody().getPosition() );
			}
			if(Math.abs(px-ex)<5)
			{
				//Throw coconuts
			}
			if(px-ex > 0) {
				facingRight = true;
				sprite.imageView.setScaleX(-1);
			} else {
				facingRight = false;
				sprite.imageView.setScaleX(1);
			}
		}
		
		
		return false;
	}

}
