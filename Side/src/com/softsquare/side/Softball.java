package com.softsquare.side;

import java.util.ArrayList;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

public class Softball extends InputManager.InputReceiver {
	private Body innerBallBody;
	private Body particles[];
	private ArrayList<ArrayList<Body>> neighbours;
	private Vector2 forces[];
	private Vector2 center = new Vector2(0, 0);
	private Camera camera;
	private World world;
	private Vector2 externalForce = new Vector2(0,0);
	
	public Softball(World world, Camera camera) {
		InputManager.add(this);
		this.world = world;
		this.camera = camera;
		
		if(false) {
			Vector2 center = new Vector2(0, 550);
			int ballsNumber = 16;
			float ballsDist = 32;
			CircleShape ballShape = new CircleShape();
			ballShape.setRadius(5);
			float spring = 3;
	
			FixtureDef ballFixture = new FixtureDef();
			ballFixture.shape = ballShape;
			ballFixture.density = 0.2f;
			ballFixture.restitution = 0.5f;
			ballFixture.friction = 4.8f;
			
			float deltaAngle = (float) ((2.0f * Math.PI) / ballsNumber);
	
			Body[] bodies = new Body[ballsNumber];
	
			for (int i = 0; i < ballsNumber; i++) {
				float theta = deltaAngle * i;
				float x = (float) (ballsDist * Math.cos(theta));
				float y = (float) (ballsDist * Math.sin(theta));
	
				Vector2 ballPos = new Vector2(x, y);
	
				BodyDef ballDef = new BodyDef();
				ballDef.type = BodyType.DynamicBody;
				ballDef.position.set(center.x + ballPos.x,center.y+ballPos.y);
				Body ballBody = world.createBody(ballDef);
				ballBody.createFixture(ballFixture);
				
				bodies[i] = ballBody;
			}
			BodyDef innerBall = new BodyDef();
			innerBall.type = BodyType.DynamicBody;
			innerBall.position.set(center);
			innerBallBody = world.createBody(innerBall);
			innerBallBody.createFixture(ballFixture);
			
			for(int i = 0;i < ballsNumber; i++){
				int next = (i+1) % ballsNumber;
				Body curBody = bodies[i];
				Body nextBody = bodies[next];
				
				DistanceJointDef joint = new DistanceJointDef();
				joint.initialize(curBody, nextBody, curBody.getWorldCenter(), nextBody.getWorldCenter());
				joint.collideConnected = true;
				joint.frequencyHz = spring;
				joint.dampingRatio = 1.5f;
				
				world.createJoint(joint);
				
				joint.initialize(curBody, innerBallBody, curBody.getWorldCenter(), center);
				joint.collideConnected = true;
				joint.frequencyHz = spring;
				joint.dampingRatio = 1.5f;
				
				world.createJoint(joint);
			}
			Logger.logSuccess("Created standard ball N" + ballsNumber + " D" + ballsDist);
		}
		createTemp();		
	}
	
	public void createTemp() {
		final int count = 150;
		particles = new Body[count];
		neighbours = new ArrayList<ArrayList<Body>>();
		forces = new Vector2[count];
		neighbours.ensureCapacity(particles.length);
		for(int i = 0; i < particles.length; i++)
			neighbours.add(new ArrayList<Body>());
		
		Vector2 center = new Vector2(0, 700);
		
		CircleShape ballShape = new CircleShape();
		ballShape.setRadius(3);
		
		FixtureDef ballFixture = new FixtureDef();
		ballFixture.shape = ballShape;
		ballFixture.density = 0.1f;
		ballFixture.restitution = 0.9f;
		ballFixture.friction = 1.1f;
		
		for (int i = 0; i < particles.length; i++) {
			float theta = (float) (Math.random() * 2 * Math.PI);
			float dist = (float) (Math.random() * 150);
			float x = (float) (dist * Math.cos(theta));
			float y = (float) (dist * Math.sin(theta));

			Vector2 ballPos = new Vector2(x, y + 200);
			BodyDef ballDef = new BodyDef();
			ballDef.type = BodyType.DynamicBody;
			ballDef.position.set(center.x + ballPos.x,center.y+ballPos.y);
			particles[i] = world.createBody(ballDef);
			particles[i].createFixture(ballFixture);
		}
	}
	
	final private float H = 64.0f;
	
	public void findAllNeightbours() {
		for(int i = 0; i < particles.length; i++)
			neighbours.get(i).clear();
		
		for(int i = 0; i < particles.length; i++)
			for (int j = i + 1; j < particles.length; j++) {
				if(particles[j].getWorldCenter().dst(particles[i].getWorldCenter()) < H){
					neighbours.get(i).add(particles[j]);
					neighbours.get(j).add(particles[i]);
				}
			}
	}
		
	public void update(float deltaTime) {
		findAllNeightbours();
		computeCenter();
		
		
		float q;
		
		//viscosity impulses
		for (int i = 0; i < particles.length; i++) {
			for(int j = 0; j < neighbours.get(i).size(); j++) {
				
			}
		}
		
		for (int i = 0; i < particles.length; i++) {
			Vector2 A = particles[i].getWorldCenter();
			

			for (int j = i + 1; j < particles.length; j++) {
				if(particles[j].isActive() == false)continue;
				Vector2 B = particles[j].getWorldCenter();
				Vector2 dir = B.sub(A);
				float dist = dir.len();
				dir = dir.nor();
				float G = 6.6738480f;
				float m = 8*particles[i].getMass();
				float coef = (float)Math.exp(dist * 0.02f); //G*(m*m)/(0.000001f + dist*dist);
				float ab = (float)  neighbours.get(i).size()/(1 + neighbours.get(i).size() + neighbours.get(j).size());
				ab =  (float) ab;
				Vector2 force1 = new Vector2(dir.x * coef * ab, dir.y * coef * ab);
				Vector2 force2 = new Vector2(-dir.x * coef * (1 - ab), -dir.y * coef * (1 - ab));
				forces[i].set(forces[i].x + force1.x, forces[i].y + force1.y);
				forces[j].set(forces[j].x + force2.x, forces[j].y + force2.y);
			}
		}
		
		for (int i = 0; i < particles.length; i++)
			particles[i].applyForceToCenter(forces[i]);
		
		if(externalForce.len() > 0.0f) {
			for (int i = 0; i < particles.length; i++) {
				if(particles[i].isActive() == false)continue;
				Vector2 p = particles[i].getWorldCenter();
				float d = center.dst(p);
				float c = 0.00001f + 1/(1.0f+d*d/200);
				particles[i].applyLinearImpulse(externalForce.x*c, externalForce.y*c, p.x, p.y);
			}
			if(innerBallBody != null) 
				innerBallBody.applyLinearImpulse(externalForce, innerBallBody.getWorldCenter());
		}
		externalForce.set(0,0);
	}
	
	public void computeCenter() {
		float x = particles[0].getWorldCenter().x;
		float y = particles[0].getWorldCenter().y;
		float n = 0.0f;
		forces[0] = new Vector2(0,0);
		for(int i = 1; i < particles.length; i++) {
			forces[i] = new Vector2(0,0);
			if(particles[i].isActive() == false)continue;
			x += particles[i].getWorldCenter().x;
			y += particles[i].getWorldCenter().y;
			n++;
		}
		center.set(x/n, y/n);
	}
	
	public Vector2 getCenterPosition() {
		return center;
	}
	
	float impulseCoef = 100;
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.LEFT:
			externalForce.set(-impulseCoef*500, 0);
			break;
		case Keys.RIGHT:
			externalForce.set(impulseCoef*500, 0);
			break;
		case Keys.UP:
			externalForce.set(0, impulseCoef*500);
			break;
		case Keys.DOWN:
			externalForce.set(0, -impulseCoef*500);
			break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		Vector3 target = new Vector3(screenX, screenY, 0);
		camera.get().unproject(target);
		externalForce.set((target.x - center.x) * impulseCoef * 500, (target.y - center.y) * impulseCoef * 500);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public boolean wantInput() {
		return !Globals.editorMode;
	}
}
