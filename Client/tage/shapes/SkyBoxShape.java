package tage.shapes;

import tage.*;

/**
 * The GameEngine instantiates one SkyBox object, there should be no reason for
 * the game application
 * to call the constructor for this class.
 * <p>
 * This is the Shape for the skybox object.
 * The game application then instantiates one or more Texture Cube Maps for
 * texturing the skybox.
 * <p>
 * The SkyBox object itself is the same cube as defined in Cube.java, except
 * that the
 * texture coordinates and normal vectors are ignored.
 * It has 36 vertices, winding order CW, but we will be viewing it from the
 * inside so we
 * set the winding order to CCW when we render it.
 * The six textures are implemented as an OpenGL cube map using the loading
 * tools in Utils.java.
 * <p>
 * SkyBox is described in Chapter 9 of Computer Graphics Programming in OpenGL
 * with Java.
 * 
 * @author Scott Gordon
 */
public class SkyBoxShape extends ObjShape {
	float[] vertices = { -10000.0f, 10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, 10000.0f, -10000.0f,
			-10000.0f, // back face
			// lower left
			10000.0f, -10000.0f, -10000.0f, 10000.0f, 10000.0f, -10000.0f, -10000.0f, 10000.0f, -10000.0f, // back face
																											// upper
																											// right
			10000.0f, -10000.0f, -10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, 10000.0f, -10000.0f, // right face
																											// lower
																											// back
			10000.0f, -10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, -10000.0f, // right face
																										// upper front
			10000.0f, -10000.0f, 10000.0f, -10000.0f, -10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, // front face
																											// lower
																											// right
			-10000.0f, -10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, // front face
																											// upper
																											// left
			-10000.0f, -10000.0f, 10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, 10000.0f, 10000.0f, // left face
																											// lower
																											// front
			-10000.0f, -10000.0f, -10000.0f, -10000.0f, 10000.0f, -10000.0f, -10000.0f, 10000.0f, 10000.0f, // left face
																											// upper
																											// back
			-10000.0f, -10000.0f, 10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, -10000.0f, -10000.0f, // bottom
																											// face
																											// right
																											// front
			10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, -10000.0f, 10000.0f, // bottom
																												// face
																												// left
																												// back
			-10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, 10000.0f, // top face
																											// right
																											// back
			10000.0f, 10000.0f, 10000.0f, -10000.0f, 10000.0f, 10000.0f, -10000.0f, 10000.0f, -10000.0f }; // top face
																											// left
																											// front

	float[] texCoords = new float[] { 10000.0f, 10000.0f, 10000.0f, 0.0f, 0.0f, 0.0f, // back face
			0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, 10000.0f,
			10000.0f, 0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, // right face
			0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, 10000.0f,
			10000.0f, 0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, // front face
			0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, 10000.0f,
			10000.0f, 0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, // left face
			0.0f, 0.0f, 0.0f, 10000.0f, 10000.0f, 10000.0f,
			0.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, 0.0f, // bottom face
			10000.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10000.0f,
			0.0f, 10000.0f, 10000.0f, 10000.0f, 10000.0f, 0.0f, // top face
			10000.0f, 0.0f, 0.0f, 0.0f, 0.0f, 10000.0f };

	float[] normals = new float[] { 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, // back face
			0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f,
			10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, // right face
			10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f,
			0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, // front face
			0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f,
			-10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, // left face
			-10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f,
			0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, // bottom face
			0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f, 0.0f, -10000.0f, 0.0f,
			0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, // top face
			0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f, 0.0f, 10000.0f, 0.0f };

	/** for engine use only. */
	public SkyBoxShape() {
		setNumVertices(36);
		setVertices(vertices);
		setTexCoords(texCoords);
		setNormals(normals);
		setWindingOrderCCW(true);
	}
}