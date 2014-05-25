package com.softsquare.side;

import java.util.ArrayList;
import java.util.Arrays;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Fluid extends InputManager.InputReceiver implements QueryCallback {
	private World world = null;
	private Camera camera = null;
	private float fluidMinX = -6000.0f;
	private float fluidMaxX = 6000.0f;
	private float fluidMinY = -50.0f;
	private float fluidMaxY = 1000.0f;
	private float fluidNeighbours = 0;
	private final double EPSILON = 0.00000011920928955078125f;
	public static Integer LIQUID_INT = new Integer(1234598372);
	final int groupIndex = -10;
	private Body[] liquid = null;
	private PointLight[] lights = null;
	private Vector3[] params = null;
	private Body grab = null;
	public float fallX = 0;
	public float fallY = 150.0f;
	private ArrayList<Integer>[][] hash = null;
	private float[] vlen = null;
	private float[] xchange = null;
	private float[] ychange = null;
	float[] xs = null;
	float[] ys = null;
	float[] vxs = null;
	float[] vys = null;
	private int hashWidth = 64;
	private int hashHeight = 64;
	private Vector2 center = new Vector2(0, 0);
	private Vector2 externalForce = new Vector2(0,0);
	int li = 0;
	private Vector2 dragCenter = new Vector2(0,0);
	boolean drag = false;
	int scrx, scry;
	Vector2 target = new Vector2(0,0);
	ArrayList<Body> dragBodies = new ArrayList<Body>();
	ArrayList<Body> bodies = new ArrayList<Body>();
	ArrayList<MouseJoint> joints = new ArrayList<MouseJoint>();
	RayHandler rayHandler;

	public Fluid(World world, RayHandler rayHandler, Camera camera) {
		this.world = world;
		this.camera = camera;
		this.rayHandler = rayHandler;
		rayHandler.setContactFilter((short)0xffff, (short)groupIndex, (short)0xffff);
		
		if(!SideGame.isOnDesktop) {
			Globals.fluidAlphaStart.set(0.2);
			Globals.fluidAlphaEnd.set(0.9);
			Globals.fluidAlphaStep.set(0.2);
			Globals.fluidAlphaBlendingEnabled.set(true);
			Globals.fluidOptimizedRendering.set(true);
			Globals.fluidOptimizedParticleRadius.set(1.0);
			Globals.fluidOptimizedParticleStep.set(4);
			Globals.fluidNumParticles.set(32);
			Globals.fluidIdealRad.set(10.0);
			Globals.fluidMass.set(10.0);
			Globals.fluidFallWidth.set(25.0);
			Globals.fluidFallHeight.set(25.0);
			Globals.fluidComputeFluidMinMax.set(true);
			hashWidth = 8;
			hashHeight = 8;
			fluidMinX = -500.0f;
			fluidMaxX = 500.0f;
			fluidMinY = Globals.fluidMinY.get().floatValue() - 10.0f;
			fluidMaxY = 300.0f;
		}   
    }
	
	public void createLiquidDrop(int i, float x, float y, boolean randomBox) {
		int nParticles = Globals.fluidNumParticles.get().intValue();
		float totalMass = Globals.fluidMass.get().floatValue();
		float particleRad = Globals.fluidParticleRadius.get().floatValue();
		float fluidRestitution = Globals.fluidRestitution.get().floatValue();
		float fluidFriction = Globals.fluidFriction.get().floatValue();
		float boxWidth = Globals.fluidFallWidth.get().floatValue();
		float boxHeight = Globals.fluidFallHeight.get().floatValue();
		float gravityScale = Globals.fluidGravityScale.get().floatValue();
		boolean sleepingAllowed = Globals.fluidSleepingAllowed.get();
		float massPerParticle = totalMass / nParticles;
		CircleShape pd = new CircleShape();
		FixtureDef fd = new FixtureDef();
		fd.shape = pd;
		fd.density = 1.0f;
		fd.filter.groupIndex = groupIndex;
		pd.setRadius(particleRad);
		fd.restitution = fluidRestitution;
		fd.friction = fluidFriction;
		float cx = x;
		float cy = y;
		if(randomBox) {
			cx = randomFloat(x - boxWidth*.5f, x + boxWidth*.5f);
			cy = randomFloat(y - boxHeight*.5f, y + boxHeight*.5f);
		}
		BodyDef bd = new BodyDef();
		bd.position.set(new Vector2( cx, cy ));
		bd.fixedRotation = true;
		bd.type = BodyType.DynamicBody;
		bd.gravityScale = gravityScale;
		Body b = world.createBody(bd);
		b.createFixture(fd).setUserData(LIQUID_INT);
		MassData md = new MassData();
		md.mass = massPerParticle;
		md.I = 1.0f;
		b.setMassData(md);
		b.setSleepingAllowed(sleepingAllowed);
		liquid[i] = b;
	}
	
	
	void updateLights() {
		for (int i=0; i< lights.length; ++i) {
			lights[i].setPosition(liquid[i].getPosition());
		}
	}
	
	public void reset() {
		InputManager.remove(this);
		if(hash != null) {
			for (int i=0; i<hashWidth; ++i) {
	        	for (int j=0; j<hashHeight; ++j) {
	        		hash[i][j] = null;
	        	}
	        }
			hash = null;
		}
		
		dragBodies.clear();
		bodies.clear();
		if(lights != null)
			for (int i=0; i<lights.length; ++i)
				lights[i].remove();
		lights = null;
		for(MouseJoint j : joints)
			world.destroyJoint(j);
		joints.clear();
		if(params != null) {
			params = null;
		}
		if(vlen != null) vlen = null;
		if(xchange != null) xchange = null;
		if(ychange != null) ychange = null;
		if(xs != null) xs = null;
		if(ys != null) ys = null;
		if(vxs != null) vxs = null;
		if(vys != null) vys = null;
		if(liquid != null) {
			for(Body b : liquid) {
				world.destroyBody(b);
			}
			liquid = null;
		}
		if(grab != null) {
			world.destroyBody(grab);
			grab = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void init(float fallX, float fallY) {
		reset();
		InputManager.add(this);
		this.fallX = fallX;
		this.fallY = fallY;
		
		hashWidth = Globals.fluidHashWidth.get().intValue();
		hashHeight = Globals.fluidHashHeight.get().intValue();
        hash = new ArrayList[hashWidth][hashHeight];
        for (int i=0; i<hashWidth; ++i) {
        	for (int j=0; j<hashHeight; ++j) {
        		hash[i][j] = new ArrayList<Integer>();
        	}
        }
        
		int nParticles = Globals.fluidNumParticles.get().intValue();
		liquid = new Body[nParticles];
		lights = new PointLight[nParticles];
		params = new Vector3[liquid.length];
		vlen = new float[liquid.length];
		xchange = new float[liquid.length];
		ychange = new float[liquid.length];
		xs = new float[liquid.length];
		ys = new float[liquid.length];
		vxs = new float[liquid.length];
		vys = new float[liquid.length];
		for (int i=0; i<liquid.length; ++i){
			params[i] = new Vector3(0, 0, 0);
			createLiquidDrop(i, fallX, fallY, true);
			lights[i] = new PointLight(rayHandler, 32);
			lights[i].setPosition(liquid[i].getPosition());
			lights[i].setDistance(100);
			lights[i].setColor(0.2f, 0.5f, 0.7f, 0.1f);
			lights[i].setSoft(false);
			lights[i].setSoftnessLenght(20);
		}
		BodyDef bodyDef = new BodyDef();
		grab = world.createBody(bodyDef);
	}
	
	public float randomFloat(float a, float b) {
		return (float) (a + Math.random()*(b-a));
	}	
	
	private float map(float val, float fromMin, float fromMax, float toMin, float toMax) {
		float mult = (val - fromMin) / (fromMax - fromMin);
		float res = toMin + mult * (toMax - toMin);
		return res;
	}
	
	private int hashX(float x) {
		float f = map(x, fluidMinX, fluidMaxX, 0, hashWidth-.001f);
		return (int)f;
	}
	
	private int hashY(float y) {
		float f = map(y,fluidMinY,fluidMaxY,0,hashHeight-.001f);
		return (int)f;
	}
	
	private void hashLocations() {
		for(int a = 0; a < hashWidth; a++) {
            for(int b = 0; b < hashHeight; b++){
            	hash[a][b].clear();
            }
        }
        for(int a = 0; a < liquid.length; a++) {
            int hcell = hashX(liquid[a].getWorldCenter().x);
            int vcell = hashY(liquid[a].getWorldCenter().y);
            if(hcell > -1 && hcell < hashWidth && vcell > -1 && vcell < hashHeight)
                hash[hcell][vcell].add(new Integer(a));
        }
	}
	
	private void applyLiquidConstraint(float deltaT) {
		float rad = Globals.fluidIdealRad.get().floatValue();
		float visc = Globals.fluidViscosity.get().floatValue();
		float targetPressure = Globals.fluidTargetPressure.get().floatValue();
		float gravityScale = Globals.fluidGravityScale.get().floatValue();
		final float idealRad = 50.0f;
		float multiplier = idealRad / rad;
		
		Arrays.fill(xchange,0.0f);
		Arrays.fill(ychange, 0.0f);
		
		for (int i=0; i<liquid.length; ++i) {
			xs[i] = multiplier*liquid[i].getWorldCenter().x;
			ys[i] = multiplier*liquid[i].getWorldCenter().y;
			vxs[i] = multiplier*liquid[i].getLinearVelocity().x;
			vys[i] = multiplier*liquid[i].getLinearVelocity().y;
		}
		
		fluidNeighbours = 0.0f;
		
		for(int i = 0; i < liquid.length; i++) {
			liquid[i].setGravityScale(gravityScale);
			
			ArrayList<Integer> neighbors = new ArrayList<Integer>();
	        int hcell = hashX(liquid[i].getWorldCenter().x);
	        int vcell = hashY(liquid[i].getWorldCenter().y);
	        for(int nx = -1; nx < 2; nx++) {
	            for(int ny = -1; ny < 2; ny++) {
	                int xc = hcell + nx;
	                int yc = vcell + ny;
	                if(xc > -1 && xc < hashWidth && yc > -1 && yc < hashHeight && hash[xc][yc].size() > 0) {
	                    for(int a = 0; a < hash[xc][yc].size(); a++) {
	                        Integer ne = (Integer)hash[xc][yc].get(a);
	                        if(ne != null && ne.intValue() != i) neighbors.add(ne);
	                    }
	                }
	            }
	        }
	        fluidNeighbours += (float)neighbors.size()/liquid.length;
	        
            
            float p = 0.0f;
            float pnear = 0.0f;
            for(int a = 0; a < neighbors.size(); a++) {
                Integer n = (Integer)neighbors.get(a);
                int j = n.intValue();
                float vx = xs[j]-xs[i];
                float vy = ys[j]-ys[i];
            
                if(vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
                    float vlensqr = (vx * vx + vy * vy);
                    if(vlensqr < idealRad*idealRad) {
                    	vlen[a] = (float)Math.sqrt(vlensqr);
                    	if (vlen[a] < EPSILON) vlen[a] = idealRad-.01f;
                        float oneminusq = 1.0f-(vlen[a] / idealRad);
                        p = (p + oneminusq*oneminusq);
                        pnear = (pnear + oneminusq*oneminusq*oneminusq);
                    } else {
                    	vlen[a] = Float.MAX_VALUE;
                    }
                }
            }
            
            float pressure = (p - targetPressure) / 2.0f;
            float presnear = pnear / 2.0f;
            params[i].x = 0.95f*params[i].x + 0.05f*p;
            params[i].y = 0.95f*params[i].y + 0.05f*pressure;
            params[i].z = 0.95f*params[i].z + 0.05f*presnear;
            float changex = 0.0F;
            float changey = 0.0F;
            for(int a = 0; a < neighbors.size(); a++) {
                Integer n = (Integer)neighbors.get(a);
                int j = n.intValue();
                float vx = xs[j]-xs[i];
                float vy = ys[j]-ys[i];
                if(vx > -idealRad && vx < idealRad && vy > -idealRad && vy < idealRad) {
                    if(vlen[a] < idealRad) {
                        float q = vlen[a] / idealRad;
                        float oneminusq = 1.0f-q;
                        float factor = oneminusq * (pressure + presnear * oneminusq) / (2.0F*vlen[a]);
                        float dx = vx * factor;
                        float dy = vy * factor;
                        float relvx = vxs[j] - vxs[i];
                        float relvy = vys[j] - vys[i];
                        factor = visc * oneminusq * deltaT;
                        dx -= relvx * factor;
                        dy -= relvy * factor;
                        xchange[j] += dx;
                        ychange[j] += dy;
                        changex -= dx;
                        changey -= dy;
                    }
                }
            }
	        xchange[i] += changex;
	        ychange[i] += changey;
        }
		
		final float speedup = 1.0f;
		for (int i=0; i<liquid.length; ++i) {
			Vector2 o = liquid[i].getPosition();
			Vector2 v = liquid[i].getLinearVelocity();
			liquid[i].setTransform(o.x + xchange[i] / multiplier, o.y + ychange[i] / multiplier, 0);
			liquid[i].setLinearVelocity(v.x + speedup * xchange[i] / (multiplier*deltaT), v.y + speedup * ychange[i] / (multiplier*deltaT));
		}
	}
	
	public void computeFluidMinMax() {
		float x = 0;
		float y = 0;
		fluidMinX = fluidMinY = Float.MAX_VALUE;
		fluidMaxX = fluidMaxY = -Float.MAX_VALUE;
		for(int i = 0; i < liquid.length; i++) {
			x = liquid[i].getWorldCenter().x;
			y = liquid[i].getWorldCenter().y;
			if(x < fluidMinX)fluidMinX = x;
			if(x > fluidMaxX)fluidMaxX = x;
			if(y < fluidMinY)fluidMinY = y;
			if(y > fluidMaxY)fluidMinY = y;
		}
		fluidMinX -= 50.0f;
		fluidMinY -= 50.0f;
		fluidMaxX += 50.0f;
		fluidMaxY += 50.0f;
		fluidMinX = (int)fluidMinX;
		fluidMinY = (int)fluidMinY;
		fluidMaxX = (int)fluidMaxX;
		fluidMinX = (int)fluidMinX;
	}
	
	public void computeCenters() {
		float x = 0;
		float y = 0;
		float n = 0;
		for(int i = 0; i < liquid.length; i++) {
			if(liquid[i].isActive() == false)continue;
			x += liquid[i].getWorldCenter().x;
			y += liquid[i].getWorldCenter().y;
			n++;
		}
		center.set(x/n, y/n);
		if(dragBodies.size() > 0) {
			x = 0;
			y = 0;
			for(Body b : dragBodies) {
				x += b.getWorldCenter().x;
				y += b.getWorldCenter().y;
			}
			dragCenter.set(x / (float)dragBodies.size(), y / (float)dragBodies.size());
		}
		else dragCenter.set(center.x, center.y);
	}
	
	public void addForce() {
		float forceToCenter = Globals.fluidForceToCenter.get().floatValue();
		float co = (SideGame.isOnDesktop)? 0.00075f : 0.5f;
		Vector2 f = new Vector2(0,0);
		Vector2 o = new Vector2(0,0);
		float c = 1.0f;
		if(forceToCenter > 0) {
			for(int i = 1; i < liquid.length; i++) {
				if(liquid[i].isActive()) {
					f.x = center.x - liquid[i].getWorldCenter().x;
					f.y = center.y - liquid[i].getWorldCenter().y;
					if(f.len() > 0) {
						c = (float) Math.exp(co*f.len());
						if(c > 20)c = 20;
						liquid[i].applyForceToCenter(f.nor().mul(c*forceToCenter));
					}
				}
			}
		}
		if(externalForce.len() > 0.000001f) {
			o.x = externalForce.x;
			o.y = externalForce.y;
			o.nor();
			o.x = 150*o.x + center.x;
			o.y = 150*o.y + center.y;
			for(int i = 1; i < liquid.length; i++) {
				if(liquid[i].isActive()) {
					f.x = o.x - liquid[i].getWorldCenter().x;
					f.y = o.y - liquid[i].getWorldCenter().y;
					c = (float) Math.exp(-0.1f*f.len()) * 500;
					liquid[i].applyForceToCenter(f.x * c, f.y *c);
				}
			}
			externalForce.set(0, 0);
		}
		if(drag) {
			Vector3 screen = new Vector3(scrx, scry, 0);
			camera.get().unproject(screen);
			target.x = screen.x;
			target.y = screen.y;
			for(MouseJoint j : joints) {
				j.setTarget(target);
			}
		}
	}
	
	private void checkBoundsAndRespawn() {
		boolean respawnAtCenter = Globals.fluidRespawnAtCenter.get();
		boolean randomOnRespawn = Globals.fluidRandomOnRespawn.get();
		float minY = Globals.fluidMinY.get().floatValue();
		float x = (respawnAtCenter) ? center.x : fallX;
		float y = (respawnAtCenter) ? center.y : fallY;
		for (int i=0; i<liquid.length; ++i) {
			if (liquid[i].getWorldCenter().y < minY) {
				world.destroyBody(liquid[i]);
				createLiquidDrop(i, x, y, randomOnRespawn);
			}
		}
	}
	
	private void dampenLiquid() {
		for (int i=0; i<liquid.length; ++i) {
			Body b = liquid[i];
			b.setLinearVelocity(b.getLinearVelocity().mul(0.995f));
		}
	}
	
	public void update(float deltaTime) {
		int nParticles = Globals.fluidNumParticles.get().intValue();
		float totalMass = Globals.fluidMass.get().floatValue();
		float particleRad = Globals.fluidParticleRadius.get().floatValue();
		float rad = Globals.fluidIdealRad.get().floatValue();
		float visc = Globals.fluidViscosity.get().floatValue();
		float targetPressure = Globals.fluidTargetPressure.get().floatValue();
		float hz = Globals.fluidStepHz.get().intValue();
		float dt = 1.0f/hz;
		if(Globals.fluidComputeFluidMinMax.get()) {
			computeFluidMinMax();
		} else {
			fluidMinX = -6000.0f; fluidMaxX = 6000.0f;
			fluidMinY = Globals.fluidMinY.get().floatValue() - 10.0f;
			fluidMaxY = 1000.0f;
		}
		hashLocations();
		applyLiquidConstraint(dt*1.0f);
		dampenLiquid();
		computeCenters();
		addForce();
		updateLights();
		if(Globals.fluidRespawn.get()) checkBoundsAndRespawn();
		Globals.game.addStats("");
		Globals.game.addStats(" Fluid (" + hz + "Hz)");
		Globals.game.addStats("   nParticles " + nParticles);
		Globals.game.addStats("   totalMass " + totalMass);
		Globals.game.addStats("   targetPressure " + targetPressure);
		Globals.game.addStats("   particleRad " + particleRad);
		Globals.game.addStats("   idealRad " + rad);
		Globals.game.addStats("   viscosity " + visc);
		Globals.game.addStats("   center " + center.toString());
		Globals.game.addStats("   minMax X " + fluidMinX + " " + fluidMaxX);
		Globals.game.addStats("   minMax Y " + fluidMinY + " " + fluidMaxY);
		Globals.game.addStats("   neigh " + fluidNeighbours);
		
		Globals.game.addStats("");
	}
	
	public void render() {
		float dragRadius = Globals.fluidDragRadius.get().floatValue();
		float particleRad = Globals.fluidParticleRadius.get().floatValue();
		float optimizedParticleRad = Globals.fluidOptimizedParticleRadius.get().floatValue();
		int optimizedParticleStep = Globals.fluidOptimizedParticleStep.get().intValue();
		boolean drawMassCenter = Globals.fluidDrawMassCenter.get();
		boolean drawDragLine = Globals.fluidDrawDragLine.get();
		boolean alphaBlendingEnabled = Globals.fluidAlphaBlendingEnabled.get();
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		if(alphaBlendingEnabled) {
			Gdx.gl.glEnable(GL10.GL_BLEND);
			Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}
		if(SideGame.isOnDesktop) {
			if(drawMassCenter) {
				shapeRenderer.begin(ShapeType.FilledCircle);
				shapeRenderer.setProjectionMatrix(camera.get().combined);
				shapeRenderer.setColor(1, 0, 1, 0.1f);
				shapeRenderer.filledCircle(center.x, center.y, 3);
				shapeRenderer.end();
			}
			if(drag && drawDragLine) {
				shapeRenderer.begin(ShapeType.Line);
				shapeRenderer.setProjectionMatrix(camera.get().combined);
				shapeRenderer.setColor(1, 0, 1, 0.5f);
				shapeRenderer.line(dragCenter.x, dragCenter.y, target.x, target.y);
				shapeRenderer.end();
			}
		}
		shapeRenderer.begin(ShapeType.FilledCircle);
		shapeRenderer.setProjectionMatrix(camera.get().combined);
		boolean expRad =  Globals.fluidExpRadius.get();
		boolean optimizedRendering =  Globals.fluidOptimizedRendering.get();
		if(optimizedRendering) {
			shapeRenderer.setColor(0.25f, 0.5f, 1, 1);
			for (int i = 0; i<liquid.length; ++i)
				shapeRenderer.filledCircle(liquid[i].getPosition().x, liquid[i].getPosition().y, particleRad*optimizedParticleRad, optimizedParticleStep);
		} else if(expRad) {
			for (int i = 0; i<liquid.length; ++i) {
				Body body = liquid[i];
				shapeRenderer.setColor(params[i].y, params[i].z, 1, 1);
				shapeRenderer.filledCircle(body.getPosition().x, body.getPosition().y, particleRad*(1+5*(float)Math.exp(-center.dst(body.getPosition())/16.0f)), 16);
			}
		} else {
			float alphaStart = Globals.fluidAlphaStart.get().floatValue();
			float alphaEnd = Globals.fluidAlphaEnd.get().floatValue();
			float alphaStep = Globals.fluidAlphaStep.get().floatValue();
			for (int i = 0; i<liquid.length; ++i) {
				Body body = liquid[i];
				float r = params[i].y;
				float g = params[i].z;
				float b = 1f;
				float o = params[i].z;
				if(drag && !body.getJointList().isEmpty()) {
					r = 1 - target.dst(body.getPosition())/(2.25f*dragRadius);
					g = g * 0.5f;
					b = 0.75f;
				}
				if(r > 1)r = 1; if(g > 1)g = 1; if(b > 1)b = 1;
				for(float a = alphaStart; a <= alphaEnd; a += 0.1f) {
					shapeRenderer.setColor(r, g, b, alphaStep);
					shapeRenderer.filledCircle(body.getPosition().x, body.getPosition().y, (1.0f-a)*particleRad*12*o, 16);
				}
			}
		}
		
		shapeRenderer.end();
		Gdx.gl.glDisable(GL10.GL_BLEND);	
	}
	
	public Vector2 getCenterPosition() {
		return center;
	}
	
	public Vector2 getCameraCenterPosition() {
		return dragCenter;
	}

	@Override
	public boolean reportFixture(Fixture fixture) {
		float dragRadius = Globals.fluidDragRadius.get().floatValue();
		if(fixture.getUserData() != null && fixture.getUserData().equals(LIQUID_INT) && fixture.getBody().getPosition().dst(target) < dragRadius) {
			bodies.add(fixture.getBody());
			return true;
		} else return true;
	}

	@Override
	public boolean wantInput() {
		return !Globals.editorMode;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		float dragRadius = Globals.fluidDragRadius.get().floatValue();
		Vector3 screen = new Vector3(scrx = screenX, scry = screenY, 0);
		camera.get().unproject(screen);
		target.x = screen.x;
		target.y = screen.y;
		if(button == 0) {
			joints.clear();
			bodies.clear();
			world.QueryAABB(this, target.x - dragRadius, target.y - dragRadius, target.x + dragRadius, target.y + dragRadius);
			grab.setTransform(center.x, center.y, 0);
			if(bodies.size() > 0)
				dragBodies.clear();
			for(Body b : bodies) {
				dragBodies.add(b);
				MouseJointDef def = new MouseJointDef();
				def.bodyA = grab;
				def.bodyB = b;
				def.collideConnected = false;
				def.dampingRatio = 0;
				def.frequencyHz = 60.0f;
				def.target.set(b.getPosition().x, b.getPosition().y);
				def.maxForce = Globals.fluidDragCoef.get().floatValue() * b.getMass();
				joints.add((MouseJoint)world.createJoint(def));
				b.setAwake(true);
			}
			drag = true;
		} 
		return false; 
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(drag) {
			Vector3 screen = new Vector3(scrx = screenX, scry = screenY, 0);
			camera.get().unproject(screen);
			target.x = screen.x;
			target.y = screen.y;
		} 
		return false; 
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		for(MouseJoint j : joints) {
			world.destroyJoint(j);
		}
		joints.clear();
		drag = false;
		return false; 
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Keys.LEFT_BRACKET:
				world.setGravity(world.getGravity().mul(0.75f));
				break;
			case Keys.RIGHT_BRACKET:
				world.setGravity(world.getGravity().mul(1.25f));
				break;
		}
		return false; 
	}
	
	@Override
	public boolean keyUp(int keycode) { return false; }
	@Override
	public boolean keyTyped(char character) { return false; }	
	@Override
	public boolean mouseMoved(int screenX, int screenY) { return false; }
	@Override
	public boolean scrolled(int amount) { return false; }
}
