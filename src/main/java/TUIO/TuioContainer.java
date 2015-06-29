/*
 TUIO Java library
 Copyright (c) 2005-2014 Martin Kaltenbrunner <martin@tuio.org>
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3.0 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library.
*/

package TUIO;

import java.util.*;

/**
 * The abstract TuioContainer class defines common attributes that apply to both subclasses {@link TuioObject} and {@link TuioCursor}.
 *
 * @author Martin Kaltenbrunner
 * @version 1.1.0
 */ 
abstract class TuioContainer extends TuioPoint {
	
	/**
	 * The unique session ID number that is assigned to each TUIO object or cursor.
	 */ 
	protected long session_id;
	/**
	 * The X-axis velocity value.
	 */ 
	
	protected float x_speed;
	/**
	 * 
	 * The Y-axis velocity value.
	 */ 
	protected float y_speed;
	/**
	 * The motion speed value.
	 */ 
	protected float motion_speed;	
	/**
	 * The motion acceleration value.
	 */ 
	protected float motion_accel;		
	/**
	 * A Vector of TuioPoints containing all the previous positions of the TUIO component.
	 */ 
	protected Vector<TuioPoint> path;
	/**
	 * Defines the ADDED state.
	 */ 
	public static final int TUIO_ADDED = 0;
	/**
	 * Defines the ACCELERATING state.
	 */ 
	public static final int TUIO_ACCELERATING = 1;
	/**
	 * Defines the DECELERATING state.
	 */ 
	public static final int TUIO_DECELERATING = 2;
	/**
	 * Defines the STOPPED state.
	 */ 
	public static final int TUIO_STOPPED = 3;
	/**
	 * Defines the REMOVED state.
	 */ 
	public static final int TUIO_REMOVED = 4;
	/**
	 * Reflects the current state of the TuioComponent
	 */ 
	protected int state;
	
	/**
	 * This constructor takes a TuioTime argument and assigns it along with the provided 
	 * Session ID, X and Y coordinate to the newly created TuioContainer.
	 *
	 * @param	ttime	the TuioTime to assign
	 * @param	si	the Session ID to assign
	 * @param	xp	the X coordinate to assign
	 * @param	yp	the Y coordinate to assign
	 */
	TuioContainer(TuioTime ttime, long si, float xp, float yp) {
		super(ttime,xp,yp);
		
		session_id = si;
		x_speed = 0.0f;
		y_speed = 0.0f;
		motion_speed = 0.0f;
		motion_accel = 0.0f;
		
		path = new Vector<TuioPoint>();
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		state = TUIO_ADDED;
	}
	
	/**
	 * This constructor takes the provided Session ID, X and Y coordinate 
	 * and assigs these values to the newly created TuioContainer.
	 *
	 * @param	si	the Session ID to assign
	 * @param	xp	the X coordinate to assign
	 * @param	yp	the Y coordinate to assign
	 */
	TuioContainer(long si, float xp, float yp) {
		super(xp,yp);
		
		session_id = si;
		x_speed = 0.0f;
		y_speed = 0.0f;
		motion_speed = 0.0f;
		motion_accel = 0.0f;
		
		path = new Vector<TuioPoint>();
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		state = TUIO_ADDED;
	}
	
	/**
	 * This constructor takes the atttibutes of the provided TuioContainer 
	 * and assigs these values to the newly created TuioContainer.
	 *
	 * @param	tcon	the TuioContainer to assign
	 */
	TuioContainer(TuioContainer tcon) {
		super(tcon);
		
		session_id = tcon.getSessionID();
		x_speed = 0.0f;
		y_speed = 0.0f;
		motion_speed = 0.0f;
		motion_accel = 0.0f;
		
		path = new Vector<TuioPoint>();
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		state = TUIO_ADDED;
	}
	
	/**
	 * Takes a TuioTime argument and assigns it along with the provided 
	 * X and Y coordinate to the private TuioContainer attributes.
	 * The speed and accleration values are calculated accordingly.
	 *
	 * @param	ttime	the TuioTime to assign
	 * @param	xp	the X coordinate to assign
	 * @param	yp	the Y coordinate to assign
	 */
	public void update(TuioTime ttime, float xp, float yp) {
		TuioPoint lastPoint = path.lastElement();
		super.update(ttime,xp,yp);
		
		TuioTime diffTime = currentTime.subtract(lastPoint.getTuioTime());
		float dt = diffTime.getTotalMilliseconds()/1000.0f;
		float dx = this.xpos - lastPoint.getX();
		float dy = this.ypos - lastPoint.getY();
		float dist = (float)Math.sqrt(dx*dx+dy*dy);
		float last_motion_speed = this.motion_speed;
		
		this.x_speed = dx/dt;
		this.y_speed = dy/dt;
		this.motion_speed = dist/dt;
		this.motion_accel = (motion_speed - last_motion_speed)/dt;
		
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		if (motion_accel>0) state = TUIO_ACCELERATING;
		else if (motion_accel<0) state = TUIO_DECELERATING;
		else state = TUIO_STOPPED;
	}
	
	/**
	 * This method is used to calculate the speed and acceleration values of
	 * TuioContainers with unchanged positions.
	 *
	 * @param	ttime	the TuioTime to assign
	 */
	public void stop(TuioTime ttime) {
		update(ttime,xpos,ypos);
	}
	
	/**
	 * Takes a TuioTime argument and assigns it along with the provided 
	 * X and Y coordinate, X and Y velocity and acceleration
	 * to the private TuioContainer attributes.
	 *
	 * @param	ttime	the TuioTime to assign
	 * @param	xp	the X coordinate to assign
	 * @param	yp	the Y coordinate to assign
	 * @param	xs	the X velocity to assign
	 * @param	ys	the Y velocity to assign
	 * @param	ma	the acceleration to assign
	 */
	public void update(TuioTime ttime, float xp, float yp , float xs, float ys, float ma) {
		super.update(ttime,xp,yp);
		x_speed = xs;
		y_speed = ys;
		motion_speed = (float)Math.sqrt(x_speed*x_speed+y_speed*y_speed);
		motion_accel = ma;
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		if (motion_accel>0) state = TUIO_ACCELERATING;
		else if (motion_accel<0) state = TUIO_DECELERATING;
		else state = TUIO_STOPPED;
	}
	
	/**
	 * Assigns the provided X and Y coordinate, X and Y velocity and acceleration
	 * to the private TuioContainer attributes. The TuioTime time stamp remains unchanged.
	 *
	 * @param	xp	the X coordinate to assign
	 * @param	yp	the Y coordinate to assign
	 * @param	xs	the X velocity to assign
	 * @param	ys	the Y velocity to assign
	 * @param	ma	the acceleration to assign
	 */
	public void update(float xp, float yp,float xs,float ys,float ma) {
		super.update(xp,yp);
		x_speed = xs;
		y_speed = ys;
		motion_speed = (float)Math.sqrt(x_speed*x_speed+y_speed*y_speed);
		motion_accel = ma;
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		if (motion_accel>0) state = TUIO_ACCELERATING;
		else if (motion_accel<0) state = TUIO_DECELERATING;
		else state = TUIO_STOPPED;
	}
	
	/**
	 * Takes the atttibutes of the provided TuioContainer 
	 * and assigs these values to this TuioContainer.
	 * The TuioTime time stamp of this TuioContainer remains unchanged.
	 *
	 * @param	tcon	the TuioContainer to assign
	 */
	public void update (TuioContainer tcon) {
		super.update(tcon);
		x_speed = tcon.getXSpeed();
		y_speed = tcon.getYSpeed();
		motion_speed = tcon.getMotionSpeed();
		motion_accel = tcon.getMotionAccel();
		path.addElement(new TuioPoint(currentTime,xpos,ypos));
		if (motion_accel>0) state = TUIO_ACCELERATING;
		else if (motion_accel<0) state = TUIO_DECELERATING;
		else state = TUIO_STOPPED;
	}
	
	/**
	 * Assigns the REMOVE state to this TuioContainer and sets
	 * its TuioTime time stamp to the provided TuioTime argument.
	 *
	 * @param	ttime	the TuioTime to assign
	 */
	public void remove(TuioTime ttime) {
		currentTime = new TuioTime(ttime);
		state = TUIO_REMOVED;
	}
	
	/**
	 * Returns the Session ID of this TuioContainer.
	 * @return	the Session ID of this TuioContainer
	 */
	public long getSessionID() {
		return session_id;
	}
	
	/**
	 * Returns the X velocity of this TuioContainer.
	 * @return	the X velocity of this TuioContainer
	 */
	public float getXSpeed() {
		return x_speed;
	}
	
	/**
	 * Returns the Y velocity of this TuioContainer.
	 * @return	the Y velocity of this TuioContainer
	 */
	public float getYSpeed() {
		return y_speed;
	}
	
	/**
	 * Returns the position of this TuioContainer.
	 * @return	the position of this TuioContainer
	 */
	public TuioPoint getPosition() {
		return new TuioPoint(xpos,ypos);
	}
	
	/**
	 * Returns the path of this TuioContainer.
	 * @return	the path of this TuioContainer
	 */
	public ArrayList<TuioPoint> getPath() {
		return new ArrayList<TuioPoint>(path);
	}
	
	/**
	 * Returns the motion speed of this TuioContainer.
	 * @return	the motion speed of this TuioContainer
	 */
	public float getMotionSpeed() {
		return motion_speed;
	}
	
	/**
	 * Returns the motion acceleration of this TuioContainer.
	 * @return	the motion acceleration of this TuioContainer
	 */
	public float getMotionAccel() {
		return motion_accel;
	}
	
	/**
	 * Returns the TUIO state of this TuioContainer.
	 * @return	the TUIO state of this TuioContainer
	 */
	public int getTuioState() {
		return state;
	}
	
	/**
	 * Returns true of this TuioContainer is moving.
	 * @return	true of this TuioContainer is moving
	 */
	public boolean isMoving() { 
		if ((state==TUIO_ACCELERATING) || (state==TUIO_DECELERATING)) return true;
		else return false;
	}
	
}
